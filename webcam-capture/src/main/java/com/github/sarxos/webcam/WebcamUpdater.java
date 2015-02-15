package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.ds.cgt.WebcamGetImageTask;


/**
 * The goal of webcam updater class is to update image in parallel, so all calls
 * to fetch image invoked on webcam instance will be non-blocking (will return
 * immediately).
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamUpdater implements Runnable {

	/**
	 * Implementation of this interface is responsible for calculating the delay
	 * between 2 image fetching, when the non-blocking (asynchronous) access to the
	 * webcam is enabled.
	 */
	public static interface DelayCalculator {

		/**
		 * Calculates delay before the next image will be fetched from the
		 * webcam.
		 * Must return number greater or equal 0. 
		 * 
		 * @param snapshotDuration - duration of taking the last image
		 * @param deviceFps - current FPS obtained from the device, or -1 if the
		 *            driver doesn't support it
		 * @return interval (in millis)
		 */
		long calculateDelay(long snapshotDuration, double deviceFps);
	}

	/**
	 * Default impl of DelayCalculator, based on TARGET_FPS. Returns 0 delay for
	 * snapshotDuration &gt; 20 millis.
	 */
	public static class DefaultDelayCalculator implements DelayCalculator {

		@Override
		public long calculateDelay(long snapshotDuration, double deviceFps) {
			// Calculate delay required to achieve target FPS. 
			// In some cases it can be less than 0 
			// because camera is not able to serve images as fast as
			// we would like to. In such case just run with no delay, 
			// so maximum FPS will be the one supported 
			// by camera device in the moment.

			long delay = Math.max((1000 / TARGET_FPS) - snapshotDuration, 0);
			return delay;
		}
	}

	/**
	 * Thread factory for executors used within updater class.
	 * 
	 * @author Bartosz Firyn (sarxos)
	 */
	private static final class UpdaterThreadFactory implements ThreadFactory {

		private static final AtomicInteger number = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, String.format("webcam-updater-thread-%d", number.incrementAndGet()));
			t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
			t.setDaemon(true);
			return t;
		}

	}

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamUpdater.class);

	/**
	 * Target FPS.
	 */
	private static final int TARGET_FPS = 50;

	private static final UpdaterThreadFactory THREAD_FACTORY = new UpdaterThreadFactory();

	/**
	 * Executor service.
	 */
	private ScheduledExecutorService executor = null;

	/**
	 * Cached image.
	 */
	private final AtomicReference<BufferedImage> image = new AtomicReference<BufferedImage>();

	/**
	 * Webcam to which this updater is attached.
	 */
	private Webcam webcam = null;

	/**
	 * Current FPS rate.
	 */
	private volatile double fps = 0;

	/**
	 * Is updater running.
	 */
	private AtomicBoolean running = new AtomicBoolean(false);

	private volatile boolean imageNew = false;

	/**
	 * DelayCalculator implementation.
	 */
	private final DelayCalculator delayCalculator;
	
	/**
	 * Construct new webcam updater using DefaultDelayCalculator.
	 * 
	 * @param webcam the webcam to which updater shall be attached
	 */
	protected WebcamUpdater(Webcam webcam) {
		this(webcam, new DefaultDelayCalculator());
	}

	/**
	 * Construct new webcam updater.
	 * 
	 * @param webcam the webcam to which updater shall be attached
	 * @param delayCalculator implementation
	 */
	public WebcamUpdater(Webcam webcam, DelayCalculator delayCalculator) {
		this.webcam = webcam;
		if (delayCalculator == null) {
			this.delayCalculator = new DefaultDelayCalculator();
		} else {
			this.delayCalculator = delayCalculator;
		}
	}

	/**
	 * Start updater.
	 */
	public void start() {

		if (running.compareAndSet(false, true)) {

			image.set(new WebcamGetImageTask(Webcam.getDriver(), webcam.getDevice()).getImage());

			executor = Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
			executor.execute(this);

			LOG.debug("Webcam updater has been started");
		} else {
			LOG.debug("Webcam updater is already started");
		}
	}

	/**
	 * Stop updater.
	 */
	public void stop() {
		if (running.compareAndSet(true, false)) {

			executor.shutdown();
			while (!executor.isTerminated()) {
				try {
					executor.awaitTermination(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					return;
				}
			}

			LOG.debug("Webcam updater has been stopped");
		} else {
			LOG.debug("Webcam updater is already stopped");
		}
	}

	@Override
	public void run() {

		if (!running.get()) {
			return;
		}

		try {
			tick();
		} catch (Throwable t) {
			WebcamExceptionHandler.handle(t);
		}

	}

	private void tick() {

		if (!webcam.isOpen()) {
			return;
		}

		long t1 = 0;
		long t2 = 0;

		// Calculate time required to fetch 1 picture.

		WebcamDriver driver = Webcam.getDriver();
		WebcamDevice device = webcam.getDevice();

		assert driver != null;
		assert device != null;

		BufferedImage img = null;

		t1 = System.currentTimeMillis();
		img = webcam.transform(new WebcamGetImageTask(driver, device).getImage());
		t2 = System.currentTimeMillis();

		image.set(img);
		imageNew = true;

		double deviceFps = -1;
		if (device instanceof WebcamDevice.FPSSource) {
			deviceFps = ((WebcamDevice.FPSSource) device).getFPS();
		}

		long duration = t2 - t1; 
		long delay = delayCalculator.calculateDelay(duration, deviceFps);
		
		long delta = duration + 1; // +1 to avoid division by zero
		if (deviceFps >= 0) {
			fps = deviceFps;
		} else {
			fps = (4 * fps + 1000 / delta) / 5;
		}

		// reschedule task

		if (webcam.isOpen()) {
			try {
				executor.schedule(this, delay, TimeUnit.MILLISECONDS);
			} catch (RejectedExecutionException e) {
				LOG.trace("Webcam update has been rejected", e);
			}
		}

		// notify webcam listeners about the new image available

		webcam.notifyWebcamImageAcquired(image.get());
	}

	/**
	 * Return currently available image. This method will return immediately
	 * while it was been called after camera has been open. In case when there
	 * are parallel threads running and there is a possibility to call this
	 * method in the opening time, or before camera has been open at all, this
	 * method will block until webcam return first image. Maximum blocking time
	 * will be 10 seconds, after this time method will return null.
	 * 
	 * @return Image stored in cache
	 */
	public BufferedImage getImage() {

		int i = 0;
		while (image.get() == null) {

			// Just in case if another thread starts calling this method before
			// updater has been properly started. This will loop while image is
			// not available.

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			// Return null if more than 10 seconds passed (timeout).

			if (i++ > 100) {
				LOG.error("Image has not been found for more than 10 seconds");
				return null;
			}
		}

		imageNew = false;

		return image.get();
	}

	protected boolean isImageNew() {
		return imageNew;
	}

	/**
	 * Return current FPS number. It is calculated in real-time on the base of
	 * how often camera serve new image.
	 * 
	 * @return FPS number
	 */
	public double getFPS() {
		return fps;
	}
}
