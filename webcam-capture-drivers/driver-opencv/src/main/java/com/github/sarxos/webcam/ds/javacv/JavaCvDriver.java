package com.github.sarxos.webcam.ds.javacv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.videoinput.videoInput;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.util.NixVideoDevUtils;
import com.github.sarxos.webcam.util.OsUtils;


/**
 * Webcam driver using JavaCV interface to OpenCV. OpenCV (Open Source Computer Vision Library) is
 * library of programming functions for real time computer vision. JavaCV provides wrappers to
 * commonly used libraries for OpenCV and few others.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class JavaCvDriver implements WebcamDriver {

	private List<WebcamDevice> getDevicesWindows() {
		final List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		int n = videoInput.listDevices();
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				devices.add(new JavaCvDevice(i));
			}
		}
		return devices;
	}

	private List<WebcamDevice> getDevicesLinux() {
		File[] videoFiles = NixVideoDevUtils.getVideoFiles();

		final List<WebcamDevice> devices = new ArrayList<>(videoFiles.length);
		for (File vfile : videoFiles) {
			/*
			Filter out video devies that only provide metadata.
			See also https://unix.stackexchange.com/questions/512759/multiple-dev-video-for-one-physical-device
			 */
			try(VideoCapture capture = new VideoCapture()) {
				if(capture.open(vfile.getAbsolutePath())) {
					devices.add(new JavaCvDevice(vfile));
					capture.close();
				}
			}
		}
		return devices;
	}

	private List<WebcamDevice> getDevicesMacOs() {
		final List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		for (int i = 0; i < 100; i++) {
			VideoCapture vc = null;
			try {
				vc = new VideoCapture();
				vc.open(i);
				if (!vc.isOpened()) {
					break;
				} else {
					devices.add(new JavaCvDevice(i));
				}
			} finally {
				if (vc != null) {
					vc.close();
				}
			}
		}
		return devices;
	}

	@Override
	public List<WebcamDevice> getDevices() {
		switch (OsUtils.getOS()) {
			case WIN:
				return getDevicesWindows();
			case NIX:
				return getDevicesLinux();
			case OSX:
				return getDevicesMacOs();
			default:
				throw new WebcamException("Operating system not supported");
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
