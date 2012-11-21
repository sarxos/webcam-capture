package com.github.sarxos.webcam.ds.buildin.cgt;

import java.awt.Dimension;

import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class StartSessionTask extends WebcamGrabberTask {

	private Dimension size = null;
	private Device device = null;
	private volatile boolean started = false;

	public boolean startSession(Dimension size, Device device) {
		this.size = size;
		this.device = device;
		process();
		return started;
	}

	@Override
	protected void handle(OpenIMAJGrabber grabber) {
		started = grabber.startSession(size.width, size.height, 50, Pointer.pointerTo(device));
	}
}
