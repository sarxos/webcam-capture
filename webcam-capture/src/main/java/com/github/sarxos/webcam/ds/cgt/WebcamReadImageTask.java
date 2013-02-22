package com.github.sarxos.webcam.ds.cgt;

import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamTask;


public class WebcamReadImageTask extends WebcamTask {

	private volatile BufferedImage image = null;

	public WebcamReadImageTask(WebcamDriver driver, WebcamDevice device) {
		super(driver, device);
	}

	public BufferedImage getImage() {
		process();
		return image;
	}

	@Override
	protected void handle() {

		WebcamDevice device = getDevice();
		if (!device.isOpen()) {
			return;
		}

		image = device.getImage();
	}
}
