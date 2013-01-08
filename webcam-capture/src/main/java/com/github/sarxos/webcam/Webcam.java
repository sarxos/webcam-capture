package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDevice;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;


/**
 * Webcam class.
 * 
 * @author Bartosz Firyn (bfiryn)
 */
public class Webcam {

	private static final Logger LOG = LoggerFactory.getLogger(Webcam.class);

	// @formatter:off
	private static final String[] DRIVERS_DEFAULT = new String[] {
		"com.github.sarxos.webcam.ds.openimaj.OpenImajDriver",
		"com.github.sarxos.webcam.ds.civil.LtiCivilDriver",
		"com.github.sarxos.webcam.ds.jmf.JmfDriver",
	};
	// @formatter:on

	private static final List<String> DRIVERS_LIST = new ArrayList<String>(Arrays.asList(DRIVERS_DEFAULT));
	private static final List<Class<?>> DRIVERS_CLASS_LIST = new ArrayList<Class<?>>();

	private static final class ShutdownHook extends Thread {

		private Webcam webcam = null;

		public ShutdownHook(Webcam webcam) {
			this.webcam = webcam;
		}

		@Override
		public void run() {
			LOG.info("Automatic resource deallocation");
			super.run();
			webcam.dispose();
			webcam.close0();
		}
	}

	private static final class WebcamsDiscovery implements Callable<List<Webcam>>, ThreadFactory {

		private final WebcamDriver driver;

		public WebcamsDiscovery(WebcamDriver driver) {
			this.driver = driver;
		}

