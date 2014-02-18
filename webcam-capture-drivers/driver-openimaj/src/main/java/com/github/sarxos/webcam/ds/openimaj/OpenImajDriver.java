package com.github.sarxos.webcam.ds.openimaj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamTask;


/**
 * This is webcam driver for OpenIMAJ library.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class OpenImajDriver implements WebcamDriver {

	static {
		if (!"true".equals(System.getProperty("webcam.debug"))) {
			System.setProperty("bridj.quiet", "true");
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(OpenImajDriver.class);

	private static class GetDevicesTask extends WebcamTask {

		private volatile List<WebcamDevice> devices = null;

		public GetDevicesTask(WebcamDriver driver) {
			super(driver, null);
		}

		/**
		 * Return camera devices.
		 * 
		 * @param grabber the native grabber to use for search
		 * @return Camera devices.
		 */
		public List<WebcamDevice> getDevices() {
			try {
				process();
			} catch (InterruptedException e) {
				LOG.debug("Interrupted", e);
				return Collections.emptyList();
			}
			return devices;
		}

		@Override
		protected void handle() {
			devices = new ArrayList<WebcamDevice>();
			for (Device device : VideoCapture.getVideoDevices()) {
				devices.add(new OpenImajDevice(device));
			}
		}
	}

	@Override
	public List<WebcamDevice> getDevices() {

		List<WebcamDevice> devices = new GetDevicesTask(this).getDevices();

		if (LOG.isDebugEnabled()) {
			for (WebcamDevice device : devices) {
				LOG.debug("OpenIMAJ found device {}", device.getName());
			}
		}

		return devices;

	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
