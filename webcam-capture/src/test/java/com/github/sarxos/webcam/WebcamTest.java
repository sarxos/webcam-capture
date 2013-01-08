package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.Image;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.sarxos.webcam.ds.test.DummyDriver;
import com.github.sarxos.webcam.ds.test.DummyDriver2;
import com.github.sarxos.webcam.ds.test.DummyDriver3;


/**
 * @author bfiryn
 */
public class WebcamTest {

	@Before
	public void prepare() {
		Webcam.registerDriver(DummyDriver.class);
	}

	@After
	public void cleanup() {
		Webcam.resetDriver();
		Webcam.clearWebcams();
	}

	@Test
	public void test_getWebcams() {

		List<Webcam> webcams = Webcam.getWebcams();
		List<WebcamDevice> devices = DummyDriver.getInstance().getDevices();

		Assert.assertTrue(webcams.size() > 0);
		Assert.assertEquals(devices.size(), webcams.size());
	}

	@Test
	public void test_getDefault() {

		List<Webcam> webcams = Webcam.getWebcams();
		List<WebcamDevice> devices = DummyDriver.getInstance().getDevices();

		Assert.assertNotNull(Webcam.getDefault());
		Assert.assertSame(webcams.get(0), Webcam.getDefault());
		Assert.assertSame(devices.get(0), Webcam.getDefault().getDevice());
	}

	@Test
	public void test_open() {
		Webcam webcam = Webcam.getDefault();
		webcam.open();

		Assert.assertTrue(webcam.isOpen());
		webcam.open();

		Assert.assertTrue(webcam.isOpen());
		webcam = Webcam.getWebcams().get(1);
		webcam.getImage();

		Assert.assertTrue(webcam.isOpen());
	}

	@Test
	public void test_close() {

		Webcam webcam = Webcam.getDefault();
		webcam.open();

		Assert.assertTrue(webcam.isOpen());
		webcam.close();
		Assert.assertFalse(webcam.isOpen());
		webcam.close();
		Assert.assertFalse(webcam.isOpen());
	}

	@Test
	public void test_getImage() {

		Webcam webcam = Webcam.getDefault();
		Image image = webcam.getImage();

		Assert.assertNotNull(image);
	}

	@Test
	public void test_getSizes() {

		Dimension[] sizes = Webcam.getDefault().getViewSizes();

		Assert.assertNotNull(sizes);
		Assert.assertEquals(2, sizes.length);
	}

	@Test
	public void test_setSize() {

		Webcam webcam = Webcam.getDefault();
		Dimension[] sizes = webcam.getViewSizes();
		webcam.setViewSize(sizes[0]);

		Assert.assertNotNull(webcam.getViewSize());
		Assert.assertSame(sizes[0], webcam.getViewSize());
	}

	@Test
	public void test_setDriver() throws InstantiationException {

		Webcam.setDriver(DummyDriver2.class);
		WebcamDriver driver2 = Webcam.getDriver();

		Assert.assertSame(DummyDriver2.class, driver2.getClass());

		WebcamDriver driver3 = new DummyDriver3();
		Webcam.setDriver(driver3);

		Assert.assertSame(driver3, Webcam.getDriver());
	}

	@Test
	public void test_registerDriver() {

		Webcam.resetDriver();
		Webcam.clearWebcams();

		Webcam.registerDriver(DummyDriver.class);
		Webcam.getWebcams();
		WebcamDriver driver = Webcam.getDriver();

		Assert.assertSame(DummyDriver.class, driver.getClass());
	}
}
