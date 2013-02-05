package com.github.sarxos.webcam.ds.cgt;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.ds.WebcamProcessor;
import com.github.sarxos.webcam.ds.WebcamTask;


/**
 * Dispose webcam device.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamDisposeTask extends WebcamTask {

	public WebcamDisposeTask(WebcamProcessor processor, WebcamDevice device, boolean sync) {
		super(processor, device, sync);
	}

	public void dispose() {
		process();
	}

	@Override
	protected void handle() {
		getDevice().dispose();
	}
}
