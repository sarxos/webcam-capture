package com.github.sarxos.webcam.ds.buildin.cgt;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class CloseSessionTask extends WebcamGrabberTask {

	public void closeSession() {
		process();
	}

	@Override
	protected void handle(OpenIMAJGrabber grabber) {
		grabber.stopSession();
	}
}
