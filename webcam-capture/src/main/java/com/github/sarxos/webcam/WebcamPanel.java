package com.github.sarxos.webcam;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


/**
 * Simply implementation of JPanel allowing users to render pictures taken with
 * webcam.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamPanel extends JPanel implements WebcamListener {

	private static final long serialVersionUID = 5792962512394656227L;

	private int frequency = 65; // Hz

	private class Repainter extends Thread {

		public Repainter() {
			setDaemon(true);

		}

		@Override
		public void run() {
			super.run();

			while (webcam.isOpen()) {

				image = webcam.getImage();

				try {
					if (paused) {
						synchronized (this) {
							this.wait();
						}
					}
					Thread.sleep(1000 / frequency);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				repaint();
			}
		}
	}

	private Webcam webcam = null;
	private BufferedImage image = null;
	private Repainter repainter = null;

	public WebcamPanel(Webcam webcam) {
		this.webcam = webcam;
		this.webcam.addWebcamListener(this);

		this.repainter = new Repainter();

		if (webcam.isOpen()) {
			setPreferredSize(webcam.getViewSize());
			repainter.start();
		}
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
		}
		repainter.start();
		setPreferredSize(webcam.getViewSize());
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		try {
			repainter.join();
			repainter = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private volatile boolean paused = false;

	/**
	 * Pause rendering.
	 */
	public void pause() {
		if (paused) {
			return;
		}
		paused = true;
		System.out.println("paused");
	}

	/**
	 * Resume rendering.
	 */
	public void resume() {
		if (!paused) {
			return;
		}
		synchronized (repainter) {
			repainter.notifyAll();
		}
		paused = false;
		System.out.println("resumed");
	}

	/**
	 * @return Rendering frequency (in Hz or FPS).
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Set rendering frequency (in Hz or FPS). Min is 1 and max is 100.
	 * 
	 * @param frequency
	 */
	public void setFrequency(int frequency) {
		if (frequency > 100) {
			frequency = 100;
		}
		if (frequency < 1) {
			frequency = 1;
		}
		this.frequency = frequency;
	}

}