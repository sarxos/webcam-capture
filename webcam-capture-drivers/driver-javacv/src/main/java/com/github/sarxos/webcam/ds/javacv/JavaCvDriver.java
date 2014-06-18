package com.github.sarxos.webcam.ds.javacv;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bytedeco.javacpp.videoInputLib.videoInput;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.util.NixVideoDevUtils;


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

	private List<WebcamDevice> getDevicesWindows() {
		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		int n = videoInput.listDevices();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				devices.add(new JavaCvDevice(i));
			}
		} else {
			devices = Collections.emptyList();
		}
		return devices;
	}

	private List<WebcamDevice> getDevicesLinux() {
		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		for (File vfile : NixVideoDevUtils.getVideoFiles()) {
			devices.add(new JavaCvDevice(vfile));
		}
		return devices;
	}

	@Override
	public List<WebcamDevice> getDevices() {
		boolean linux = System.getProperty("os.name").toLowerCase().indexOf("linux") != -1;
		if (linux) {
			return getDevicesLinux();
		} else {
			return getDevicesWindows();
		}
	}

	public static void main(String[] args) {
		for (WebcamDevice d : new JavaCvDriver().getDevices()) {
			System.out.println(d);
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
