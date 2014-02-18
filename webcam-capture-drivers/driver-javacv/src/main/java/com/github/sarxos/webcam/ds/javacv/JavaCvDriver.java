package com.github.sarxos.webcam.ds.javacv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.googlecode.javacv.cpp.videoInputLib.videoInput;


/**
 * Webcam driver using JavaCV interface to OpenCV. OpenCV (Open Source Computer
 * Vision Library) is library of programming functions for real time computer
 * vision. JavaCV provides wrappers to commonly used libraries for OpenCV and
 * few others.
 * 
 * UNSTABLE, EXPERIMENTALL STUFF !!!
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class JavaCvDriver implements WebcamDriver {

	private static List<WebcamDevice> devices = null;

	@Override
	public List<WebcamDevice> getDevices() {
		if (devices == null) {
			devices = new ArrayList<WebcamDevice>();
			int n = videoInput.listDevices();
			if (n > 0) {
				for (int i = 0; i < n; i++) {
					devices.add(new JavaCvDevice(i));
				}
			} else {
				devices = Collections.emptyList();
			}
		}
		return devices;
	}

	public static void main(String[] args) {
		new JavaCvDriver().getDevices();

		int n = videoInput.listDevices();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				System.out.println(videoInput.getDeviceName(i));
			}
		}

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
