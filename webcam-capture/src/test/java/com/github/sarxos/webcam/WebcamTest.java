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
		System.out.println(Thread.currentThread().getName() + ": Register dummy driver");
		Webcam.registerDriver(DummyDriver.class);
	}

	@After
	public void cleanup() {
		System.out.println(Thread.currentThread().getName() + ": Reset driver");
		for (Webcam webcam : Webcam.getWebcams()) {
			webcam.close();
		}
		Webcam.resetDriver();
	}

	@Test
	public void test_getWebcams() {

		System.out.println(Thread.currentThread().getName() + ": test_getWebcams() start");

		List<Webcam> webcams = Webcam.getWebcams();
		List<WebcamDevice> devices = DummyDriver.getInstance().getDevices();

		Assert.assertTrue(webcams.size() > 0);
		Assert.assertEquals(devices.size(), webcams.size());

		System.out.println(Thread.currentThread().getName() + ": test_getWebcams() end");
	}

	@Test
	public void test_getDefault() {

		System.out.println(Thread.currentThread().getName() + ": test_getDefault() start");

		List<Webcam> webcams = Webcam.getWebcams();
		List<WebcamDevice> devices = DummyDriver.getInstance().getDevices();

		Assert.assertNotNull(Webcam.getDefault());
		Assert.assertSame(webcams.get(0), Webcam.getDefault());
		Assert.assertSame(devices.get(0), Webcam.getDefault().getDevice());

		System.out.println(Thread.currentThread().getName() + ": test_getDefault() end");
	}

	@Test
	public void test_open() {

		System.out.println(Thread.currentThread().getName() + ": test_open() start");

		Webcam webcam = Webcam.getDefault();
		webcam.open();

		Assert.assertTrue(webcam.isOpen());
		webcam.open();
		Assert.assertTrue(webcam.isOpen());

		System.out.println(Thread.currentThread().getName() + ": test_open() end");
	}

	@Test
	public void test_close() {

		System.out.println(Thread.currentThread().getName() + ": test_close() start");

		Webcam webcam = Webcam.getDefault();
		webcam.open();

		Assert.assertSame(DummyDriver.class, Webcam.getDriver().getClass());

		Assert.assertTrue(webcam.isOpen());
		webcam.close();
		Assert.assertFalse(webcam.isOpen());
		webcam.close();
		Assert.assertFalse(webcam.isOpen());

		System.out.println(Thread.currentThread().getName() + ": test_close() end");
	}

	@Test
	public void test_getImage() {

		System.out.println(Thread.currentThread().getName() + ": test_getImage() start");

		Webcam webcam = Webcam.getDefault();
		webcam.open();

		Assert.assertSame(DummyDriver.class, Webcam.getDriver().getClass());

		Image image = webcam.getImage();

		Assert.assertNotNull(image);

		System.out.println(Thread.currentThread().getName() + ": test_getImage() end");
	}

	@Test
	public void test_getSizes() {

		System.out.println(Thread.currentThread().getName() + ": test_getSizes() start");

		Dimension[] sizes = Webcam.getDefault().getViewSizes();

		Assert.assertSame(DummyDriver.class, Webcam.getDriver().getClass());

		Assert.assertNotNull(sizes);
		Assert.assertEquals(2, sizes.length);

		System.out.println(Thread.currentThread().getName() + ": test_getSizes() end");
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

		Webcam.registerDriver(DummyDriver.class);
		Webcam.getWebcams();
		WebcamDriver driver = Webcam.getDriver();

		Assert.assertSame(DummyDriver.class, driver.getClass());
	}
}
