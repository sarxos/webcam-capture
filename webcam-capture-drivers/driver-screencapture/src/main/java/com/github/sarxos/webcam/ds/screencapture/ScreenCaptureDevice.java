package com.github.sarxos.webcam.ds.screencapture;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;


/**
 * This class abstract screen device (display) which can be used to capture image from.
 *
 * @author Bartosz Firyn (sarxos)
 * @author Hagen Stanek (gubjack)
 */
public class ScreenCaptureDevice implements WebcamDevice {

	private static final Logger LOG = LoggerFactory.getLogger(ScreenCaptureDevice.class);

	private final GraphicsDevice device;
	private final DisplayMode mode;
	private final Dimension resolution;
	private final Dimension[] resolutions;
	private final Robot robot;

	private boolean open = false;
	private Rectangle bounds;

	public ScreenCaptureDevice(final GraphicsDevice device) {

		this.device = device;
		this.mode = device.getDisplayMode();
		this.resolution = new Dimension(mode.getWidth(), mode.getHeight());
		this.resolutions = new Dimension[] { this.resolution };

		try {
			this.robot = new Robot(device);
		} catch (AWTException e) {
			throw new WebcamException("Unable to create robot", e);
		}

		LOG.trace("Screen device {} with resolution {} has been created", getName(), getResolution());
	}

	@Override
	public String getName() {
		return device.getIDstring();
	}

	@Override
	public Dimension[] getResolutions() {
		return resolutions;
	}

	@Override
	public Dimension getResolution() {
		return resolution;
	}

	@Override
	public void setResolution(Dimension size) {
		resolution.setSize(size.getWidth(), size.getHeight());
	}

	@Override
	public BufferedImage getImage() {

		final BufferedImage screen = robot.createScreenCapture(bounds);
		final int width = resolution.width;
		final int height = resolution.height;

		if (screen.getWidth() == width && screen.getHeight() == height) {
			return screen; // No need for adaptation
		}

		final BufferedImage img = new BufferedImage(width, height, screen.getType());
		final Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(screen, 0, 0, width, height, 0, 0, screen.getWidth(), screen.getHeight(), null);
		g.dispose();

		return img;
	}

	@Override
	public void open() {

		if (open) {
			return;
		}

		LOG.debug("Opening screen device {} with resolution {}", getName(), getResolution());

		this.bounds = device
			.getDefaultConfiguration()
			.getBounds();

		open = true;
	}

	@Override
	public void close() {
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
