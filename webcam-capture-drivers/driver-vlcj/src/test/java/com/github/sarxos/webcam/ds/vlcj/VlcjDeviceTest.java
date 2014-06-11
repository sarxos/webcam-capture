package com.github.sarxos.webcam.ds.vlcj;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.medialist.MediaListItem;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.discoverer.MediaDiscoverer;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamResolution;


@RunWith(PowerMockRunner.class)
@PrepareForTest({
	MediaList.class,
	MediaDiscoverer.class, //
})
public class VlcjDeviceTest {

	private static class DummyDevice implements WebcamDevice {

		private static final AtomicInteger N = new AtomicInteger();
		private final int number;

		public DummyDevice() {
			this.number = N.incrementAndGet();
		}

		@Override
		public String getName() {
			return "Dummy device " + number;
		}

		@Override
		public Dimension[] getResolutions() {
			return new Dimension[] { WebcamResolution.VGA.getSize() };
		}

		@Override
		public Dimension getResolution() {
			return WebcamResolution.VGA.getSize();
		}

		@Override
		public void setResolution(Dimension size) {
			throw new IllegalStateException("Not supported");
		}

		@Override
		public BufferedImage getImage() {
			return EasyMock.createMock(BufferedImage.class);
		}

		@Override
		public void open() {
			// ignore
		}

		@Override
		public void close() {
			// ignore
		}

		@Override
		public void dispose() {
			// ignore
		}

		@Override
		public boolean isOpen() {
			return true;
		}
	}

	private VlcjDriver getDriverMock() {

		List<MediaListItem> items = new ArrayList<MediaListItem>();
		items.add(EasyMock.createMock(MediaListItem.class));
		items.add(EasyMock.createMock(MediaListItem.class));
		items.add(EasyMock.createMock(MediaListItem.class));
		items.add(EasyMock.createMock(MediaListItem.class));
		for (MediaListItem item : items) {
			EasyMock.replay(item);
		}

		MediaList list = PowerMock.createNiceMock(MediaList.class);
		EasyMock.expect(list.items()).andReturn(items).anyTimes();
		EasyMock.replay(list);

		MediaDiscoverer discoverer = PowerMock.createNiceMock(MediaDiscoverer.class);
		EasyMock.expect(discoverer.getMediaList()).andReturn(list).anyTimes();
		EasyMock.replay(discoverer);

		MediaPlayerFactory factory = EasyMock.createNiceMock(MediaPlayerFactory.class);
		EasyMock.expect(factory.newVideoMediaDiscoverer()).andReturn(discoverer).anyTimes();
		EasyMock.replay(factory);

		VlcjDriver driver = EasyMock.createMockBuilder(VlcjDriver.class)
		.addMockedMethod("createMediaPlayerFactory")
		.addMockedMethod("mediaListItemToDevice")
		.createMock();
		EasyMock.expect(driver.createMediaPlayerFactory()).andReturn(factory).anyTimes();
		for (MediaListItem item : items) {
			EasyMock.expect(driver.mediaListItemToDevice(item)).andReturn(new DummyDevice()).anyTimes();
		}
		EasyMock.replay(driver);

		return driver;
	}

	@Test
	public void test_getDevices() {
		VlcjDriver driver = getDriverMock();
		List<WebcamDevice> devices = driver.getDevices();
		Assert.assertNotNull(devices);
		Assert.assertEquals(4, devices.size());
	}

	@Test
	public void test_isThreadSafe() {
		VlcjDriver driver = getDriverMock();
		Assert.assertFalse(driver.isThreadSafe());
	}

	@Test
	public void test_isScanPossible() {
		VlcjDriver driver = getDriverMock();
		Assert.assertTrue(driver.isScanPossible());
	}

	@Test
	public void test_getSetScanInterval() {
		VlcjDriver driver = getDriverMock();
		Assert.assertEquals(VlcjDriver.DEFAULT_SCAN_INTERVAL, driver.getScanInterval());
		driver.setScanInterval(12345);
		Assert.assertEquals(12345, driver.getScanInterval());
	}

	@Test
	public void test_toString() {
		VlcjDriver driver = getDriverMock();
		Assert.assertNotNull(driver.toString());
	}

}
