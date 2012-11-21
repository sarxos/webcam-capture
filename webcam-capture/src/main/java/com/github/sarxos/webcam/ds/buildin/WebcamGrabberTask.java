package com.github.sarxos.webcam.ds.buildin;

import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public abstract class WebcamGrabberTask {

	protected void process() {
		WebcamGrabberProcessor.getInstance().process(this);
	}

	protected abstract void handle(OpenIMAJGrabber grabber);
}