		@Override
		public List<Webcam> call() throws Exception {
			List<Webcam> webcams = new ArrayList<Webcam>();
			for (WebcamDevice device : driver.getDevices()) {
				webcams.add(new Webcam(device));
			}
			return webcams;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "webcam-discovery");
			t.setDaemon(true);
			return t;
		}
	}

	/**
	 * Timeout for devices discovery. By default this is set to 1 minute, but
	 * can be changed by appropriate static setter.
	 * 
	 * @see Webcam#setDiscoveryTimeout(long)
	 */
	private static long timeout = 60000;

	private static WebcamDriver driver = null;
	private static List<Webcam> webcams = null;

	/**
	 * Is automated deallocation on TERM signal enabled.
	 */
	private static volatile boolean deallocOnTermSignal = false;

	/**
	 * Webcam listeners.
	 */
	private List<WebcamListener> listeners = new ArrayList<WebcamListener>();

	private List<Dimension> customSizes = new ArrayList<Dimension>();

	private ShutdownHook hook = null;
	private WebcamDevice device = null;

	private volatile boolean open = false;
	private volatile boolean disposed = false;

	/**
	 * Webcam class.
	 * 
	 * @param device - device to be used as webcam
	 */
	protected Webcam(WebcamDevice device) {
		if (device == null) {
			throw new IllegalArgumentException("Webcam device cannot be null");
		}
		this.device = device;
	}

	/**
	 * Check if size is set up.
	 */
	private void ensureSize() {

		Dimension size = device.getSize();
		if (size == null) {

			Dimension[] sizes = device.getSizes();

			if (sizes == null) {
				throw new WebcamException("Sizes array from driver cannot be null!");
			}
			if (sizes.length == 0) {
				throw new WebcamException("Sizes array from driver is empty, cannot choose image size");
			}

			device.setSize(sizes[0]);
		}
	}

	/**
	 * Open webcam.
	 */
	public synchronized void open() {

		if (open) {
			return;
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Opening webcam " + getName());
		}

		ensureSize();

		device.open();
		open = true;

		hook = new ShutdownHook(this);

		Runtime.getRuntime().addShutdownHook(hook);

		WebcamEvent we = new WebcamEvent(this);
		for (WebcamListener l : listeners) {
			try {
				l.webcamOpen(we);
			} catch (Exception e) {
				LOG.error(String.format("Notify webcam open, exception when calling %s listener", l.getClass()), e);
			}
		}
	}

	/**
	 * Close webcam.
	 */
	public synchronized void close() {

		if (!open) {
			return;
		}

		Runtime.getRuntime().removeShutdownHook(hook);

		close0();
	}

	/**
	 * Close webcam (internal impl).
	 */
	private void close0() {

		if (!open) {
			return;
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Closing {}", getName());
		}

		device.close();
		open = false;

		WebcamEvent we = new WebcamEvent(this);
		for (WebcamListener l : listeners) {
			try {
				l.webcamClosed(we);
			} catch (Exception e) {
				LOG.error(String.format("Notify webcam closed, exception when calling %s listener", l.getClass()), e);
			}
		}
	}

	/**
	 * Is webcam open?
	 * 
	 * @return true if open, false otherwise
	 */
	public synchronized boolean isOpen() {
		return open;
	}

	/**
	 * @return Webcam view size (picture size) in pixels.
	 */
	public Dimension getViewSize() {
		return device.getSize();
	}

	/**
	 * Return list of supported view sizes. It can differ between vary webcam
	 * data sources.
	 * 
	 * @return
	 */
	public Dimension[] getViewSizes() {
		return device.getSizes();
	}

	/**
	 * Set custom resolution. If you are using this method you have to make sure
	 * that your webcam device can support this specific resolution.
	 * 
	 * @param sizes the array of custom resolutions to be supported by webcam
	 */
	public void setCustomViewSizes(Dimension[] sizes) {
		if (sizes == null) {
			customSizes.clear();
			return;
		}
		customSizes = Arrays.asList(sizes);
	}

	public Dimension[] getCustomViewSizes() {
		return customSizes.toArray(new Dimension[customSizes.size()]);
	}

	/**
	 * Set new view size. New size has to exactly the same as one of the default
	 * sized or exactly the same as one of the custom ones.
	 * 
	 * @param size the new view size to be set
	 * @see Webcam#setCustomViewSizes(Dimension[])
	 * @see Webcam#getViewSizes()
	 */
	public void setViewSize(Dimension size) {

		if (size == null) {
			throw new IllegalArgumentException("View size cannot be null!");
		}

		// check if dimension is valid

		Dimension[] predefined = getViewSizes();
		Dimension[] custom = getCustomViewSizes();

		boolean ok = false;
		for (Dimension d : predefined) {
			if (d.width == size.width && d.height == size.height) {
				ok = true;
				break;
			}
		}
		if (!ok) {
			for (Dimension d : custom) {
				if (d.width == size.width && d.height == size.height) {
					ok = true;
					break;
				}
			}
		}

		if (!ok) {
			StringBuilder sb = new StringBuilder("Incorrect dimension [");
			sb.append(size.width).append("x").append(size.height).append("] ");
			sb.append("possible ones are ");
			for (Dimension d : predefined) {
				sb.append("[").append(d.width).append("x").append(d.height).append("] ");
			}
			for (Dimension d : custom) {
				sb.append("[").append(d.width).append("x").append(d.height).append("] ");
			}
			throw new IllegalArgumentException(sb.toString());
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Setting new view size {} x {}", size.width, size.height);
		}

		device.setSize(size);
	}

	/**
	 * Capture image from webcam.
	 * 
	 * @return Captured image
	 */
	public BufferedImage getImage() {

		if (disposed) {
			return null;
		}

		synchronized (this) {
			if (!open) {
				LOG.debug("Try to get image on closed webcam, opening it automatically");
				open();
			}
			return device.getImage();
		}
	}

	/**
	 * Get list of webcams to use.
	 * 
	 * @return List of webcams
	 * @throws WebcamException when something is wrong
	 */
	public static List<Webcam> getWebcams() {
		try {
			return getWebcams(timeout);
		} catch (TimeoutException e) {
			throw new WebcamException(e);
		}
	}

	/**
	 * Get list of webcams to use.
	 * 
	 * @param timeout the devices discovery timeout
	 * @return List of webcams
	 * @throws TimeoutException when timeout has been exceeded
	 * @throws WebcamException when something is wrong
	 */
	public static List<Webcam> getWebcams(long timeout) throws TimeoutException {

		if (webcams == null) {

			if (driver == null) {
				driver = WebcamDriverUtils.findDriver(DRIVERS_LIST, DRIVERS_CLASS_LIST);
			}
			if (driver == null) {
				LOG.info("Webcam driver has not been found, default one will be used!");
				driver = new WebcamDefaultDriver();
			}

			WebcamsDiscovery discovery = new WebcamsDiscovery(driver);
			ExecutorService executor = Executors.newSingleThreadExecutor(discovery);
			Future<List<Webcam>> future = executor.submit(discovery);

			executor.shutdown();

			try {

				executor.awaitTermination(timeout, TimeUnit.MILLISECONDS);

				if (future.isDone()) {
					webcams = future.get();
				} else {
					future.cancel(true);
				}

			} catch (InterruptedException e) {
				throw new WebcamException(e);
			} catch (ExecutionException e) {
				throw new WebcamException(e);
			}

			if (webcams == null) {
				throw new TimeoutException(String.format("Webcams discovery timeout (%d ms) has been exceeded", timeout));
			}

			if (deallocOnTermSignal) {
				LOG.warn("Automated deallocation on TERM signal is enabled!");
				WebcamDeallocator.store(webcams.toArray(new Webcam[webcams.size()]));
			}

			if (LOG.isInfoEnabled()) {
				for (Webcam webcam : webcams) {
					LOG.info("Webcam found " + webcam.getName());
				}
			}
		}

		return Collections.unmodifiableList(webcams);
	}

	protected static void clearWebcams() {
		webcams = null;
	}

	/**
	 * Will discover and return first webcam available in the system.
	 * 
	 * @return Default webcam (first from the list)
	 * @throws WebcamException if something is wrong
	 */
	public static Webcam getDefault() {
		try {
			return getDefault(timeout);
		} catch (TimeoutException e) {
			throw new WebcamException(e);
		}
	}

	/**
	 * Will discover and return first webcam available in the system.
	 * 
	 * @param timeout the webcam discovery timeout (1 minute by default)
	 * @return Default webcam (first from the list)
	 * @throws TimeoutException when discovery timeout has been exceeded
	 */
	public static Webcam getDefault(long timeout) throws TimeoutException {
		List<Webcam> webcams = getWebcams(timeout);
		if (webcams.isEmpty()) {
			throw new WebcamException("No webcam available in the system");
		}
		return webcams.get(0);
	}

	/**
	 * Get webcam name (device name). The name of device depends on the value
	 * returned by the underlying data source, so in some cases it can be
	 * human-readable value and sometimes it can be some strange number.
	 * 
	 * @return Name
	 */
	public String getName() {
		return device.getName();
	}

	@Override
	public String toString() {
		return String.format("Webcam %s", getName());
	}

	/**
	 * Add webcam listener.
	 * 
	 * @param l the listener to be added
	 */
	public boolean addWebcamListener(WebcamListener l) {
		if (l == null) {
			throw new IllegalArgumentException("Webcam listener cannot be null!");
		}
		synchronized (listeners) {
			return listeners.add(l);
		}
	}

	/**
	 * @return All webcam listeners
	 */
	public WebcamListener[] getWebcamListeners() {
		synchronized (listeners) {
			return listeners.toArray(new WebcamListener[listeners.size()]);
		}
	}

	/**
	 * Removes webcam listener.
	 * 
	 * @param l the listener to be removed
	 * @return True if listener has been removed, false otherwise
	 */
	public boolean removeWebcamListener(WebcamListener l) {
		synchronized (listeners) {
			return listeners.remove(l);
		}
	}

	/**
	 * @return Data source currently used by webcam
	 */
	public static WebcamDriver getDriver() {
		return driver;
	}

	/**
	 * Set new video driver to be used by webcam.
	 * 
	 * @param driver new video driver to use (e.g. Civil, JFM, FMJ, QTJ, etc)
	 */
	public static void setDriver(WebcamDriver driver) {
		if (driver == null) {
			throw new IllegalArgumentException("Webcam driver cannot be null!");
		}
		resetDriver();
		Webcam.driver = driver;
	}

	/**
	 * Set new video driver class to be used by webcam. Class given in the
	 * argument shall extend {@link WebcamDriver} interface and should have
	 * public default constructor, so instance can be created by reflection.
	 * 
	 * @param driver new video driver class to use
	 */
	public static void setDriver(Class<? extends WebcamDriver> driverClass) {

		resetDriver();

		if (driverClass == null) {
			throw new IllegalArgumentException("Webcam driver class cannot be null!");
		}

		try {
			driver = driverClass.newInstance();
		} catch (InstantiationException e) {
			throw new WebcamException(e);
		} catch (IllegalAccessException e) {
			throw new WebcamException(e);
		}

	}

	public static void resetDriver() {

		DRIVERS_LIST.clear();
		DRIVERS_LIST.addAll(Arrays.asList(DRIVERS_DEFAULT));

		driver = null;

		if (deallocOnTermSignal) {
			WebcamDeallocator.unstore();
		}

		if (webcams != null && !webcams.isEmpty()) {
			for (Webcam webcam : webcams) {
				webcam.dispose();
			}
			webcams.clear();
		}

		webcams = null;
	}

	/**
	 * Register new webcam video driver.
	 * 
	 * @param clazz webcam video driver class
	 */
	public static void registerDriver(Class<? extends WebcamDriver> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("Webcam driver class to register cannot be null!");
		}
		DRIVERS_CLASS_LIST.add(clazz);
		registerDriver(clazz.getCanonicalName());
	}

	/**
	 * Register new webcam video driver.
	 * 
	 * @param clazzName webcam video driver class name
	 */
	public static void registerDriver(String clazzName) {
		if (clazzName == null) {
			throw new IllegalArgumentException("Webcam driver class name to register cannot be null!");
		}
		DRIVERS_LIST.add(clazzName);
	}

	/**
	 * Return underlying webcam device. Depending on the driver used to discover
	 * devices, this method can return instances of different class. By default
	 * {@link WebcamDefaultDevice} is returned when no external driver is used.
	 * 
	 * @return Underlying webcam device instance
	 */
	public WebcamDevice getDevice() {
		return device;
	}

	/**
	 * Completely dispose capture device. After this operation webcam cannot be
	 * used any more and reinstantiation is required.
	 */
	protected void dispose() {

		LOG.info("Disposing webcam {}", getName());

		// hook can be null because there is a possibility that webcam has never
		// been open and therefore hook was not created
		if (hook != null) {
			Runtime.getRuntime().removeShutdownHook(hook);
		}

		open = false;
		disposed = true;

		WebcamEvent we = new WebcamEvent(this);
		for (WebcamListener l : listeners) {
			try {
				l.webcamClosed(we);
				l.webcamDisposed(we);
			} catch (Exception e) {
				LOG.error(String.format("Notify webcam disposed, exception when calling %s listener", l.getClass()), e);
			}
		}

		synchronized (this) {
			device.close();
			device.dispose();
		}
	}

	/**
	 * <b>CAUTION!!!</b><br>
	 * <br>
	 * This is experimental feature to be used mostly in in development phase.
	 * After you set handle term signal to true, and fetch capture devices,
	 * Webcam Capture API will listen for TERM signal and try to close all
	 * devices after it has been received. <b>This feature can be unstable on
	 * some systems!</b>
	 * 
	 * @param on
	 */
	public static void handleTermSignal(boolean on) {
		deallocOnTermSignal = on;
	}

	/**
	 * Set new devices discovery timeout. By default this is set to 1 minute
	 * (60000 milliseconds).
	 * 
	 * @param timeout the new discovery timeout in milliseconds
	 */
	public static void setDiscoveryTimeout(long timeout) {
		Webcam.timeout = timeout;
	}
}
