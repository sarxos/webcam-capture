package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.Map;

import com.github.sarxos.webcam.WebcamDevice;

public class TestIPCDriver extends IPCDriver {
	/** 
	 * Creates a new instance of TestIPCDriver. 
	 * 
	 * @param command 
	 */ 
	public TestIPCDriver(String command) {
		super("top");
	}
	
	@Override
	protected String[] getDefaultOptions() {
		return new String[] {};
	}

	@Override
	protected WebcamDevice createIPCDevice(int camSelect, Map<String, String> parameters) {
		return new TestIPCDevice(camSelect, parameters, this);
	}

}
