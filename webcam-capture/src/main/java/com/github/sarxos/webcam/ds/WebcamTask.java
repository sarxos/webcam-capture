package com.github.sarxos.webcam.ds;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;


public abstract class WebcamTask {

	private boolean sync = true;
	private WebcamProcessor processor = null;
	private WebcamDevice device = null;
	private WebcamException exception = null;

	public WebcamTask(WebcamProcessor processor, WebcamDevice device, boolean sync) {

		if (processor == null) {
			throw new IllegalArgumentException("Processor argument cannot be null!");
		}
		if (device == null) {
			throw new IllegalArgumentException("Device argument cannot be null!");
		}

		this.processor = processor;
		this.device = device;
		this.sync = sync;
	}

	public WebcamDevice getDevice() {
		return device;
	}

	/**
	 * Process task by processor thread.
	 * 
	 * @param processor the processor to be used to process this task
	 */
	public void process() {
		if (sync) {
			processor.process(this);
		} else {
			handle();
		}
	}

	public WebcamException getException() {
		return exception;
	}

	public void setException(WebcamException exception) {
		this.exception = exception;
	}

	protected abstract void handle();
}
