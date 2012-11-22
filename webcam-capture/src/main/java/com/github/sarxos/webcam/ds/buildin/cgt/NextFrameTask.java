package com.github.sarxos.webcam.ds.buildin.cgt;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberProcessor;
import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;


public class NextFrameTask extends WebcamGrabberTask {

	private WebcamGrabberProcessor processor = null;

	public NextFrameTask(WebcamGrabberProcessor processor) {
		this.processor = processor;
	}

	public void nextFrame() {
		process(processor);
	}

	@Override
	protected void handle() {
		grabber.nextFrame();
	}
}
