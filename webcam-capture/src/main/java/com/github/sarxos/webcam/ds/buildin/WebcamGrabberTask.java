package com.github.sarxos.webcam.ds.buildin;

import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public abstract class WebcamGrabberTask {

	protected volatile OpenIMAJGrabber grabber = null;

	protected void process(WebcamGrabberProcessor grabber) {
		grabber.process(this);
	}

	public void setGrabber(OpenIMAJGrabber grabber) {
		this.grabber = grabber;
	}

	protected abstract void handle();
}
