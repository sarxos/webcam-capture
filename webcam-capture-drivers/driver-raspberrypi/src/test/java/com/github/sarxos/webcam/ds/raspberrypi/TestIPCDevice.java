package com.github.sarxos.webcam.ds.raspberrypi;

import java.awt.image.BufferedImage;
import java.util.Map;

public class TestIPCDevice extends IPCDevice {
	
	/** 
	 * Creates a new instance of TestIPCDevice. 
	 * 
	 * @param camSelect
	 * @param parameters
	 * @param driver 
	 */ 
	public TestIPCDevice(int camSelect, Map<String, String> parameters, IPCDriver driver) {
		super(camSelect, parameters, driver);
	}

	@Override
	public BufferedImage getImage() {
		return null;
	}

}
