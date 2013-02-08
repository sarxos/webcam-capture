package com.github.sarxos.webcam.ds.buildin;

import java.util.ArrayList;
import java.util.List;

import org.bridj.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


/**
 * Default build-in webcam driver based on natives from OpenIMAJ framework. It
 * can be widely used on various systems - Mac OS, Linux (x86, x64, ARM),
 * Windows (win32, win64).
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamDefaultDriver implements WebcamDriver, WebcamDiscoverySupport {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamDefaultDriver.class);

	private static OpenIMAJGrabber grabber = null;

	@Override
	public List<WebcamDevice> getDevices() {

		LOG.debug("Searching devices");

		if (grabber == null) {
			grabber = new OpenIMAJGrabber();
		}

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		Pointer<DeviceList> pointer = grabber.getVideoDevices();
		DeviceList list = pointer.get();

		for (Device device : list.asArrayList()) {
			devices.add(new WebcamDefaultDevice(device));
		}

		if (LOG.isDebugEnabled()) {
			for (WebcamDevice device : devices) {
				LOG.debug("Found device {}", device.getName());
			}
		}

		return devices;
	}

	@Override
	public long getScanInterval() {
		return 3000;
	}

	@Override
	public boolean isScanPossible() {
		return true;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}
}
