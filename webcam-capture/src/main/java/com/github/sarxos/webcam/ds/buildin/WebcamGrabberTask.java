package com.github.sarxos.webcam.ds.buildin;

import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


/**
 * Abstract webcam processor class.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public abstract class WebcamGrabberTask {

	/**
	 * Native grabber.
	 */
	protected volatile OpenIMAJGrabber grabber = null;

	/**
	 * ne = true; Process task by processor thread.
	 * 
	 * @param processor the processor to be used to process this task
	 */
	protected void process(WebcamGrabberProcessor processor) {
		processor.process(this);
	}

	/**
	 * Set grabber connected with specific device.
	 * 
	 * @param grabber the grabber to be set
	 */
	public void setGrabber(OpenIMAJGrabber grabber) {
		this.grabber = grabber;
	}

	/**
	 * Method to be called from inside of the processor thread.
	 */
	protected abstract void handle();
}
