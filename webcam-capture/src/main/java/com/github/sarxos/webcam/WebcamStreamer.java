package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebcamStreamer implements ThreadFactory, WebcamListener {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamStreamer.class);

	private static final String BOUNDARY = "mjpegframe";

	private static final String CRLF = "\r\n";

	private class Acceptor implements Runnable {

		@Override
		public void run() {
			try {
				ServerSocket server = new ServerSocket(port);
				while (started.get()) {
					Socket socket = server.accept();
					LOG.info("New connection from {}", socket.getRemoteSocketAddress());
					executor.execute(new Connection(socket));
				}
			} catch (Exception e) {
				LOG.error("Cannot accept socket connection", e);
			}
		}
	}

	private class Connection implements Runnable {

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
			} catch (IOException e) {
				LOG.error("Fatal I/O exception when creating socket streams", e);
				try {
					socket.close();
				} catch (IOException e1) {
					LOG.error("Canot close socket connection from " + socket.getRemoteSocketAddress(), e1);
				}
				return;
			}

			// consume whole input

			try {
				while (br.ready()) {
					br.readLine();
				}
			} catch (IOException e) {
				LOG.error("Error when reading input", e);
				return;
			}

			// stream

			try {

				socket.setSoTimeout(0);
				socket.setKeepAlive(false);
				socket.setTcpNoDelay(true);

				while (started.get()) {

					StringBuilder sb = new StringBuilder();
					sb.append("HTTP/1.0 200 OK").append(CRLF);
					sb.append("Connection: close").append(CRLF);
					sb.append("Cache-Control: no-cache").append(CRLF);
					sb.append("Cache-Control: private").append(CRLF);
					sb.append("Pragma: no-cache").append(CRLF);
					sb.append("Content-type: multipart/x-mixed-replace; boundary=--").append(BOUNDARY).append(CRLF);
					sb.append(CRLF);

					bos.write(sb.toString().getBytes());

					do {

						if (!webcam.isOpen() || socket.isInputShutdown() || socket.isClosed()) {
							br.close();
							bos.close();
							return;
						}

						baos.reset();

						long now = System.currentTimeMillis();
						if (now > last + delay) {
							image = webcam.getImage();
						}

						ImageIO.write(image, "JPG", baos);

						sb.delete(0, sb.length());
						sb.append("--").append(BOUNDARY).append(CRLF);
						sb.append("Content-type: image/jpeg").append(CRLF);
						sb.append("Content-Length: ").append(baos.size()).append(CRLF);
						sb.append(CRLF);

						try {
							bos.write(sb.toString().getBytes());
							bos.write(baos.toByteArray());
							bos.write(CRLF.getBytes());
							bos.flush();
						} catch (SocketException e) {
							LOG.error("Socket exception from " + socket.getRemoteSocketAddress(), e);
							br.close();
							bos.close();
							return;
						}

						Thread.sleep(delay);

					} while (started.get());
				}
			} catch (Exception e) {

				String message = e.getMessage();

				if (message != null) {
					if (message.startsWith("Software caused connection abort")) {
						LOG.info("User closed stream");
						return;
					}
					if (message.startsWith("Broken pipe")) {
						LOG.info("User connection broken");
						return;
					}
				}

				LOG.error("Error", e);

				try {
					bos.write("HTTP/1.0 501 Internal Server Error\r\n\r\n\r\n".getBytes());
				} catch (IOException e1) {
					LOG.error("Not ablte to write to output stream", e);
				}

			} finally {
				for (Closeable closeable : new Closeable[] { br, bos, baos }) {
					try {
						closeable.close();
					} catch (IOException e) {
						LOG.error("Cannot close socket", e);
					}
				}
				try {
					socket.close();
				} catch (IOException e) {
					LOG.error("Cannot close socket", e);
				}
			}
		}
	}

	private Webcam webcam = null;
	private double fps = 0;
	private int number = 0;
	private int port = 0;
	private long last = -1;
	private long delay = -1;
	private BufferedImage image = null;
	private ExecutorService executor = Executors.newCachedThreadPool(this);
	private AtomicBoolean started = new AtomicBoolean(false);

	public WebcamStreamer(int port, Webcam webcam, double fps, boolean start) {

		if (webcam == null) {
			throw new IllegalArgumentException("Webcam for streaming cannot be null");
		}

		this.port = port;
		this.webcam = webcam;
		this.fps = fps;
		this.delay = (long) (1000 / fps);

		if (start) {
			start();
		}
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, String.format("streamer-thread-%s", number++));
		thread.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
		thread.setDaemon(true);
		return thread;
	}

	public void start() {
		if (started.compareAndSet(false, true)) {
			webcam.addWebcamListener(this);
			webcam.open();
			executor.execute(new Acceptor());
		}
	}

	public void stop() {
		if (started.compareAndSet(true, false)) {
			executor.shutdown();
			webcam.removeWebcamListener(this);
			webcam.close();
		}
	}

	@Override
	public void webcamOpen(WebcamEvent we) {
		start();
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		stop();
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
	}

	public double getFPS() {
		return fps;
	}

	public boolean isInitialized() {
		return started.get();
	}

	public int getPort() {
		return port;
	}

}
