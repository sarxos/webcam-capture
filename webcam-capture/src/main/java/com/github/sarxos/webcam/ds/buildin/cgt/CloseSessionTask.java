package com.github.sarxos.webcam.ds.buildin.cgt;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberProcessor;
import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;


/**
 * This task is to close grabber session.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class CloseSessionTask extends WebcamGrabberTask {

	/**
	 * Grabber processor.
	 */
	private WebcamGrabberProcessor processor = null;

	/**
	 * Create task closing session of grabber connected with specific device.
	 * 
	 * @param processor
	 */
	public CloseSessionTask(WebcamGrabberProcessor processor) {
		this.processor = processor;
	}

	/**
	 * Method to be called from outside of the processor thread.
	 */
	public void closeSession() {
		process(processor);
	}

	@Override
	protected void handle() {
		grabber.stopSession();
	}
}
