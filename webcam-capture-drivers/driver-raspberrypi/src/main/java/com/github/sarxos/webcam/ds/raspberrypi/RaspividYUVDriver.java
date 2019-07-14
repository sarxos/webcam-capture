package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.Map;

import com.github.sarxos.webcam.WebcamDevice;

/**
 * ClassName: RaspividDriver <br/>
 * Runs camera for specific time, and take h.264 capture at end if requested.
 * java wrapper for raspivid
 * 
 * @author maoanapex88@163.com alexmao86
 * @version
 * @since JDK 1.8
 */
public class RaspividYUVDriver extends IPCDriver {
	private final static String[] DEFAULT_ARGUMENTS = { "--width", "320", "--height", "240", "--framerate", "3",
			"--timeout", "0" };

	/**
	 * Creates a new instance of RaspiYUVDriver.
	 * 
	 * @param command
	 */
	public RaspividYUVDriver() {
		super(Constants.COMMAND_RASPIVIDYUV);
	}

	@Override
	protected String[] getDefaultOptions() {
		return DEFAULT_ARGUMENTS;
	}

	@Override
	protected WebcamDevice createIPCDevice(int camSelect, Map<String, String> parameters) {
		return new RaspividYUVDevice(camSelect, parameters, this);
	}
}
