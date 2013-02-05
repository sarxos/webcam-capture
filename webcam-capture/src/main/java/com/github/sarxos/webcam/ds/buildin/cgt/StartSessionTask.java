package com.github.sarxos.webcam.ds.buildin.cgt;

import java.awt.Dimension;

import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberProcessor;
import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.Device;


public class StartSessionTask extends WebcamGrabberTask {

	private Dimension size = null;
	private Device device = null;
	private volatile boolean started = false;

	private WebcamGrabberProcessor processor = null;

	public StartSessionTask(WebcamGrabberProcessor processor) {
		this.processor = processor;
	}

	public boolean startSession(Dimension size, Device device) {

		this.size = size;
		this.device = device;

		process(processor);

		return started;
	}

	@Override
	protected void handle() {
		started = grabber.startSession(size.width, size.height, 50, Pointer.pointerTo(device));
	}
}
