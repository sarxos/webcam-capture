package com.github.sarxos.webcam.ds.raspberrypi;

import java.awt.Dimension;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sarxos.webcam.WebcamDevice;

import junit.framework.TestCase;

public class TestDriver extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty(Constants.SYSTEM_PROP_PREFIX+"prop1", "value1");
		System.setProperty(Constants.SYSTEM_PROP_PREFIX+"prop2", "value2");
	}

	public void testIPCDriver() {
		MockIPCDriver driver = new MockIPCDriver();
		assertEquals(driver.isDeviceCalled(), false);
		assertEquals(true, driver.getCommand().startsWith("ping"));
		assertEquals(driver.isThreadSafe(), false);
		
		assertEquals(driver.getOptions().hasOption("width"), true);
		
		List<WebcamDevice> devices=driver.getDevices();
		assertEquals(0, devices.size());

		WebcamDevice device = driver.createIPCDevice(0, new HashMap<String, String>());
		assertEquals(device instanceof MockIPCDevice, true);
	}

	public void testIPCDevice() {
		MockIPCDriver driver = new MockIPCDriver();
		
		Map<String, String> parameters=new HashMap<String, String>();
		parameters.put(Constants.OPT_WIDTH, "320");
		parameters.put(Constants.OPT_HEIGHT, "240");
		
		MockIPCDevice device = new MockIPCDevice(0, parameters, driver);
		assertEquals(device.getName().endsWith("0"), true);
		assertEquals(device.isOpen(), false);
		
		Dimension dim=device.getResolution();
		assertEquals(240, dim.height);
		assertEquals(320, dim.width);
		assertEquals(true, device.getResolutions().length>0);
		
		device.setResolution(new Dimension(640, 480));
		dim=device.getResolution();
		assertEquals(480, dim.height);
		assertEquals(640, dim.width);
		
		assertEquals(640, dim.width);
		assertEquals("640", device.parameters.get("width"));
		assertEquals("480", device.parameters.get("height"));
		
		device.setParameters(Collections.singletonMap("help", ""));
		assertEquals(true, device.parameters.containsKey("help"));
		device.validateParameters();
		assertEquals(false, device.parameters.containsKey("help"));
		
		device.parameters.clear();
		
		device.open();
		device.close();
		device.dispose();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		System.clearProperty(Constants.SYSTEM_PROP_PREFIX+"prop1");
		System.clearProperty(Constants.SYSTEM_PROP_PREFIX+"prop2");
	}
}
