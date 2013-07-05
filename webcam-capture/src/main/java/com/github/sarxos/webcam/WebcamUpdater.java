package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.ds.cgt.WebcamReadImageTask;


/**
 * The goal of webcam updater class is to update image in parallel, so all calls
 * to fetch image invoked on webcam instance will be non-blocking (will return
 * immediately).
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamUpdater implements Runnable {

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
	 * Class used to asynchronously notify all webcam listeners about new image
	 * available.
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
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);

	/**
	 * Executor service for image notifications.
	 */
	private final ExecutorService notificator = Executors.newSingleThreadExecutor(THREAD_FACTORY);

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
	private volatile boolean running = false;

	private volatile boolean imageNew = false;

	/**
	 * Construct new webcam updater.
	 * 
	 * @param webcam the webcam to which updater shall be attached
	 */
	protected WebcamUpdater(Webcam webcam) {
		this.webcam = webcam;
	}

	/**
	 * Start updater.
	 */
	public void start() {
		running = true;
		image.set(new WebcamReadImageTask(Webcam.getDriver(), webcam.getDevice()).getImage());
		executor.execute(this);

		LOG.debug("Webcam updater has been started");
	}

	/**
	 * Stop updater.
	 */
	public void stop() {
		running = false;
		LOG.debug("Webcam updater has been stopped");
	}

	@Override
	public void run() {

		if (!running) {
			return;
		}

		try {
			tick();
		} catch (Throwable t) {
			WebcamExceptionHandler.handle(t);
		}

	}

	private void tick() {

		long t1 = 0;
		long t2 = 0;

		// Calculate time required to fetch 1 picture.

		WebcamDriver driver = Webcam.getDriver();
		WebcamDevice device = webcam.getDevice();

		assert driver != null;
		assert device != null;

		BufferedImage img = null;

		t1 = System.currentTimeMillis();
		img = webcam.transform(new WebcamReadImageTask(driver, device).getImage());
		t2 = System.currentTimeMillis();

		image.set(img);
		imageNew = true;

		// Calculate delay required to achieve target FPS. In some cases it can
		// be less than 0 because camera is not able to serve images as fast as
		// we would like to. In such case just run with no delay, so maximum FPS
		// will be the one supported by camera device in the moment.

		long delta = t2 - t1 + 1; // +1 to avoid division by zero
		long delay = Math.max((1000 / TARGET_FPS) - delta, 0);

		if (device instanceof WebcamDevice.FPSSource) {
			fps = ((WebcamDevice.FPSSource) device).getFPS();
		} else {
			fps = (4 * fps + 1000 / delta) / 5;
		}

		// reschedule task

		executor.schedule(this, delay, TimeUnit.MILLISECONDS);

		// notify webcam listeners about the new image available

		notifyWebcamImageObtained(webcam, image.get());
	}

	/**
	 * Asynchronously start new thread which will notify all webcam listeners
	 * about the new image available.
	 */
	protected void notifyWebcamImageObtained(Webcam webcam, BufferedImage image) {

		// notify webcam listeners of new image available, do that only if there
		// are any webcam listeners available because there is no sense to start
		// additional threads for no purpose

		if (webcam.getWebcamListenersCount() > 0) {
			notificator.execute(new ImageNotification(webcam, image));
		}
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
