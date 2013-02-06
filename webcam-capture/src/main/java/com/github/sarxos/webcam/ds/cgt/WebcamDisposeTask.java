package com.github.sarxos.webcam.ds.cgt;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamTask;


/**
 * Dispose webcam device.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamDisposeTask extends WebcamTask {

	public WebcamDisposeTask(Webcam webcam) {
		super(webcam);
	}

	public void dispose() {
		process();
	}

	@Override
	protected void handle() {
		getDevice().dispose();
	}
}
