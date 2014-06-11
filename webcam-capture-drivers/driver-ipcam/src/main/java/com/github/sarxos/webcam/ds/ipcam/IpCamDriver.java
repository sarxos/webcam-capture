package com.github.sarxos.webcam.ds.ipcam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamExceptionHandler;


/**
 * IP camera driver.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class IpCamDriver implements WebcamDriver, WebcamDiscoverySupport {

	/**
	 * Thread factory.
	 * 
	 * @author Bartosz Firyn (sarxos)
	 */
	private static class DeviceCheckThreadFactory implements ThreadFactory {

		/**
		 * Next number for created thread.
		 */
		private AtomicInteger number = new AtomicInteger();

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "online-check-" + number.incrementAndGet());
			t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
			t.setDaemon(true);
			return t;
		}
	}

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(IpCamDriver.class);

	/**
	 * Thread factory.
	 */
	private static final ThreadFactory THREAD_FACTORY = new DeviceCheckThreadFactory();

	/**
	 * The callable to query single IP camera device. Callable getter will
	 * return device if it's online or null if it's offline.
	 * 
	 * @author Bartosz Firyn (sarxos)
	 */
	private static class DeviceOnlineCheck implements Callable<IpCamDevice> {

		/**
		 * IP camera device.
		 */
		private final IpCamDevice device;

		private final CountDownLatch latch;

		/**
		 * The callable to query single IP camera device.
		 * 
		 * @param device the device to check online status
		 */
		public DeviceOnlineCheck(IpCamDevice device, CountDownLatch latch) {
			this.device = device;
			this.latch = latch;
		}

		@Override
		public IpCamDevice call() throws Exception {
			try {
				return device.isOnline() ? device : null;
			} finally {
				latch.countDown();
			}
		}
	}

	/**
	 * Discovery scan interval in milliseconds.
	 */
	private volatile long scanInterval = 10000;

	/**
	 * Discovery scan timeout in milliseconds. This is maximum time which
	 * executor will wait for online detection to succeed.
	 */
	private volatile long scanTimeout = 10000;

	/**
	 * Is discovery scanning possible.
	 */
	private volatile boolean scanning = false;

	/**
	 * Execution service.
	 */
	private final ExecutorService executor = Executors.newCachedThreadPool(THREAD_FACTORY);

	public IpCamDriver() {
		this(null, false);
	}

	public IpCamDriver(boolean scanning) {
		this(null, scanning);
	}

	public IpCamDriver(IpCamStorage storage) {
		this(storage, false);
	}

	public IpCamDriver(IpCamStorage storage, boolean scanning) {
		if (storage != null) {
			storage.open();
		}
		this.scanning = scanning;
	}

	@Override
	public List<WebcamDevice> getDevices() {

		// in case when scanning is disabled (by default) this method will
		// return all registered devices

		if (!isScanPossible()) {
			return Collections.unmodifiableList((List<? extends WebcamDevice>) IpCamDeviceRegistry.getIpCameras());
		}

		// if scanning is enabled, this method will first perform HTTP lookup
		// for every IP camera device and only online devices will be returned

		List<IpCamDevice> devices = IpCamDeviceRegistry.getIpCameras();
		CountDownLatch latch = new CountDownLatch(devices.size());
		List<Future<IpCamDevice>> futures = new ArrayList<Future<IpCamDevice>>(devices.size());

		for (IpCamDevice device : devices) {
			futures.add(executor.submit(new DeviceOnlineCheck(device, latch)));
		}

		try {
			if (!latch.await(scanTimeout, TimeUnit.MILLISECONDS)) {
				for (Future<IpCamDevice> future : futures) {
					if (!future.isDone()) {
						future.cancel(true);
					}
				}
			}
		} catch (InterruptedException e1) {
			return null;
		}

		List<IpCamDevice> online = new ArrayList<IpCamDevice>(devices.size());

		for (Future<IpCamDevice> future : futures) {

			IpCamDevice device = null;
			try {
				if ((device = future.get()) != null) {
					online.add(device);
				}
			} catch (InterruptedException e) {
				LOG.debug(e.getMessage(), e);
			} catch (CancellationException e) {
				continue;
			} catch (ExecutionException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		return Collections.unmodifiableList((List<? extends WebcamDevice>) online);
	}

	public void register(IpCamDevice device) {
		IpCamDeviceRegistry.register(device);
	}

	public void unregister(IpCamDevice device) {
		IpCamDeviceRegistry.unregister(device);
	}

	@Override
	public boolean isThreadSafe() {
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
	public long getScanInterval() {
		return scanInterval;
	}

	/**
	 * Set new scan interval. Value must be given in milliseconds and shall not
	 * be negative.
	 * 
	 * @param scanInterval
	 */
	public void setScanInterval(long scanInterval) {
		if (scanInterval > 0) {
			this.scanInterval = scanInterval;
		} else {
			throw new IllegalArgumentException("Scan interval for IP camera cannot be negative");
		}
	}

	@Override
	public boolean isScanPossible() {
		return scanning;
	}

	/**
	 * Set discovery scanning possible.
	 * 
	 * @param scanning
	 */
	public void setScanPossible(boolean scanning) {
		this.scanning = scanning;
	}

	/**
	 * @return Scan timeout in milliseconds
	 */
	public long getScanTimeout() {
		return scanTimeout;
	}

	/**
	 * Set new scan timeout. This value cannot be less than 1000 milliseconds
	 * (which equals 1 second).
	 * 
	 * @param scanTimeout the scan timeout in milliseconds
	 */
	public void setScanTimeout(long scanTimeout) {
		if (scanTimeout < 1000) {
			scanTimeout = 1000;
		}
		this.scanTimeout = scanTimeout;
	}
}
