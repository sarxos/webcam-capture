package com.github.sarxos.webcam.ds.cgt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamTask;


public class WebcamOpenTask extends WebcamTask {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamOpenTask.class);

	private Webcam webcam = null;

	public WebcamOpenTask(Webcam webcam) {
		super(webcam);
	}

	public void open(Webcam webcam) {
		this.webcam = webcam;
		process();
	}

	@Override
	protected void handle() {

		WebcamDevice device = getDevice();

		if (device.isOpen()) {
			return;
		}

		if (device.getResolution() == null) {
			device.setResolution(device.getResolutions()[0]);
		}

		LOG.info("Opening webcam {}", device.getName());

		device.open();

		// TODO move back to Webcam

		WebcamEvent we = new WebcamEvent(webcam);
		for (WebcamListener l : webcam.getWebcamListeners()) {
			try {
				l.webcamOpen(we);
			} catch (Exception e) {
				LOG.error(String.format("Notify webcam open, exception when calling listener %s", l.getClass()), e);
			}
		}
	}
}
