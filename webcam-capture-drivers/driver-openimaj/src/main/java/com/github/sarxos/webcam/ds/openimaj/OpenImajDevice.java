package com.github.sarxos.webcam.ds.openimaj;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.openimaj.image.ImageUtilities;
import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;


public class OpenImajDevice implements WebcamDevice {

	private static final Logger LOG = LoggerFactory.getLogger(OpenImajDriver.class);

	/**
	 * Artificial view sizes. I'm really not sure if will fit into other webcams
	 * but hope that OpenIMAJ can handle this.
	 */
	private final static Dimension[] DIMENSIONS = new Dimension[] {
		new Dimension(176, 144),
		new Dimension(320, 240),
		new Dimension(352, 288),
		new Dimension(640, 400),
		new Dimension(640, 480),
		new Dimension(1280, 720),
	};

	private Device device = null;
	private VideoCapture capture = null;
	private Dimension size = null;

	private volatile boolean open = false;
	private volatile boolean disposed = false;

	public OpenImajDevice(Device device) {
		this.device = device;
	}

	@Override
	public String getName() {
		return device.getNameStr();
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
		if (open) {
			throw new RuntimeException("Cannot set new size when device is open, please close it first");
		}
		this.size = size;
	}

	@Override
	public BufferedImage getImage() {

		if (!open) {
			throw new RuntimeException("Cannot get image from closed device");
		}

		// TODO scale to dimension if not equal
		return ImageUtilities.createBufferedImageForDisplay(capture.getNextFrame());
	}

	@Override
	public void open() {

		if (disposed) {
			LOG.warn("Cannot open device because it's already disposed");
			return;
		}

		if (open) {
			return;
		}

		try {
			capture = new VideoCapture(size.width, size.height, device);
		} catch (VideoCaptureException e) {
			throw new WebcamException("Cannot initialize video capture", e);
		}
		open = true;

		// what the hell is that something below? that's ugly w/a for black
		// images at the very capture beginning, if you have some other idea of
		// how to remove them, please share or fix

		int i = 0;
		do {
			capture.getNextFrame();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (i++ < 3);

		LOG.info("OpenIMAJ webcam device has been initialized");
	}

	@Override
	public void close() {

		if (!open) {
			return;
		}

		capture.stopCapture();

		open = false;

		LOG.info("OpenIMAJ webcam device has been closed");
	}

	@Override
	public void dispose() {
		disposed = true;
	}

	@Override
	public boolean isOpen() {
		return open;
	}
}
