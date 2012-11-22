package com.github.sarxos.webcam.ds.buildin.cgt;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberProcessor;
import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.Device;


public class GetDevicesTask extends WebcamGrabberTask {

	private AtomicReference<List<Device>> devices = new AtomicReference<List<Device>>();

	private WebcamGrabberProcessor processor = null;

	public GetDevicesTask(WebcamGrabberProcessor processor) {
		this.processor = processor;
	}

	public List<Device> getDevices() {
		process(processor);
		return devices.get();
	}

	@Override
	protected void handle() {
		List<Device> devices = grabber.getVideoDevices().get().asArrayList();
		this.devices.set(devices);
	}
}
