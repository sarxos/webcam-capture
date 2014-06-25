package com.github.sarxos.webcam.ds.dummy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;


/**
 * Just a dummy device to be used for test purpose.
 * 
 * @author Bartosz Firyn (sarxos)
 **/
public class WebcamDummyDevice implements WebcamDevice {

	private final static Dimension[] DIMENSIONS = new Dimension[] {
		WebcamResolution.QQVGA.getSize(),
		WebcamResolution.QVGA.getSize(),
		WebcamResolution.VGA.getSize(),
	};

	private AtomicBoolean open = new AtomicBoolean(false);
	private Dimension resolution = DIMENSIONS[0];

	private final String name;

	public WebcamDummyDevice(int number) {
		this.name = "Dummy Webcam " + number;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Dimension[] getResolutions() {
		return DIMENSIONS;
	}

	@Override
	public Dimension getResolution() {
		return resolution;
	}

	@Override
	public void setResolution(Dimension size) {
		this.resolution = size;
	}

	byte r = (byte) (Math.random() * Byte.MAX_VALUE);
	byte g = (byte) (Math.random() * Byte.MAX_VALUE);
	byte b = (byte) (Math.random() * Byte.MAX_VALUE);

	private void drawRect(Graphics2D g2, int w, int h) {

		int rx = (int) (w * Math.random() / 1.5);
		int ry = (int) (h * Math.random() / 1.5);
		int rw = (int) (w * Math.random() / 1.5);
		int rh = (int) (w * Math.random() / 1.5);

		g2.setColor(new Color((int) (Integer.MAX_VALUE * Math.random())));
		g2.fillRect(rx, ry, rw, rh);
	}

	@Override
	public BufferedImage getImage() {

		if (!isOpen()) {
			throw new WebcamException("Webcam is not open");
		}

		try {
			Thread.sleep(1000 / 30);
		} catch (InterruptedException e) {
			return null;
		}

		Dimension resolution = getResolution();

		int w = resolution.width;
		int h = resolution.height;

		String s = getName();

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage bi = gc.createCompatibleImage(w, h);

		Graphics2D g2 = ge.createGraphics(bi);
		g2.setBackground(new Color(Math.abs(r++), Math.abs(g++), Math.abs(b++)));
		g2.clearRect(0, 0, w, h);

		drawRect(g2, w, h);
		drawRect(g2, w, h);
		drawRect(g2, w, h);
		drawRect(g2, w, h);
		drawRect(g2, w, h);

		Font font = new Font("sans-serif", Font.BOLD, 16);

		g2.setFont(font);

		FontMetrics metrics = g2.getFontMetrics(font);
		int sw = (w - metrics.stringWidth(s)) / 2;
		int sh = (h - metrics.getHeight()) / 2 + metrics.getHeight() / 2;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.BLACK);
		g2.drawString(s, sw + 1, sh + 1);
		g2.setColor(Color.WHITE);
		g2.drawString(s, sw, sh);

		g2.dispose();
		bi.flush();

		return bi;
	}

	@Override
	public void open() {
		if (open.compareAndSet(false, true)) {
			// ...
		}
	}

	@Override
	public void close() {
		if (open.compareAndSet(true, false)) {
			// ...
		}
	}

	@Override
	public void dispose() {
		close();
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}
}
