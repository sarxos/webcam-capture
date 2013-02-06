package com.github.sarxos.webcam.ds.cgt;

import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamTask;


public class WebcamReadBufferTask extends WebcamTask {

	private BufferedImage image = null;

	public WebcamReadBufferTask(Webcam webcam) {
		super(webcam);
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
