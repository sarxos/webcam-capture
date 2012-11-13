package com.github.sarxos.webcam.ds.ipcam;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamException;


/**
 * IP camera driver.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class IpCamDriver implements WebcamDriver {

	private static final List<WebcamDevice> DEVICES = new ArrayList<WebcamDevice>();

	@Override
	public List<WebcamDevice> getDevices() {
		return Collections.unmodifiableList(DEVICES);
	}

	public void register(IpCamDevice device) {
		for (WebcamDevice d : DEVICES) {
			String name = device.getName();
			if (d.getName().equals(name)) {
				throw new WebcamException(String.format("Name '%s' is already in use", name));
			}
		}
		DEVICES.add(device);
	}

	public void registerURL(String name, String url, IpCamMode mode) {
		try {
			DEVICES.add(new IpCamDevice(name, new URL(url), mode));
		} catch (MalformedURLException e) {
			throw new WebcamException(String.format("Incorrect URL '%s'", url), e);
		}
	}

}
