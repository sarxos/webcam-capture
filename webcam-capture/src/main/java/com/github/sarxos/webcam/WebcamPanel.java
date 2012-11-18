package com.github.sarxos.webcam;

import java.awt.Graphics;
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

	private double frequency = 5; // Hz

	private class Repainter extends Thread {

		public Repainter() {
			setDaemon(true);
		}

		@Override
		public void run() {
			super.run();

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

	public WebcamPanel(Webcam webcam, boolean start) {

		this.webcam = webcam;
		this.webcam.addWebcamListener(this);

		if (!webcam.isOpen()) {
			webcam.open();
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

		super.paintComponent(g);

		if (image == null) {
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
			repainter.start();
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
	 * @return Rendering frequency (in Hz or FPS).
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