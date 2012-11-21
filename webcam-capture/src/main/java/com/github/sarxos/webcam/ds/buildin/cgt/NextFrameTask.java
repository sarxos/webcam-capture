package com.github.sarxos.webcam.ds.buildin.cgt;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class NextFrameTask extends WebcamGrabberTask {

	public void nextFrame() {
		process();
	}

	@Override
	protected void handle(OpenIMAJGrabber grabber) {
		grabber.nextFrame();
	}
}
