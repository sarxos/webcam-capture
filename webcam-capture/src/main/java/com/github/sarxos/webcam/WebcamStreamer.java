package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebcamStreamer implements ThreadFactory, WebcamListener {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamStreamer.class);

	private static final String BOUNDARY = "mjpeg-frame";

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

		private Socket socket = null;

		public Connection(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {

			BufferedReader br = null;
			DataOutputStream dos = null;

			try {

				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				dos = new DataOutputStream(socket.getOutputStream());

				try {
					while (true) {

						String line = br.readLine();
						if (line.isEmpty()) {
							break;
						}

						if (line.startsWith("GET")) {

							socket.setSoTimeout(0);
							socket.setKeepAlive(true);

							StringBuilder sb = new StringBuilder();
							sb.append("HTTP/1.0 200 OK\n");
							sb.append("Connection: keep-alive\n");
							sb.append("Cache-Control: no-cache\n");
							sb.append("Cache-Control: private\n");
							sb.append("Pragma: no-cache\n");
							sb.append("Content-type: multipart/x-mixed-replace; boundary=").append(BOUNDARY).append("\n\n");

							dos.writeBytes(sb.toString());
							dos.flush();

							do {

								sb.delete(0, sb.length());

								sb.append("--").append(BOUNDARY).append("\n");
								sb.append("Content-type: image/jpeg\n\n");

								dos.writeBytes(sb.toString());

								ImageIO.write(getImage(), "JPG", dos);

								dos.flush();

								Thread.sleep(getDelay());

							} while (true);
						}
					}
				} catch (Exception e) {
					dos.writeBytes("HTTP/1.0 501 Internal Server Error\n\n\n");
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
				if (dos != null) {
					try {
						dos.close();
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
	}

	public static void main(String[] args) throws InterruptedException {
		new WebcamStreamer(8081, Webcam.getDefault(), 0.5, true);
		do {
			Thread.sleep(1000);
		} while (true);
	}
}
