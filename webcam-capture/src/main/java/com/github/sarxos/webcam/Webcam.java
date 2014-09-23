package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice.BufferAccess;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDevice;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import com.github.sarxos.webcam.ds.cgt.WebcamCloseTask;
import com.github.sarxos.webcam.ds.cgt.WebcamDisposeTask;
import com.github.sarxos.webcam.ds.cgt.WebcamGetBufferTask;
import com.github.sarxos.webcam.ds.cgt.WebcamGetImageTask;
import com.github.sarxos.webcam.ds.cgt.WebcamOpenTask;
import com.github.sarxos.webcam.ds.cgt.WebcamReadBufferTask;


/**
 * Webcam class. It wraps webcam device obtained from webcam driver.
 *
 * @author Bartosz Firyn (bfiryn)
 */
public class Webcam {

	/**
	 * Class used to asynchronously notify all webcam listeners about new image available.
	 *
	 * @author Bartosz Firyn (sarxos)
	 */
	private static final class ImageNotification implements Runnable {

		/**
		 * Camera.
		 */
		private final Webcam webcam;

		/**
		 * Acquired image.
		 */
		private final BufferedImage image;

		/**
		 * Create new notification.
		 *
		 * @param webcam the webcam from which image has been acquired
		 * @param image the acquired image
		 */
		public ImageNotification(Webcam webcam, BufferedImage image) {
			this.webcam = webcam;
			this.image = image;
		}

		@Override
		public void run() {
			if (image != null) {
				WebcamEvent we = new WebcamEvent(WebcamEventType.NEW_IMAGE, webcam, image);
				for (WebcamListener l : webcam.getWebcamListeners()) {
					try {
						l.webcamImageObtained(we);
					} catch (Exception e) {
						LOG.error(String.format("Notify image acquired, exception when calling listener %s", l.getClass()), e);
					}
				}
			}
		}
	}

