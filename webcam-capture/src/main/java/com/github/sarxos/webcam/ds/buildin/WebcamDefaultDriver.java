package com.github.sarxos.webcam.ds.buildin;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.buildin.cgt.GetDevicesTask;
import com.github.sarxos.webcam.ds.buildin.natives.Device;


/**
 * Default build-in webcam driver based on natives from OpenIMAJ framework. It
 * can be widely used on various systems - Mac OS X, Linux (x86, x64, 32-bit
 * ARM), Windows (win32, win64).
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamDefaultDriver implements WebcamDriver {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamDefaultDriver.class);

	/**
	 * Synchronous video grabber processor.
	 */
	private static final WebcamGrabberProcessor processor = new WebcamGrabberProcessor();

	/**
	 * Task to fetch images list from grabber.
	 */
	private static final GetDevicesTask DEVICES_TASK = new GetDevicesTask(processor);

	@Override
	public List<WebcamDevice> getDevices() {

		LOG.debug("Searching devices");

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		for (Device device : DEVICES_TASK.getDevices()) {
			devices.add(new WebcamDefaultDevice(device));
		}

		if (LOG.isDebugEnabled()) {
			for (WebcamDevice device : devices) {
				LOG.debug("Found device {}", device.getName());
			}
		}

		return devices;
	}
}
