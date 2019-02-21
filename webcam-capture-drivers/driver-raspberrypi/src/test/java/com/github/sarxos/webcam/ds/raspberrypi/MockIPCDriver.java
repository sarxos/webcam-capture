package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.Map;

import com.github.sarxos.webcam.WebcamDevice;

public class MockIPCDriver extends IPCDriver {
	/** 
	 * Creates a new instance of TestIPCDriver. 
	 * 
	 * @param command 
	 */ 
	public MockIPCDriver() {
		super("top");
	}
	
	@Override
	protected String[] getDefaultOptions() {
		return new String[] {"-n", "1"};
	}

	@Override
	protected WebcamDevice createIPCDevice(int camSelect, Map<String, String> parameters) {
		return new MockIPCDevice(camSelect, parameters, this);
	}

}
