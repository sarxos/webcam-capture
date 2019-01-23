package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.Map;

import com.github.sarxos.webcam.WebcamDevice;

/**
 * ClassName: RaspistillDriver <br/>
 * date: Jan 31, 2019 10:57:58 AM <br/>
 * java wrapper for raspistill
 * 
 * @author maoanapex88@163.com alexmao86
 * @version
 * @since JDK 1.8
 */
public class RaspistillDriver extends IPCDriver {
	private final static String[] DEFAULT_ARGUMENTS = { "--width", "320", "--height", "240", "--quality", "36",
			"--timelapse", "10", "--timeout", "0" };

	/**
	 * Creates a new instance of RaspistillDriver.
	 * 
	 * @param command
	 */
	public RaspistillDriver() {
		super(Constants.COMMAND_RASPISTILL);
	}

	@Override
	protected String[] getDefaultOptions() {
		return DEFAULT_ARGUMENTS;
	}

	@Override
	protected WebcamDevice createIPCDevice(int camSelect, Map<String, String> parameters) {
		return new RaspistillDevice(camSelect, parameters, this);
	}
}
