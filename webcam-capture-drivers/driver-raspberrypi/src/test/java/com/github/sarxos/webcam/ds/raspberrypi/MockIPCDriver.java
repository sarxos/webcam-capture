package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.Map;

import com.github.sarxos.webcam.WebcamDevice;

public class MockIPCDriver extends IPCDriver {
	private final static String[] DEFAULT_ARGUMENTS = {};

	/**
	 * Creates a new instance of TestIPCDriver.
	 * 
	 * @param command
	 */
	public MockIPCDriver() {
		super("cat src/etc/still.txt");
	}

	@Override
	protected String[] getDefaultOptions() {
		return DEFAULT_ARGUMENTS;
	}

	@Override
	protected WebcamDevice createIPCDevice(int camSelect, Map<String, String> parameters) {
		return new MockIPCDevice(camSelect, parameters, this);
	}

	@Override
	protected String getCommand() {
		return "ping localhost"+(System.getProperty("os.name").toLowerCase().contains("windows")?" -t":"");
	}

}
