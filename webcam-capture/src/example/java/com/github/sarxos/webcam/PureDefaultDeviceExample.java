package com.github.sarxos.webcam;

import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class PureDefaultDeviceExample {

	public static void main(String[] args) {

		/**
		 * This example show how to use native OpenIMAJ API to capture raw bytes
		 * data as byte[] array. It also calculates current FPS.
		 */

		OpenIMAJGrabber grabber = new OpenIMAJGrabber();

		Device device = null;
		Pointer<DeviceList> devices = grabber.getVideoDevices();
		for (Device d : devices.get().asArrayList()) {
			device = d;
			break;
		}

		grabber.startSession(320, 240, 30, Pointer.pointerTo(device));

		long t1 = System.currentTimeMillis() / 1000;

		int n = 100;
		int i = 0;
		do {
			grabber.nextFrame();
			grabber.getImage().getBytes(320 * 240 * 3); // byte[]
		} while (++i < n);

		long t2 = System.currentTimeMillis() / 1000;

		System.out.println("FPS: " + ((double) n / (t2 - t1)));

		grabber.stopSession();
	}
}
