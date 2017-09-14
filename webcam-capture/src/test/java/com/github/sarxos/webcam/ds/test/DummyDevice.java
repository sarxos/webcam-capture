package com.github.sarxos.webcam.ds.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;


public class DummyDevice implements WebcamDevice {

	private static final AtomicInteger INSTANCE_NUM = new AtomicInteger(0);
	private static final Dimension[] DIMENSIONS = new Dimension[] {
		new Dimension(300, 200),
		new Dimension(400, 300),
	};

	private String name = DummyDevice.class.getSimpleName() + "-" + INSTANCE_NUM.incrementAndGet();
	private Dimension size = DIMENSIONS[0];
	private boolean open = false;

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
		return size;
	}

	@Override
	public void setResolution(Dimension size) {
		this.size = size;
	}

	private int mx = 1;
	private int my = 1;
	private int r = 10;
	private int x = r;
	private int y = r;

	@Override
	public BufferedImage getImage() {

		if (!open) {
			throw new WebcamException("Not open");
		}

		final BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = bi.createGraphics();
		g2.setColor(Color.RED);
		g2.fillRect(0, 0, size.width, size.height);
		g2.setColor(Color.BLACK);
		g2.drawString(getName(), 10, 20);
		g2.setColor(Color.WHITE);
		g2.drawOval(x += mx, y += my, r, r);
		g2.dispose();
		bi.flush();

		if (x <= 0 + r || x >= size.width - r) {
			mx = -mx;
		}
		if (y <= 0 + r || y >= size.height - r) {
			my = -my;
		}

		return bi;
	}

	@Override
	public void open() {
		open = true;
	}

	@Override
	public void close() {
		open = false;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void dispose() {
		// do nothing
	}
}
