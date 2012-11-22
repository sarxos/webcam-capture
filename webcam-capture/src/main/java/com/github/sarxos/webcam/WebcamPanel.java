package com.github.sarxos.webcam;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simply implementation of JPanel allowing users to render pictures taken with
 * webcam.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamPanel extends JPanel implements WebcamListener {

	private static final long serialVersionUID = 5792962512394656227L;

	private static final Logger LOG = LoggerFactory.getLogger(WebcamPanel.class);

	private double frequency = 5; // FPS

	private class Repainter extends Thread {

		public Repainter() {
			setDaemon(true);
		}

		@Override
		public void run() {

			while (starting) {
				repaint();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					LOG.error("Nasty interrupted exception");
				}
			}

			if (!webcam.isOpen()) {
				webcam.open();
			}

			while (webcam.isOpen()) {

				image = webcam.getImage();
				if (image == null) {
					LOG.error("Image is null");
				}

				try {
					if (paused) {
						synchronized (this) {
							this.wait();
						}
					}

					Thread.sleep((long) (1000 / frequency));

				} catch (InterruptedException e) {
					LOG.error("Nasty interrupted exception");
				}

				repaint();
			}
		}
	}

	private Webcam webcam = null;
	private BufferedImage image = null;
	private Repainter repainter = null;

	private volatile boolean starting = false;

	public WebcamPanel(Webcam webcam, boolean start) {

		this.webcam = webcam;
		this.webcam.addWebcamListener(this);

		if (start) {
			if (!webcam.isOpen()) {
				webcam.open();
			}
		}

		setPreferredSize(webcam.getViewSize());

		repainter = new Repainter();

		if (start) {
			repainter.start();
		}
	}

	public WebcamPanel(Webcam webcam) {
		this(webcam, true);
	}

	@Override
	protected void paintComponent(Graphics g) {

		if (image == null) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setBackground(Color.BLACK);
			g2.fillRect(0, 0, getWidth(), getHeight());

			int cx = (getWidth() - 70) / 2;
			int cy = (getHeight() - 40) / 2;

			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillRoundRect(cx, cy, 70, 40, 10, 10);
			g2.setColor(Color.WHITE);
			g2.fillOval(cx + 5, cy + 5, 30, 30);
			g2.setColor(Color.LIGHT_GRAY);
			g2.fillOval(cx + 10, cy + 10, 20, 20);
			g2.setColor(Color.WHITE);
			g2.fillOval(cx + 12, cy + 12, 16, 16);
			g2.fillRoundRect(cx + 50, cy + 5, 15, 10, 5, 5);
			g2.fillRect(cx + 63, cy + 25, 7, 2);
			g2.fillRect(cx + 63, cy + 28, 7, 2);
			g2.fillRect(cx + 63, cy + 31, 7, 2);

			String str = starting ? "Initializing" : "No Image";
			FontMetrics metrics = g2.getFontMetrics(getFont());
			int w = metrics.stringWidth(str);
			int h = metrics.getHeight();

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			g2.drawString(str, (getWidth() - w) / 2, cy - h / 2);

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.DARK_GRAY);
			g2.setStroke(new BasicStroke(3));
			g2.drawLine(0, 0, getWidth(), getHeight());
			g2.drawLine(0, getHeight(), getWidth(), 0);

			return;
		}

		g.drawImage(image, 0, 0, null);
	}

	@Override
	public void webcamOpen(WebcamEvent we) {
		if (repainter == null) {
			repainter = new Repainter();
			repainter.start();
		}
		setPreferredSize(webcam.getViewSize());
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		if (repainter != null) {
			if (repainter.isAlive()) {
				try {
					repainter.join(1000);
				} catch (InterruptedException e) {
					throw new WebcamException("Thread interrupted", e);
				}
			}
			repainter = null;
		}
	}

	private AtomicBoolean started = new AtomicBoolean(false);

	private volatile boolean paused = false;

	public void start() {
		if (started.compareAndSet(false, true)) {
			starting = true;
			repainter.start();
			webcam.open();
			starting = false;
		}
	}

	public void stop() {
		webcam.close();
	}

	/**
	 * Pause rendering.
	 */
	public void pause() {
		if (paused) {
			return;
		}
		paused = true;
	}

	/**
	 * Resume rendering.
	 */
	public void resume() {
		if (!paused) {
			return;
		}
		paused = false;
	}

	/**
	 * @return Rendering frequency (in FPS).
	 */
	public double getFrequency() {
		return frequency;
	}

	private static final double MIN_FREQUENCY = 0.016; // 1 frame per minute
	private static final double MAX_FREQUENCY = 25; // 25 frames per second

	/**
	 * Set rendering frequency (in Hz or FPS). Minimum frequency is 0.016 (1
	 * frame per minute) and maximum is 25 (25 frames per second).
	 * 
	 * @param frequency the frequency
	 */
	public void setFPS(double frequency) {
		if (frequency > MAX_FREQUENCY) {
			frequency = MAX_FREQUENCY;
		}
		if (frequency < MIN_FREQUENCY) {
			frequency = MIN_FREQUENCY;
		}
		this.frequency = frequency;
	}

}