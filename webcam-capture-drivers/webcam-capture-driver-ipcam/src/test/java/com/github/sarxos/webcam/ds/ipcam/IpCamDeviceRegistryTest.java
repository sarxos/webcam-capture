package com.github.sarxos.webcam.ds.ipcam;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.sarxos.webcam.Webcam;


public class IpCamDeviceRegistryTest {

	@BeforeClass
	public static void setup() {
		Webcam.setDriver(new IpCamDriver());
	}

	@Before
	public void reset() {
		IpCamDeviceRegistry.unregisterAll();
	}

	@Test
	public void test_register() throws MalformedURLException {

		IpCamDeviceRegistry.register("test 01", "http://p.de/c=1", IpCamMode.PULL);

		Assert.assertEquals(1, Webcam.getWebcams().size());

		IpCamDeviceRegistry.register("test 02", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 03", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 04", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 05", "http://p.de/c=1", IpCamMode.PULL);

		Assert.assertEquals(5, Webcam.getWebcams().size());
	}

	@Test
	public void test_unregister() throws MalformedURLException {

		IpCamDevice d1 = IpCamDeviceRegistry.register("test 01", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDevice d2 = IpCamDeviceRegistry.register("test 02", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDevice d3 = IpCamDeviceRegistry.register("test 03", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDevice d4 = IpCamDeviceRegistry.register("test 04", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDevice d5 = IpCamDeviceRegistry.register("test 05", "http://p.de/c=1", IpCamMode.PULL);

		Assert.assertEquals(5, Webcam.getWebcams().size());

		IpCamDeviceRegistry.unregister(d5); // remove "test 05"
		IpCamDeviceRegistry.unregister(d4); // remove "test 04"
		IpCamDeviceRegistry.unregister(d3); // remove "test 03"
		IpCamDeviceRegistry.unregister(d2); // remove "test 02"
		IpCamDeviceRegistry.unregister(d1); // remove "test 01"

		Assert.assertTrue(Webcam.getWebcams().isEmpty());
	}

	@Test
	public void test_unregisterAll() throws MalformedURLException {

		IpCamDeviceRegistry.register("test 01", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 02", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 03", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 04", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 05", "http://p.de/c=1", IpCamMode.PULL);

		Assert.assertEquals(5, Webcam.getWebcams().size());

		IpCamDeviceRegistry.unregisterAll(); // remove "test 05"

		Assert.assertTrue(Webcam.getWebcams().isEmpty());
	}

	@Test
	public void test_isRegisteredName() throws MalformedURLException {

		IpCamDeviceRegistry.register("test 01", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 02", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 03", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 04", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 05", "http://p.de/c=1", IpCamMode.PULL);

		Assert.assertTrue(IpCamDeviceRegistry.isRegistered("test 04"));
	}

	@Test
	public void test_isRegisteredURL() throws MalformedURLException {

		IpCamDeviceRegistry.register("test 01", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 02", "http://p.be/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 03", "http://p.pl/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 04", "http://p.co/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 05", "http://p.lt/c=1", IpCamMode.PULL);

		Assert.assertTrue(IpCamDeviceRegistry.isRegistered(new URL("http://p.pl/c=1")));
	}

	@Test
	public void test_isRegisteredURI() throws MalformedURLException, URISyntaxException {

		IpCamDeviceRegistry.register("test 01", "http://p.de/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 02", "http://p.be/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 03", "http://p.pl/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 04", "http://p.co/c=1", IpCamMode.PULL);
		IpCamDeviceRegistry.register("test 05", "http://p.lt/c=1", IpCamMode.PULL);

		Assert.assertTrue(IpCamDeviceRegistry.isRegistered(new URI("http://p.pl/c=1")));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_isRegisteredURIIllegal() throws MalformedURLException, URISyntaxException {
		Assert.assertTrue(IpCamDeviceRegistry.isRegistered((URI) null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_isRegisteredURLIllegal() throws MalformedURLException, URISyntaxException {
		Assert.assertTrue(IpCamDeviceRegistry.isRegistered((URL) null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_isRegisteredDeviceIllegal() throws MalformedURLException, URISyntaxException {
		Assert.assertTrue(IpCamDeviceRegistry.isRegistered((IpCamDevice) null));
	}
}
