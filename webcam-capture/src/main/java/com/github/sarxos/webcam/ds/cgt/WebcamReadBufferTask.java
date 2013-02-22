package com.github.sarxos.webcam.ds.cgt;

import java.nio.ByteBuffer;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDevice.BufferAccess;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamTask;


public class WebcamReadBufferTask extends WebcamTask {

	private volatile ByteBuffer buffer = null;

	public WebcamReadBufferTask(WebcamDriver driver, WebcamDevice device) {
		super(driver, device);
	}

	public ByteBuffer getBuffer() {
		process();
		return buffer;
	}

	@Override
	protected void handle() {

		WebcamDevice device = getDevice();
		if (!device.isOpen()) {
			return;
		}

		if (!(device instanceof BufferAccess)) {
			return;
		}

		buffer = ((BufferAccess) device).getImageBytes();
	}
}
