package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.HashMap;

import com.github.sarxos.webcam.WebcamDevice;

import junit.framework.TestCase;

public class TestDriver extends TestCase {
	public void testDriver() {
		MockIPCDriver driver = new MockIPCDriver();
		assertEquals(driver.isDeviceCalled(), false);
		assertEquals(driver.getCommand(), "cat src/etc/still.txt");
		assertEquals(driver.isThreadSafe(), false);

		WebcamDevice device = driver.createIPCDevice(0, new HashMap<String, String>());
		assertEquals(device instanceof MockIPCDevice, true);
	}

	public void testDevice() {
		MockIPCDriver driver = new MockIPCDriver();
		MockIPCDevice device = new MockIPCDevice(0, new HashMap<String, String>(), driver);
		assertEquals(device.getName().endsWith("0"), true);
		assertEquals(device.isOpen(), false);
	}
}
