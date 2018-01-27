package com.github.sarxos.webcam.ds.screencapture;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;


/**
 * This is capture driver which returns a list of {@link ScreenCaptureDevice} instances which can be
 * used by {@link Webcam} to stream images feed from.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ScreenCaptureDriver implements WebcamDriver {

	@Override
	public List<WebcamDevice> getDevices() {

		final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice[] devices = environment.getScreenDevices();

		final List<WebcamDevice> list = new ArrayList<>(devices.length);
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
