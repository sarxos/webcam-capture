package com.github.sarxos.webcam.ds.buildin;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class DefaultDriver implements WebcamDriver {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultDriver.class);

	private static OpenIMAJGrabber grabber = null;
	private static List<WebcamDevice> devices = null;

	@Override
	public List<WebcamDevice> getDevices() {

		if (grabber == null) {
			LOG.debug("Creating grabber");
			grabber = new OpenIMAJGrabber();
		}

		if (devices == null) {

			LOG.debug("Searching devices");

			devices = new ArrayList<WebcamDevice>();

			DeviceList list = grabber.getVideoDevices().get();
			for (Device device : list.asArrayList()) {
				devices.add(new DefaultDevice(device));
			}

			if (LOG.isDebugEnabled()) {
				for (WebcamDevice device : devices) {
					LOG.debug("Found device " + device);
				}
			}
		}

		return devices;
	}

}
