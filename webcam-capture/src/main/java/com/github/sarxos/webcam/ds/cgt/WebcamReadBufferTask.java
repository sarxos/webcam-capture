package com.github.sarxos.webcam.ds.cgt;

import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.ds.WebcamProcessor;
import com.github.sarxos.webcam.ds.WebcamTask;


public class WebcamReadBufferTask extends WebcamTask {

	private BufferedImage image = null;

	public WebcamReadBufferTask(WebcamProcessor processor, WebcamDevice device, boolean sync) {
		super(processor, device, sync);
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
