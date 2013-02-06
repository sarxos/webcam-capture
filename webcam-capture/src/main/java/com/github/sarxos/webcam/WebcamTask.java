package com.github.sarxos.webcam;

public abstract class WebcamTask {

	private boolean sync = true;
	private WebcamProcessor processor = null;
	private WebcamDevice device = null;
	private WebcamException exception = null;

	public WebcamTask(Webcam webcam) {

		if (webcam == null) {
			throw new IllegalArgumentException("Webcam argument cannot be null!");
		}

		this.sync = !Webcam.getDriver().isThreadSafe();
		this.device = webcam.getDevice();
		this.processor = webcam.getProcessor();
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
			if (processor == null) {
				throw new RuntimeException("Driver should be synchronized, but processor is null");
			}
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
