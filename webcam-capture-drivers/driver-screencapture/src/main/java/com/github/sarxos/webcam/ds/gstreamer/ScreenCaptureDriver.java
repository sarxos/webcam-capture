package com.github.sarxos.webcam.ds.gstreamer;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;


public class ScreenCaptureDriver implements WebcamDriver {

	@Override
	public List<WebcamDevice> getDevices() {

		final GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice[] devices = g.getScreenDevices();

		final List<WebcamDevice> list = new ArrayList<>();
		for (final GraphicsDevice device : devices) {
			list.add(new ScreenCaptureDevice(device));
		}

		return list;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}
}
