package com.github.sarxos.webcam.ds.buildin.cgt;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberProcessor;
import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;


public class CloseSessionTask extends WebcamGrabberTask {

	private WebcamGrabberProcessor processor = null;

	public CloseSessionTask(WebcamGrabberProcessor processor) {
		this.processor = processor;
	}

	public void closeSession() {
		process(processor);
	}

	@Override
	protected void handle() {
		grabber.stopSession();
	}
}
