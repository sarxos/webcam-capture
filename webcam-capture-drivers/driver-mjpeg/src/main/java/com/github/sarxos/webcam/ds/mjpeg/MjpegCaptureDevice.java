package com.github.sarxos.webcam.ds.mjpeg;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.util.MjpegInputStream;


/**
 * This class abstract virtual device getting images from MJPEG source.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class MjpegCaptureDevice implements WebcamDevice {

	private final URL url;

	private URLConnection connection;
	private MjpegInputStream stream;
	private Dimension size;
	private boolean open = false;

	public MjpegCaptureDevice(final URL url) {
		this.url = url;
	}

	@Override
	public String getName() {
		return url.toString();
	}

	@Override
	public Dimension[] getResolutions() {

		if (size != null) {
			return new Dimension[] { size };
		}

		if (!open) {
			open();
		}

		int attempts = 0;
		do {
			final BufferedImage img = getImage();
			if (img != null) {
				size = new Dimension(img.getWidth(), img.getHeight());
				break;
			}
		} while (attempts++ < 5);

		close();

		return new Dimension[] { size };
	}

	@Override
	public Dimension getResolution() {
		return size;
	}

	@Override
	public void setResolution(Dimension size) {
		// ignore resolution change
	}

	@Override
	public BufferedImage getImage() {
		try {
			return stream.readFrame();
		} catch (IOException e) {
			throw new WebcamException("Cannot get image frame from " + url, e);
		}
	}

	@Override
	public void open() {

		if (open) {
			return;
		}

		try {
			connection = url.openConnection();
		} catch (IOException e) {
			throw new WebcamException("Unable to open connection to " + url, e);
		}
		try {
			stream = new MjpegInputStream(connection.getInputStream());
		} catch (IOException e) {
			throw new WebcamException("Unable to get input stream from connection to " + url, e);
		}

		open = true;
	}

	@Override
	public void close() {

		if (!open) {
			return;
		}

		if (connection != null && connection instanceof Closeable) {
			try {
				((Closeable) connection).close();
			} catch (IOException e) {
				throw new WebcamException("Unable to close connection to " + url, e);
			}
		}

		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				throw new WebcamException("Unable to close stream from connection to " + url, e);
			}
		}

		open = false;
	}

	@Override
	public void dispose() {
		// do nothing, no need to dispose anything here
	}

	@Override
	public boolean isOpen() {
		return open;
	}
}
