package com.github.sarxos.webcam.ds.openimaj;

import java.util.ArrayList;
import java.util.List;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.buildin.natives.Device;


/**
 * This is webcam driver for OpenIMAJ library.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class OpenImajDriver implements WebcamDriver {

	/**
	 * Cached webcams list.
	 */
	private static List<WebcamDevice> webcamDevices = null;

	@Override
	public List<WebcamDevice> getDevices() {

		if (webcamDevices == null) {
			webcamDevices = new ArrayList<WebcamDevice>();
			List<Device> devices = VideoCapture.getVideoDevices();
			for (Device device : devices) {
				webcamDevices.add(new OpenImajDevice(device));
			}
		}

		return webcamDevices;
	}

}
