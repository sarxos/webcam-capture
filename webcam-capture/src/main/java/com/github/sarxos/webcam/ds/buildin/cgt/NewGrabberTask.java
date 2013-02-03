package com.github.sarxos.webcam.ds.buildin.cgt;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


/**
 * Internal task used to create new grabber. Yeah, native grabber construction,
 * same as all other methods invoked on its instance, also has to be
 * super-synchronized.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class NewGrabberTask extends WebcamGrabberTask {

	private volatile OpenIMAJGrabber grabber = null;

	public OpenIMAJGrabber getGrabber() {
		return grabber;
	}

	@Override
	protected void handle() {
		grabber = new OpenIMAJGrabber();
	}
}