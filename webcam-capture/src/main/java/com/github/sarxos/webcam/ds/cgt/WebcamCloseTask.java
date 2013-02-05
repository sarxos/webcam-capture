package com.github.sarxos.webcam.ds.cgt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.ds.WebcamProcessor;
import com.github.sarxos.webcam.ds.WebcamTask;


public class WebcamCloseTask extends WebcamTask {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamCloseTask.class);

	private Webcam webcam = null;

	public WebcamCloseTask(WebcamProcessor processor, WebcamDevice device, boolean sync) {
		super(processor, device, sync);
	}

	public void close(Webcam webcam) {
		this.webcam = webcam;
		process();
	}

	@Override
	protected void handle() {

		WebcamDevice device = getDevice();
		if (!device.isOpen()) {
			return;
		}

		LOG.info("Closing {}", device.getName());

		device.close();

		// TODO move back to Webcam

		WebcamEvent we = new WebcamEvent(webcam);
		for (WebcamListener l : webcam.getWebcamListeners()) {
			try {
				l.webcamClosed(we);
			} catch (Exception e) {
				LOG.error(String.format("Notify webcam closed, exception when calling %s listener", l.getClass()), e);
			}
		}
	}
}
