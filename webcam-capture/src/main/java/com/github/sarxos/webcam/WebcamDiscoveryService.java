package com.github.sarxos.webcam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebcamDiscoveryService implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamDiscoveryService.class);

	private static final class WebcamsDiscovery implements Callable<List<Webcam>>, ThreadFactory {

		private final WebcamDriver driver;

		public WebcamsDiscovery(WebcamDriver driver) {
			this.driver = driver;
		}

		@Override
		public List<Webcam> call() throws Exception {
			return toWebcams(driver.getDevices());
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "webcam-discovery-service");
			t.setDaemon(true);
			t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
			return t;
		}
	}

	private final WebcamDriver driver;
	private final WebcamDiscoverySupport support;

	private volatile List<Webcam> webcams = null;

	private AtomicBoolean running = new AtomicBoolean(false);
	private AtomicBoolean enabled = new AtomicBoolean(true);

	private Thread runner = null;

	protected WebcamDiscoveryService(WebcamDriver driver) {

		if (driver == null) {
			throw new IllegalArgumentException("Driver cannot be null!");
		}

		this.driver = driver;
		this.support = (WebcamDiscoverySupport) (driver instanceof WebcamDiscoverySupport ? driver : null);
	}

	private static List<Webcam> toWebcams(List<WebcamDevice> devices) {
		List<Webcam> webcams = new ArrayList<Webcam>();
		for (WebcamDevice device : devices) {
			webcams.add(new Webcam(device));
		}
		return webcams;
	}

	/**
	 * Get list of devices used by webcams.
	 * 
	 * @return List of webcam devices
	 */
	private static List<WebcamDevice> getDevices(List<Webcam> webcams) {
		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		for (Webcam webcam : webcams) {
			devices.add(webcam.getDevice());
		}
		return devices;
	}

	public List<Webcam> getWebcams(long timeout, TimeUnit tunit) throws TimeoutException {

		if (timeout < 0) {
			throw new IllegalArgumentException("Timeout cannot be negative");
		}

		if (tunit == null) {
			throw new IllegalArgumentException("Time unit cannot be null!");
		}

		List<Webcam> tmp = null;

		synchronized (Webcam.class) {

			if (webcams == null) {

				WebcamsDiscovery discovery = new WebcamsDiscovery(driver);
				ExecutorService executor = Executors.newSingleThreadExecutor(discovery);
				Future<List<Webcam>> future = executor.submit(discovery);

				executor.shutdown();

				try {

					executor.awaitTermination(timeout, tunit);

					if (future.isDone()) {
						webcams = future.get();
					} else {
						future.cancel(true);
					}

				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (ExecutionException e) {
					throw new WebcamException(e);
				}

				if (webcams == null) {
					throw new TimeoutException(String.format("Webcams discovery timeout (%d ms) has been exceeded", timeout));
				}

				tmp = new ArrayList<Webcam>(webcams);

				if (Webcam.isHandleTermSignal()) {
					WebcamDeallocator.store(webcams.toArray(new Webcam[webcams.size()]));
				}
			}
		}

		if (tmp != null) {
			WebcamDiscoveryListener[] listeners = Webcam.getDiscoveryListeners();
			for (Webcam webcam : tmp) {
				notifyWebcamFound(webcam, listeners);
			}
		}

		return Collections.unmodifiableList(webcams);
	}

	/**
	 * Scan for newly added or already removed webcams.
	 */
	public void scan() {

		WebcamDiscoveryListener[] listeners = Webcam.getDiscoveryListeners();

		List<WebcamDevice> tmpnew = driver.getDevices();
		List<WebcamDevice> tmpold = null;

		try {
			tmpold = getDevices(getWebcams(Long.MAX_VALUE, TimeUnit.MILLISECONDS));
		} catch (TimeoutException e) {
			throw new WebcamException(e);
		}

		// convert to linked list due to O(1) on remove operation on
		// iterator versus O(n) for the same operation in array list

		List<WebcamDevice> oldones = new LinkedList<WebcamDevice>(tmpold);
		List<WebcamDevice> newones = new LinkedList<WebcamDevice>(tmpnew);

		Iterator<WebcamDevice> oi = oldones.iterator();
		Iterator<WebcamDevice> ni = null;

		WebcamDevice od = null; // old device
		WebcamDevice nd = null; // new device

		// reduce lists

		while (oi.hasNext()) {

			od = oi.next();
			ni = newones.iterator();

			while (ni.hasNext()) {

				nd = ni.next();

				// remove both elements, if device name is the same, which
				// actually means that device is exactly the same

				if (nd.getName().equals(od.getName())) {
					ni.remove();
					oi.remove();
					break;
				}
			}
		}

		// if any left in old ones it means that devices has been removed
		if (oldones.size() > 0) {

			List<Webcam> notified = new ArrayList<Webcam>();

			for (WebcamDevice device : oldones) {
				for (Webcam webcam : webcams) {
					if (webcam.getDevice().getName().equals(device.getName())) {
						notified.add(webcam);
						break;
					}
				}
			}

			setCurrentWebcams(tmpnew);

			for (Webcam webcam : notified) {
				notifyWebcamGone(webcam, listeners);
				webcam.dispose();
			}
		}

		// if any left in new ones it means that devices has been added
		if (newones.size() > 0) {

			setCurrentWebcams(tmpnew);

			for (WebcamDevice device : newones) {
				for (Webcam webcam : webcams) {
					if (webcam.getDevice().getName().equals(device.getName())) {
						notifyWebcamFound(webcam, listeners);
						break;
					}
				}
			}
		}
	}

	@Override
	public void run() {

		// do not run if driver does not support discovery

		if (support == null) {
			return;
		}
		if (!support.isScanPossible()) {
			return;
		}

		// wait initial time interval since devices has been initially
		// discovered

		Object monitor = new Object();
		do {

			synchronized (monitor) {
				try {
					monitor.wait(support.getScanInterval());
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					throw new RuntimeException("Problem waiting on monitor", e);
				}
			}

			scan();

		} while (running.get());

		LOG.debug("Webcam discovery service loop has been stopped");
	}

	private void setCurrentWebcams(List<WebcamDevice> devices) {
		webcams = toWebcams(devices);
		if (Webcam.isHandleTermSignal()) {
			WebcamDeallocator.unstore();
			WebcamDeallocator.store(webcams.toArray(new Webcam[webcams.size()]));
		}
	}

	private static void notifyWebcamGone(Webcam webcam, WebcamDiscoveryListener[] listeners) {
		WebcamDiscoveryEvent event = new WebcamDiscoveryEvent(webcam, WebcamDiscoveryEvent.REMOVED);
		for (WebcamDiscoveryListener l : listeners) {
			try {
				l.webcamGone(event);
			} catch (Exception e) {
				LOG.error(String.format("Webcam gone, exception when calling listener %s", l.getClass()), e);
			}
		}
	}

	private static void notifyWebcamFound(Webcam webcam, WebcamDiscoveryListener[] listeners) {
		WebcamDiscoveryEvent event = new WebcamDiscoveryEvent(webcam, WebcamDiscoveryEvent.ADDED);
		for (WebcamDiscoveryListener l : listeners) {
			try {
				l.webcamFound(event);
			} catch (Exception e) {
				LOG.error(String.format("Webcam found, exception when calling listener %s", l.getClass()), e);
			}
		}
	}

	/**
	 * Stop discovery service.
	 */
	public void stop() {

		// return if not running

		if (!running.compareAndSet(true, false)) {
			return;
		}

		try {
			runner.join();
		} catch (InterruptedException e) {
			throw new WebcamException("Joint interrupted");
		}

		LOG.debug("Discovery service has been stopped");

		runner = null;
	}

	/**
	 * Start discovery service.
	 */
	public void start() {

		// if configured to not start, then simply return

		if (!enabled.get()) {
			LOG.info("Discovery service has been disabled and thus it will not be started");
			return;
		}

		// capture driver does not support discovery - nothing to do

		if (support == null) {
			LOG.info("Discovery will not run - driver {} does not support this feature", driver.getClass().getSimpleName());
			return;
		}

		// return if already running

		if (!running.compareAndSet(false, true)) {
			return;
		}

		// start discovery service runner

		runner = new Thread(this, "webcam-discovery-service");
		runner.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
		runner.setDaemon(true);
		runner.start();
	}

	/**
	 * Is discovery service running?
	 * 
	 * @return True or false
	 */
	public boolean isRunning() {
		return running.get();
	}

	/**
	 * Webcam discovery service will be automatically started if it's enabled,
	 * otherwise, when set to disabled, it will never start, even when user try
	 * to run it.
	 * 
	 * @param enabled the parameter controlling if discovery shall be started
	 */
	public void setEnabled(boolean enabled) {
		this.enabled.set(enabled);
	}

	/**
	 * Cleanup.
	 */
	protected void shutdown() {

		stop();

		if (webcams == null)
			return;

		// dispose all webcams

		Iterator<Webcam> wi = webcams.iterator();
		while (wi.hasNext()) {
			Webcam webcam = wi.next();
			webcam.dispose();
		}

		synchronized (Webcam.class) {

			// clear webcams list

			webcams.clear();

			// unassign webcams from deallocator

			if (Webcam.isHandleTermSignal()) {
				WebcamDeallocator.unstore();
			}
		}
	}
}
