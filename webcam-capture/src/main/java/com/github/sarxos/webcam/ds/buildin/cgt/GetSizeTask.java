package com.github.sarxos.webcam.ds.buildin.cgt;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicReference;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class GetSizeTask extends WebcamGrabberTask {

	private AtomicReference<Dimension> dimension = new AtomicReference<Dimension>();

	public Dimension getSize() {
		process();
		return dimension.get();
	}

	@Override
	protected void handle(OpenIMAJGrabber grabber) {
		dimension.set(new Dimension(grabber.getWidth(), grabber.getHeight()));
	}
}