	private final class NotificationThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, String.format("notificator-[%s]", getName()));
			t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
			t.setDaemon(true);
			return t;
		}
	}

	/**
	 * Logger instance.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Webcam.class);

	/**
	 * List of driver classes names to search for.
	 */
	private static final List<String> DRIVERS_LIST = new ArrayList<String>();

	/**
	 * List of driver classes to search for.
	 */
	private static final List<Class<?>> DRIVERS_CLASS_LIST = new ArrayList<Class<?>>();

	/**
	 * Discovery listeners.
	 */
	private static final List<WebcamDiscoveryListener> DISCOVERY_LISTENERS = Collections.synchronizedList(new ArrayList<WebcamDiscoveryListener>());

	/**
	 * Webcam driver (LtiCivil, JMF, FMJ, JQT, OpenCV, VLCj, etc).
	 */
	private static volatile WebcamDriver driver = null;

	/**
	 * Webcam discovery service.
	 */
	private static volatile WebcamDiscoveryService discovery = null;

	/**
	 * Is automated deallocation on TERM signal enabled.
	 */
	private static boolean deallocOnTermSignal = false;

	/**
	 * Is auto-open feature enabled?
	 */
	private static boolean autoOpen = false;

	/**
	 * Webcam listeners.
	 */
	private List<WebcamListener> listeners = new CopyOnWriteArrayList<WebcamListener>();

	/**
	 * List of custom resolution sizes supported by webcam instance.
	 */
	private List<Dimension> customSizes = new ArrayList<Dimension>();

	/**
	 * Shutdown hook.
	 */
	private WebcamShutdownHook hook = null;

	/**
	 * Underlying webcam device.
	 */
	private WebcamDevice device = null;

	/**
	 * Is webcam open?
	 */
	private AtomicBoolean open = new AtomicBoolean(false);

	/**
	 * Is webcam already disposed?
	 */
	private AtomicBoolean disposed = new AtomicBoolean(false);

	/**
	 * Is non-blocking (asynchronous) access enabled?
	 */
	private volatile boolean asynchronous = false;

	/**
	 * Current FPS.
	 */
	private volatile double fps = 0;

	/**
	 * Webcam image updater.
	 */
	private volatile WebcamUpdater updater = null;

	/**
	 * Image transformer.
	 */
	private volatile WebcamImageTransformer transformer = null;

	/**
	 * Lock which denies access to the given webcam when it's already in use by other webcam capture
	 * API process or thread.
	 */
	private WebcamLock lock = null;

	/**
	 * Executor service for image notifications.
	 */
	private ExecutorService notificator = null;

	/**
	 * Webcam class.
	 *
	 * @param device - device to be used as webcam
	 * @throws IllegalArgumentException when device argument is null
	 */
	protected Webcam(WebcamDevice device) {
		if (device == null) {
			throw new IllegalArgumentException("Webcam device cannot be null");
		}
		this.device = device;
		this.lock = new WebcamLock(this);
	}

	/**
	 * Asynchronously start new thread which will notify all webcam listeners about the new image
	 * available.
	 */
	protected void notifyWebcamImageAcquired(BufferedImage image) {

		// notify webcam listeners of new image available, do that only if there
		// are any webcam listeners available because there is no sense to start
		// additional threads for no purpose

		if (getWebcamListenersCount() > 0) {
			notificator.execute(new ImageNotification(this, image));
		}
	}

	/**
	 * Open the webcam in blocking (synchronous) mode.
	 *
	 * @return True if webcam has been open, false otherwise
	 * @see #open(boolean)
	 * @throws WebcamException when something went wrong
	 */
	public boolean open() {
		return open(false);
	}

	/**
	 * Open the webcam in either blocking (synchronous) or non-blocking (asynchronous) mode.The
	 * difference between those two modes lies in the image acquisition mechanism.<br>
	 * <br>
	 * In blocking mode, when user calls {@link #getImage()} method, device is being queried for new
	 * image buffer and user have to wait for it to be available.<br>
	 * <br>
	 * In non-blocking mode, there is a special thread running in the background which constantly
	 * fetch new images and cache them internally for further use. This cached instance is returned
	 * every time when user request new image. Because of that it can be used when timeing is very
	 * important, because all users calls for new image do not have to wait on device response. By
	 * using this mode user should be aware of the fact that in some cases, when two consecutive
	 * calls to get new image are executed more often than webcam device can serve them, the same
	 * image instance will be returned. User should use {@link #isImageNew()} method to distinguish
	 * if returned image is not the same as the previous one.
	 *
	 * @param async true for non-blocking mode, false for blocking
	 * @return True if webcam has been open
	 * @throws WebcamException when something went wrong
	 */
	public boolean open(boolean async) {

		if (open.compareAndSet(false, true)) {

			assert lock != null;

			notificator = Executors.newSingleThreadExecutor(new NotificationThreadFactory());

			// lock webcam for other Java (only) processes

			lock.lock();

			// open webcam device

			WebcamOpenTask task = new WebcamOpenTask(driver, device);
			try {
				task.open();
			} catch (InterruptedException e) {
				lock.unlock();
				open.set(false);
				LOG.debug("Thread has been interrupted in the middle of webcam opening process!", e);
				return false;
			} catch (WebcamException e) {
				lock.unlock();
				open.set(false);
				LOG.debug("Webcam exception when opening", e);
				throw e;
			}

			LOG.debug("Webcam is now open {}", getName());

			// setup non-blocking configuration

			if (asynchronous = async) {
				if (updater == null) {
					updater = new WebcamUpdater(this);
				}
				updater.start();
			}

			// install shutdown hook

			Runtime.getRuntime().addShutdownHook(hook = new WebcamShutdownHook(this));

			// notify listeners

			WebcamEvent we = new WebcamEvent(WebcamEventType.OPEN, this);
			Iterator<WebcamListener> wli = listeners.iterator();
			WebcamListener l = null;

			while (wli.hasNext()) {
				l = wli.next();
				try {
					l.webcamOpen(we);
				} catch (Exception e) {
					LOG.error(String.format("Notify webcam open, exception when calling listener %s", l.getClass()), e);
				}
			}

		} else {
			LOG.debug("Webcam is already open {}", getName());
		}

		return true;
	}

	/**
	 * Close the webcam.
	 *
	 * @return True if webcam has been open, false otherwise
	 */
	public boolean close() {

		if (open.compareAndSet(true, false)) {

			LOG.debug("Closing webcam {}", getName());

			assert lock != null;

			// close webcam

			WebcamCloseTask task = new WebcamCloseTask(driver, device);
			try {
				task.close();
			} catch (InterruptedException e) {
				open.set(true);
				LOG.debug("Thread has been interrupted before webcam was closed!", e);
				return false;
			} catch (WebcamException e) {
				open.set(true);
				throw e;
			}

			// stop updater
			if (asynchronous) {
				updater.stop();
			}

			// remove shutdown hook (it's not more necessary)
			removeShutdownHook();

			// unlock webcam so other Java processes can start using it
			lock.unlock();

			// notify listeners

			WebcamEvent we = new WebcamEvent(WebcamEventType.CLOSED, this);
			Iterator<WebcamListener> wli = listeners.iterator();
			WebcamListener l = null;

			while (wli.hasNext()) {
				l = wli.next();
				try {
					l.webcamClosed(we);
				} catch (Exception e) {
					LOG.error(String.format("Notify webcam closed, exception when calling %s listener", l.getClass()), e);
				}
			}

			notificator.shutdown();
			while (!notificator.isTerminated()) {
				try {
					notificator.awaitTermination(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					return false;
				}
			}

			LOG.debug("Webcam {} has been closed", getName());

		} else {
			LOG.debug("Webcam {} is already closed", getName());
		}

		return true;
	}

	/**
	 * Return underlying webcam device. Depending on the driver used to discover devices, this
	 * method can return instances of different class. By default {@link WebcamDefaultDevice} is
	 * returned when no external driver is used.
	 *
	 * @return Underlying webcam device instance
	 */
	public WebcamDevice getDevice() {
		assert device != null;
		return device;
	}

	/**
	 * Completely dispose capture device. After this operation webcam cannot be used any more and
	 * full reinstantiation is required.
	 */
	protected void dispose() {

		assert disposed != null;
		assert open != null;
		assert driver != null;
		assert device != null;
		assert listeners != null;

		if (!disposed.compareAndSet(false, true)) {
			return;
		}

		open.set(false);

		LOG.info("Disposing webcam {}", getName());

		WebcamDisposeTask task = new WebcamDisposeTask(driver, device);
		try {
			task.dispose();
		} catch (InterruptedException e) {
			LOG.error("Processor has been interrupted before webcam was disposed!", e);
			return;
		}

		WebcamEvent we = new WebcamEvent(WebcamEventType.DISPOSED, this);
		Iterator<WebcamListener> wli = listeners.iterator();
		WebcamListener l = null;

		while (wli.hasNext()) {
			l = wli.next();
			try {
				l.webcamClosed(we);
				l.webcamDisposed(we);
			} catch (Exception e) {
				LOG.error(String.format("Notify webcam disposed, exception when calling %s listener", l.getClass()), e);
			}
		}

		removeShutdownHook();

		LOG.debug("Webcam disposed {}", getName());
	}

	private void removeShutdownHook() {

		// hook can be null because there is a possibility that webcam has never
		// been open and therefore hook was not created

		if (hook != null) {
			try {
				Runtime.getRuntime().removeShutdownHook(hook);
			} catch (IllegalStateException e) {
				LOG.trace("Shutdown in progress, cannot remove hook");
			}
		}
	}

	/**
	 * TRansform image using image transformer. If image transformer has not been set, this method
	 * return instance passed in the argument, without any modifications.
	 *
	 * @param image the image to be transformed
	 * @return Transformed image (if transformer is set)
	 */
	protected BufferedImage transform(BufferedImage image) {
		if (image != null) {
			WebcamImageTransformer tr = getImageTransformer();
			if (tr != null) {
				return tr.transform(image);
			}
		}
		return image;
	}

	/**
	 * Is webcam open?
	 *
	 * @return true if open, false otherwise
	 */
	public boolean isOpen() {
		return open.get();
	}

	/**
	 * Get current webcam resolution in pixels.
	 *
	 * @return Webcam resolution (picture size) in pixels.
	 */
	public Dimension getViewSize() {
		return device.getResolution();
	}

	/**
	 * Return list of supported view sizes. It can differ between vary webcam data sources.
	 *
	 * @return Array of supported dimensions
	 */
	public Dimension[] getViewSizes() {
		return device.getResolutions();
	}

	/**
	 * Set custom resolution. If you are using this method you have to make sure that your webcam
	 * device can support this specific resolution.
	 *
	 * @param sizes the array of custom resolutions to be supported by webcam
	 */
	public void setCustomViewSizes(Dimension[] sizes) {
		assert customSizes != null;
		if (sizes == null) {
			customSizes.clear();
			return;
		}
		customSizes = Arrays.asList(sizes);
	}

	public Dimension[] getCustomViewSizes() {
		assert customSizes != null;
		return customSizes.toArray(new Dimension[customSizes.size()]);
	}

	/**
	 * Set new view size. New size has to exactly the same as one of the default sized or exactly
	 * the same as one of the custom ones.
	 *
	 * @param size the new view size to be set
	 * @see Webcam#setCustomViewSizes(Dimension[])
	 * @see Webcam#getViewSizes()
	 */
	public void setViewSize(Dimension size) {

		if (size == null) {
			throw new IllegalArgumentException("Resolution cannot be null!");
		}

		if (open.get()) {
			throw new IllegalStateException("Cannot change resolution when webcam is open, please close it first");
		}

		// check if new resolution is the same as current one

		Dimension current = getViewSize();
		if (current != null && current.width == size.width && current.height == size.height) {
			return;
		}

		// check if new resolution is valid

		Dimension[] predefined = getViewSizes();
		Dimension[] custom = getCustomViewSizes();

		assert predefined != null;
		assert custom != null;

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

		LOG.debug("Setting new resolution {}x{}", size.width, size.height);

		device.setResolution(size);
	}

	/**
	 * Capture image from webcam and return it. Will return image object or null if webcam is closed
	 * or has been already disposed by JVM.<br>
	 * <br>
	 * <b>IMPORTANT NOTE!!!</b><br>
	 * <br>
	 * There are two possible behaviors of what webcam should do when you try to get image and
	 * webcam is actually closed. Normally it will return null, but there is a special flag which
	 * can be statically set to switch all webcams to auto open mode. In this mode, webcam will be
	 * automatically open, when you try to get image from closed webcam. Please be aware of some
	 * side effects! In case of multi-threaded applications, there is no guarantee that one thread
	 * will not try to open webcam even if it was manually closed in different thread.
	 *
	 * @return Captured image or null if webcam is closed or disposed by JVM
	 */
	public BufferedImage getImage() {

		if (!isReady()) {
			return null;
		}

		long t1 = 0;
		long t2 = 0;

		if (asynchronous) {
			return updater.getImage();
		} else {

			// get image

			t1 = System.currentTimeMillis();
			BufferedImage image = transform(new WebcamGetImageTask(driver, device).getImage());
			t2 = System.currentTimeMillis();

			if (image == null) {
				return null;
			}

			// get FPS

			if (device instanceof WebcamDevice.FPSSource) {
				fps = ((WebcamDevice.FPSSource) device).getFPS();
			} else {
				// +1 to avoid division by zero
				fps = (4 * fps + 1000 / (t2 - t1 + 1)) / 5;
			}

			// notify webcam listeners about new image available

			notifyWebcamImageAcquired(image);

			return image;
		}
	}

	public boolean isImageNew() {
		if (asynchronous) {
			return updater.isImageNew();
		}
		return true;
	}

	public double getFPS() {
		if (asynchronous) {
			return updater.getFPS();
		} else {
			return fps;
		}
	}

	/**
	 * Get RAW image ByteBuffer. It will always return buffer with 3 x 1 bytes per each pixel, where
	 * RGB components are on (0, 1, 2) and color space is sRGB.<br>
	 * <br>
	 * <b>IMPORTANT!</b><br>
	 * Some drivers can return direct ByteBuffer, so there is no guarantee that underlying bytes
	 * will not be released in next read image operation. Therefore, to avoid potential bugs you
	 * should convert this ByteBuffer to bytes array before you fetch next image.
	 *
	 * @return Byte buffer
	 */
	public ByteBuffer getImageBytes() {

		if (!isReady()) {
			return null;
		}

		assert driver != null;
		assert device != null;

		// some devices can support direct image buffers, and for those call
		// processor task, and for those which does not support direct image
		// buffers, just convert image to RGB byte array

		if (device instanceof BufferAccess) {
			return new WebcamGetBufferTask(driver, device).getBuffer();
		} else {
			throw new IllegalStateException(String.format("Driver %s does not support buffer access", driver.getClass().getName()));
		}
	}

	/**
	 * Get RAW image ByteBuffer. It will always return buffer with 3 x 1 bytes per each pixel, where
	 * RGB components are on (0, 1, 2) and color space is sRGB.<br>
	 * <br>
	 * <b>IMPORTANT!</b><br>
	 * Some drivers can return direct ByteBuffer, so there is no guarantee that underlying bytes
	 * will not be released in next read image operation. Therefore, to avoid potential bugs you
	 * should convert this ByteBuffer to bytes array before you fetch next image.
	 *
	 * @param target the target {@link ByteBuffer} object to copy data into
	 */
	public void getImageBytes(ByteBuffer target) {

		if (!isReady()) {
			return;
		}

		assert driver != null;
		assert device != null;

		// some devices can support direct image buffers, and for those call
		// processor task, and for those which does not support direct image
		// buffers, just convert image to RGB byte array

		if (device instanceof BufferAccess) {
			new WebcamReadBufferTask(driver, device, target).readBuffer();
		} else {
			throw new IllegalStateException(String.format("Driver %s does not support buffer access", driver.getClass().getName()));
		}
	}

	/**
	 * Is webcam ready to be read.
	 *
	 * @return True if ready, false otherwise
	 */
	private boolean isReady() {

		assert disposed != null;
		assert open != null;

		if (disposed.get()) {
			LOG.warn("Cannot get image, webcam has been already disposed");
			return false;
		}

		if (!open.get()) {
			if (autoOpen) {
				open();
			} else {
				return false;
			}
		}

		return true;
	}

	/**
	 * Get list of webcams to use. This method will wait predefined time interval for webcam devices
	 * to be discovered. By default this time is set to 1 minute.
	 *
	 * @return List of webcams existing in the system
	 * @throws WebcamException when something is wrong
	 * @see Webcam#getWebcams(long, TimeUnit)
	 */
	public static List<Webcam> getWebcams() throws WebcamException {

		// timeout exception below will never be caught since user would have to
		// wait around three hundreds billion years for it to occur

		try {
			return getWebcams(Long.MAX_VALUE);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get list of webcams to use. This method will wait given time interval for webcam devices to
	 * be discovered. Time argument is given in milliseconds.
	 *
	 * @param timeout the time to wait for webcam devices to be discovered
	 * @return List of webcams existing in the ssytem
	 * @throws TimeoutException when timeout occurs
	 * @throws WebcamException when something is wrong
	 * @throws IllegalArgumentException when timeout is negative
	 * @see Webcam#getWebcams(long, TimeUnit)
	 */
	public static List<Webcam> getWebcams(long timeout) throws TimeoutException, WebcamException {
		if (timeout < 0) {
			throw new IllegalArgumentException(String.format("Timeout cannot be negative (%d)", timeout));
		}
		return getWebcams(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get list of webcams to use. This method will wait given time interval for webcam devices to
	 * be discovered.
	 *
	 * @param timeout the devices discovery timeout
	 * @param tunit the time unit
	 * @return List of webcams
	 * @throws TimeoutException when timeout has been exceeded
	 * @throws WebcamException when something is wrong
	 * @throws IllegalArgumentException when timeout is negative or tunit null
	 */
	public static synchronized List<Webcam> getWebcams(long timeout, TimeUnit tunit) throws TimeoutException, WebcamException {

		if (timeout < 0) {
			throw new IllegalArgumentException(String.format("Timeout cannot be negative (%d)", timeout));
		}
		if (tunit == null) {
			throw new IllegalArgumentException("Time unit cannot be null!");
		}

		WebcamDiscoveryService discovery = getDiscoveryService();

		assert discovery != null;

		List<Webcam> webcams = discovery.getWebcams(timeout, tunit);
		if (!discovery.isRunning()) {
			discovery.start();
		}

		return webcams;
	}

	/**
	 * Will discover and return first webcam available in the system.
	 *
	 * @return Default webcam (first from the list)
	 * @throws WebcamException if something is really wrong
	 * @see Webcam#getWebcams()
	 */
	public static Webcam getDefault() throws WebcamException {

		try {
			return getDefault(Long.MAX_VALUE);
		} catch (TimeoutException e) {
			// this should never happen since user would have to wait 300000000
			// years for it to occur
			throw new RuntimeException(e);
		}
	}

	/**
	 * Will discover and return first webcam available in the system.
	 *
	 * @param timeout the webcam discovery timeout (1 minute by default)
	 * @return Default webcam (first from the list)
	 * @throws TimeoutException when discovery timeout has been exceeded
	 * @throws WebcamException if something is really wrong
	 * @throws IllegalArgumentException when timeout is negative
	 * @see Webcam#getWebcams(long)
	 */
	public static Webcam getDefault(long timeout) throws TimeoutException, WebcamException {
		if (timeout < 0) {
			throw new IllegalArgumentException(String.format("Timeout cannot be negative (%d)", timeout));
		}
		return getDefault(timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Will discover and return first webcam available in the system.
	 *
	 * @param timeout the webcam discovery timeout (1 minute by default)
	 * @param tunit the time unit
	 * @return Default webcam (first from the list)
	 * @throws TimeoutException when discovery timeout has been exceeded
	 * @throws WebcamException if something is really wrong
	 * @throws IllegalArgumentException when timeout is negative or tunit null
	 * @see Webcam#getWebcams(long, TimeUnit)
	 */
	public static Webcam getDefault(long timeout, TimeUnit tunit) throws TimeoutException, WebcamException {

		if (timeout < 0) {
			throw new IllegalArgumentException(String.format("Timeout cannot be negative (%d)", timeout));
		}
		if (tunit == null) {
			throw new IllegalArgumentException("Time unit cannot be null!");
		}

		List<Webcam> webcams = getWebcams(timeout, tunit);

		assert webcams != null;

		if (!webcams.isEmpty()) {
			return webcams.get(0);
		}

		LOG.warn("No webcam has been detected!");

		return null;
	}

	/**
	 * Get webcam name (device name). The name of device depends on the value returned by the
	 * underlying data source, so in some cases it can be human-readable value and sometimes it can
	 * be some strange number.
	 *
	 * @return Name
	 */
	public String getName() {
		assert device != null;
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
	 * @return True if listener has been added, false if it was already there
	 * @throws IllegalArgumentException when argument is null
	 */
	public boolean addWebcamListener(WebcamListener l) {
		if (l == null) {
			throw new IllegalArgumentException("Webcam listener cannot be null!");
		}
		assert listeners != null;
		return listeners.add(l);
	}

	/**
	 * @return All webcam listeners
	 */
	public WebcamListener[] getWebcamListeners() {
		assert listeners != null;
		return listeners.toArray(new WebcamListener[listeners.size()]);
	}

	/**
	 * @return Number of webcam listeners
	 */
	public int getWebcamListenersCount() {
		assert listeners != null;
		return listeners.size();
	}

	/**
	 * Removes webcam listener.
	 *
	 * @param l the listener to be removed
	 * @return True if listener has been removed, false otherwise
	 */
	public boolean removeWebcamListener(WebcamListener l) {
		assert listeners != null;
		return listeners.remove(l);
	}

	/**
	 * Return webcam driver. Perform search if necessary.<br>
	 * <br>
	 * <b>This method is not thread-safe!</b>
	 *
	 * @return Webcam driver
	 */
	public static synchronized WebcamDriver getDriver() {

		if (driver != null) {
			return driver;
		}

		if (driver == null) {
			driver = WebcamDriverUtils.findDriver(DRIVERS_LIST, DRIVERS_CLASS_LIST);
		}
		if (driver == null) {
			driver = new WebcamDefaultDriver();
		}

		LOG.info("{} capture driver will be used", driver.getClass().getSimpleName());

		return driver;
	}

	/**
	 * Set new video driver to be used by webcam.<br>
	 * <br>
	 * <b>This method is not thread-safe!</b>
	 *
	 * @param wd new webcam driver to be used (e.g. LtiCivil, JFM, FMJ, QTJ)
	 * @throws IllegalArgumentException when argument is null
	 */
	public static void setDriver(WebcamDriver wd) {

		if (wd == null) {
			throw new IllegalArgumentException("Webcam driver cannot be null!");
		}

		LOG.debug("Setting new capture driver {}", wd);

		resetDriver();

		driver = wd;
	}

	/**
	 * Set new video driver class to be used by webcam. Class given in the argument shall extend
	 * {@link WebcamDriver} interface and should have public default constructor, so instance can be
	 * created by reflection.<br>
	 * <br>
	 * <b>This method is not thread-safe!</b>
	 *
	 * @param driverClass new video driver class to use
	 * @throws IllegalArgumentException when argument is null
	 */
	public static void setDriver(Class<? extends WebcamDriver> driverClass) {

		if (driverClass == null) {
			throw new IllegalArgumentException("Webcam driver class cannot be null!");
		}

		resetDriver();

		try {
			driver = driverClass.newInstance();
		} catch (InstantiationException e) {
			throw new WebcamException(e);
		} catch (IllegalAccessException e) {
			throw new WebcamException(e);
		}
	}

	/**
	 * Reset webcam driver.<br>
	 * <br>
	 * <b>This method is not thread-safe!</b>
	 */
	public static void resetDriver() {

		synchronized (DRIVERS_LIST) {
			DRIVERS_LIST.clear();
		}

		if (discovery != null) {
			discovery.shutdown();
			discovery = null;
		}

		driver = null;
	}

	/**
	 * Register new webcam video driver.
	 *
	 * @param clazz webcam video driver class
	 * @throws IllegalArgumentException when argument is null
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
	 * @throws IllegalArgumentException when argument is null
	 */
	public static void registerDriver(String clazzName) {
		if (clazzName == null) {
			throw new IllegalArgumentException("Webcam driver class name to register cannot be null!");
		}
		DRIVERS_LIST.add(clazzName);
	}

	/**
	 * <b>CAUTION!!!</b><br>
	 * <br>
	 * This is experimental feature to be used mostly in in development phase. After you set handle
	 * term signal to true, and fetch capture devices, Webcam Capture API will listen for TERM
	 * signal and try to close all devices after it has been received. <b>This feature can be
	 * unstable on some systems!</b>
	 *
	 * @param on signal handling will be enabled if true, disabled otherwise
	 */
	public static void setHandleTermSignal(boolean on) {
		if (on) {
			LOG.warn("Automated deallocation on TERM signal is now enabled! Make sure to not use it in production!");
		}
		deallocOnTermSignal = on;
	}

	/**
	 * Is TERM signal handler enabled.
	 *
	 * @return True if enabled, false otherwise
	 */
	public static boolean isHandleTermSignal() {
		return deallocOnTermSignal;
	}

	/**
	 * Switch all webcams to auto open mode. In this mode, each webcam will be automatically open
	 * whenever user will try to get image from instance which has not yet been open. Please be
	 * aware of some side effects! In case of multi-threaded applications, there is no guarantee
	 * that one thread will not try to open webcam even if it was manually closed in different
	 * thread.
	 *
	 * @param on true to enable, false to disable
	 */
	public static void setAutoOpenMode(boolean on) {
		autoOpen = on;
	}

	/**
	 * Is auto open mode enabled. Auto open mode will will automatically open webcam whenever user
	 * will try to get image from instance which has not yet been open. Please be aware of some side
	 * effects! In case of multi-threaded applications, there is no guarantee that one thread will
	 * not try to open webcam even if it was manually closed in different thread.
	 *
	 * @return True if mode is enabled, false otherwise
	 */
	public static boolean isAutoOpenMode() {
		return autoOpen;
	}

	/**
	 * Add new webcam discovery listener.
	 *
	 * @param l the listener to be added
	 * @return True, if listeners list size has been changed, false otherwise
	 * @throws IllegalArgumentException when argument is null
	 */
	public static boolean addDiscoveryListener(WebcamDiscoveryListener l) {
		if (l == null) {
			throw new IllegalArgumentException("Webcam discovery listener cannot be null!");
		}
		return DISCOVERY_LISTENERS.add(l);
	}

	public static WebcamDiscoveryListener[] getDiscoveryListeners() {
		return DISCOVERY_LISTENERS.toArray(new WebcamDiscoveryListener[DISCOVERY_LISTENERS.size()]);
	}

	/**
	 * Remove discovery listener
	 *
	 * @param l the listener to be removed
	 * @return True if listeners list contained the specified element
	 */
	public static boolean removeDiscoveryListener(WebcamDiscoveryListener l) {
		return DISCOVERY_LISTENERS.remove(l);
	}

	/**
	 * Return discovery service.
	 *
	 * @return Discovery service
	 */
	public static synchronized WebcamDiscoveryService getDiscoveryService() {
		if (discovery == null) {
			discovery = new WebcamDiscoveryService(getDriver());
		}
		return discovery;
	}

	/**
	 * Return discovery service without creating it if not exists.
	 *
	 * @return Discovery service or null if not yet created
	 */
	public static synchronized WebcamDiscoveryService getDiscoveryServiceRef() {
		return discovery;
	}

	/**
	 * Return image transformer.
	 *
	 * @return Transformer instance
	 */
	public WebcamImageTransformer getImageTransformer() {
		return transformer;
	}

	/**
	 * Set image transformer.
	 *
	 * @param transformer the transformer to be set
	 */
	public void setImageTransformer(WebcamImageTransformer transformer) {
		this.transformer = transformer;
	}

	/**
	 * Return webcam lock.
	 *
	 * @return Webcam lock
	 */
	public WebcamLock getLock() {
		return lock;
	}

	public static void shutdown() {

		// stop discovery service
		WebcamDiscoveryService discovery = getDiscoveryServiceRef();
		if (discovery != null) {
			discovery.stop();
		}

		// stop processor
		WebcamProcessor.getInstance().shutdown();
	}
}
