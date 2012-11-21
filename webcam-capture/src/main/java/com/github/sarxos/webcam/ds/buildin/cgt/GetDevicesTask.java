package com.github.sarxos.webcam.ds.buildin.cgt;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class GetDevicesTask extends WebcamGrabberTask {

	private AtomicReference<List<Device>> devices = new AtomicReference<List<Device>>();

	public List<Device> getDevices() {
		process();
		return devices.get();
	}

	@Override
	protected void handle(OpenIMAJGrabber grabber) {
		this.devices.set(grabber.getVideoDevices().get().asArrayList());
	}
}
