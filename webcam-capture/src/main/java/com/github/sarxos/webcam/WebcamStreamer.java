package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebcamStreamer implements ThreadFactory, WebcamListener {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamStreamer.class);

	private static final String BOUNDARY = "mjpegframe";

	private class Acceptor implements Runnable {

		@Override
		public void run() {
			try {
				ServerSocket server = new ServerSocket(port);
				while (true) {
					executor.execute(new Connection(server.accept()));
				}
			} catch (Exception e) {
				LOG.error("Cannot accept socket connection", e);
			}
		}
	}

	private class Reader implements Runnable {

		@Override
		public void run() {
			while (webcam.isOpen()) {

				image = webcam.getImage();

				if (image != null) {
					synchronized (lock) {
						lock.notifyAll();
					}
				}

				try {
					Thread.sleep(getDelay());
				} catch (InterruptedException e) {
					LOG.error("Nasty interrupted exception", e);
				}
			}
		}
	}

	private class Connection implements Runnable {

		private static final String CRLF = "\r\n";

		private Socket socket = null;

		public Connection(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {

			BufferedReader br = null;
			BufferedOutputStream bos = null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try {

				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				bos = new BufferedOutputStream(socket.getOutputStream());

				try {

					while (webcam.isOpen()) {

						if (socket.isInputShutdown()) {
							break;
						}
						if (socket.isClosed()) {
							break;
						}

						String line = br.readLine();
						if (line == null || line.isEmpty()) {
							bos.write(("--" + BOUNDARY + "--" + CRLF).getBytes());
							LOG.info("Breaking");
							break;
						}

						if (line.startsWith("GET")) {

							socket.setSoTimeout(0);
							socket.setKeepAlive(true);

							getImage();

							StringBuilder sb = new StringBuilder();
							sb.append("HTTP/1.0 200 OK").append(CRLF);
							sb.append("Connection: keep-alive").append(CRLF);
							sb.append("Cache-Control: no-cache").append(CRLF);
							sb.append("Cache-Control: private").append(CRLF);
							sb.append("Pragma: no-cache").append(CRLF);
							sb.append("Content-type: multipart/x-mixed-replace; boundary=--").append(BOUNDARY).append(CRLF);
							sb.append(CRLF);

							bos.write(sb.toString().getBytes());

							do {

								if (socket.isInputShutdown()) {
									break;
								}
								if (socket.isClosed()) {
									break;
								}

								baos.reset();

								ImageIO.write(getImage(), "JPG", baos);

								sb.delete(0, sb.length());
								sb.append("--").append(BOUNDARY).append(CRLF);
								sb.append("Content-type: image/jpeg").append(CRLF);
								sb.append("Content-Length: ").append(baos.size()).append(CRLF);
								sb.append(CRLF);

								bos.write(sb.toString().getBytes());
								bos.write(baos.toByteArray());
								bos.write(CRLF.getBytes());

								try {
									bos.flush();
								} catch (SocketException e) {
									if (LOG.isTraceEnabled()) {
										LOG.error("Socket exception", e);
									}
								}

								Thread.sleep(getDelay());

							} while (webcam.isOpen());
						}
					}
				} catch (Exception e) {

					String message = e.getMessage();
					if (message != null && message.startsWith("Software caused connection abort")) {
						LOG.info("User closed stream");
						return;
					}

					LOG.error("Error", e);

					bos.write("HTTP/1.0 501 Internal Server Error\r\n\r\n\r\n".getBytes());
				}

			} catch (IOException e) {
				LOG.error("I/O exception", e);
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						LOG.error("Cannot close buffered reader", e);
					}
				}
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						LOG.error("Cannot close data output stream", e);
					}
				}
			}
		}
	}

	private Webcam webcam = null;
	private double fps = 0;
	private BufferedImage image = null;
	private Object lock = new Object();
	private int number = 0;
	private int port = 0;
	private Executor executor = Executors.newCachedThreadPool(this);
	private AtomicBoolean open = new AtomicBoolean(false);
	private AtomicBoolean initialized = new AtomicBoolean(false);

	public WebcamStreamer(int port, Webcam webcam, double fps, boolean start) {

		if (webcam == null) {
			throw new IllegalArgumentException("Webcam for streaming cannot be null");
		}

		this.port = port;
		this.webcam = webcam;
		this.fps = fps;

		if (start) {
			start();
		}
	}

	private long getDelay() {
		return (long) (1000 / fps);
	}

	private BufferedImage getImage() {
		if (image == null) {
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					LOG.error("Nasty interrupted exception", e);
				} catch (Exception e) {
					throw new RuntimeException("Problem waiting on lock", e);
				}
			}
		}
		return image;
	}

	public void start() {
		if (open.compareAndSet(false, true)) {
			webcam.addWebcamListener(this);
			webcam.open();
		}
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, String.format("streamer-thread-%s", number++));
		thread.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
		thread.setDaemon(true);
		return thread;
	}

	@Override
	public void webcamOpen(WebcamEvent we) {
		if (initialized.compareAndSet(false, true)) {
			executor.execute(new Acceptor());
			executor.execute(new Reader());
		}
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		// TODO: shutdown executor?
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
	}

	public static void main(String[] args) throws InterruptedException {
		new WebcamStreamer(8081, Webcam.getDefault(), 0.5, true);
		do {
			Thread.sleep(1000);
		} while (true);
	}
}
