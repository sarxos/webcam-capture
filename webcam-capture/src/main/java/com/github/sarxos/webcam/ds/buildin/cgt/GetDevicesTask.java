package com.github.sarxos.webcam.ds.buildin.cgt;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberProcessor;
import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.Device;


/**
 * Processor task used to discover native webcam devices.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class GetDevicesTask extends WebcamGrabberTask {

	/**
	 * Devices list.
	 */
	private AtomicReference<List<Device>> devices = new AtomicReference<List<Device>>();

	/**
	 * Processor.
	 */
	private WebcamGrabberProcessor processor = null;

	/**
	 * Creates new task used to discover native webcam devices.
	 * 
	 * @param processor
	 */
	public GetDevicesTask(WebcamGrabberProcessor processor) {
		this.processor = processor;
	}

	/**
	 * @return Discovered devices
	 */
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
