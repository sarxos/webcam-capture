package com.github.sarxos.webcam;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.sarxos.webcam.ds.test.DummyDriver;


/**
 * @author bfiryn
 */
public class WebcamTest {

	@BeforeClass
	public static void setUp() {
		Webcam.setDriver(DummyDriver.class);
	}

	@Test
	public void test_getDevices() {

		List<Webcam> webcams = Webcam.getWebcams();
		List<WebcamDevice> devices = DummyDriver.getInstance().getDevices();

		Assert.assertEquals(devices.size(), webcams.size());
	}

	// @Test
	// public void test_setDriver() {
	//
	// }

}
