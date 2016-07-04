package com.github.sarxos.webcam;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Webcam motion detector.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamMotionDetector {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamMotionDetector.class);

	/**
	 * Thread number in pool.
	 */
	private static final AtomicInteger NT = new AtomicInteger(0);

	/**
	 * Thread factory.
	 */
	private static final ThreadFactory THREAD_FACTORY = new DetectorThreadFactory();

	/**
	 * Default check interval, in milliseconds, set to 500 ms.
	 */
	public static final int DEFAULT_INTERVAL = 500;

	/**
	 * Create new threads for detector internals.
	 *
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class DetectorThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable runnable) {
			Thread t = new Thread(runnable, String.format("motion-detector-%d", NT.incrementAndGet()));
			t.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
			t.setDaemon(true);
			return t;
		}
	}

	/**
	 * Run motion detector.
	 *
	 * @author Bartosz Firyn (SarXos)
	 */
	private class Runner implements Runnable {

		@Override
		public void run() {

			running.set(true);

			while (running.get() && webcam.isOpen()) {
				try {
					detect();
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					WebcamExceptionHandler.handle(e);
				}
			}

			running.set(false);
		}
	}

	/**
	 * Change motion to false after specified number of seconds.
	 *
	 * @author Bartosz Firyn (SarXos)
	 */
	private class Inverter implements Runnable {

		@Override
		public void run() {

			int delay = 0;

			while (running.get()) {

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					break;
				}

				delay = inertia != -1 ? inertia : 2 * interval;

				if (lastMotionTimestamp + delay < System.currentTimeMillis()) {
					motion = false;
				}
			}
		}
	}

	/**
	 * Executor.
	 */
	private final ExecutorService executor = Executors.newFixedThreadPool(2, THREAD_FACTORY);

	/**
	 * Motion listeners.
	 */
	private final List<WebcamMotionListener> listeners = new ArrayList<WebcamMotionListener>();

	/**
	 * Is detector running?
	 */
	private final AtomicBoolean running = new AtomicBoolean(false);

	/**
	 * Is motion?
	 */
	private volatile boolean motion = false;

	/**
	 * Previously captured image.
	 */
	private BufferedImage previousOriginal = null;

	/**
	 * Previously captured image with blur and gray filters applied.
	 */
	private BufferedImage previousFiltered = null;

	/**
	 * Webcam to be used to detect motion.
	 */
	private Webcam webcam = null;

	/**
	 * Motion check interval (1000 ms by default).
	 */
	private volatile int interval = DEFAULT_INTERVAL;

	/**
	 * How long motion is valid (in milliseconds). Default value is 2 seconds.
	 */
	private volatile int inertia = -1;

	/**
	 * Timestamp when motion has been observed last time.
	 */
	private volatile long lastMotionTimestamp = 0;

	/**
	 * Implementation of motion detection algorithm.
	 */
	private final WebcamMotionDetectorAlgorithm algorithm;

	/**
	 * Create motion detector. Will open webcam if it is closed.
	 * 
	 * @param webcam web camera instance
	 * @param motion detector algorithm implementation
	 * @param interval the check interval (in milliseconds)
	 */
	public WebcamMotionDetector(Webcam webcam, WebcamMotionDetectorAlgorithm algorithm, int interval) {
		this.webcam = webcam;
		this.algorithm = algorithm;
		setInterval(interval);
	}

	/**
	 * Create motion detector. Will open webcam if it is closed. Uses
	 * WebcamMotionDetectorDefaultAlgorithm for motion detection.
	 *
	 * @param webcam web camera instance
	 * @param pixelThreshold intensity threshold (0 - 255)
	 * @param areaThreshold percentage threshold of image covered by motion
	 * @param interval the check interval
	 */
	public WebcamMotionDetector(Webcam webcam, int pixelThreshold, double areaThreshold, int interval) {
		this(webcam, new WebcamMotionDetectorDefaultAlgorithm(pixelThreshold, areaThreshold), interval);
	}

	/**
	 * Create motion detector with default parameter inertia = 0. Uses
	 * WebcamMotionDetectorDefaultAlgorithm for motion detection.
	 *
	 * @param webcam web camera instance
	 * @param pixelThreshold intensity threshold (0 - 255)
	 * @param areaThreshold percentage threshold of image covered by motion (0 - 100)
	 */
	public WebcamMotionDetector(Webcam webcam, int pixelThreshold, double areaThreshold) {
		this(webcam, pixelThreshold, areaThreshold, DEFAULT_INTERVAL);
	}

	/**
	 * Create motion detector with default parameter inertia = 0. Uses
	 * WebcamMotionDetectorDefaultAlgorithm for motion detection.
	 *
	 * @param webcam web camera instance
	 * @param pixelThreshold intensity threshold (0 - 255)
	 */
	public WebcamMotionDetector(Webcam webcam, int pixelThreshold) {
		this(webcam, pixelThreshold, WebcamMotionDetectorDefaultAlgorithm.DEFAULT_AREA_THREASHOLD);
	}

	/**
	 * Create motion detector with default parameters - threshold = 25, inertia = 0.
	 *
	 * @param webcam web camera instance
	 */
	public WebcamMotionDetector(Webcam webcam) {
		this(webcam, WebcamMotionDetectorDefaultAlgorithm.DEFAULT_PIXEL_THREASHOLD);
	}

	public void start() {
		if (running.compareAndSet(false, true)) {
			webcam.open();
			executor.submit(new Runner());
			executor.submit(new Inverter());
		}
	}

	public void stop() {
		if (running.compareAndSet(true, false)) {
			webcam.close();
			executor.shutdownNow();
		}
	}

	protected void detect() {

		if (!webcam.isOpen()) {
			motion = false;
			return;
		}

		BufferedImage currentOriginal = webcam.getImage();

		if (currentOriginal == null) {
			motion = false;
			return;
		}

		final BufferedImage currentFiltered = algorithm.filter(currentOriginal);
		final boolean motionDetected = algorithm.detect(previousFiltered, currentFiltered);

		if (motionDetected) {
			motion = true;
			lastMotionTimestamp = System.currentTimeMillis();
			notifyMotionListeners(currentOriginal);
		}

		previousOriginal = currentOriginal;
		previousFiltered = currentFiltered;
	}

	/**
	 * Will notify all attached motion listeners.
	 * 
	 * @param image with the motion detected
	 */
	private void notifyMotionListeners(BufferedImage currentOriginal) {
		WebcamMotionEvent wme = new WebcamMotionEvent(this, previousOriginal, currentOriginal, algorithm.getArea(), algorithm.getCog(), algorithm.getPoints());
		for (WebcamMotionListener l : listeners) {
			try {
				l.motionDetected(wme);
			} catch (Exception e) {
				WebcamExceptionHandler.handle(e);
			}
		}
	}

	/**
	 * Add motion listener.
	 *
	 * @param l listener to add
	 * @return true if listeners list has been changed, false otherwise
	 */
	public boolean addMotionListener(WebcamMotionListener l) {
		return listeners.add(l);
	}

	/**
	 * @return All motion listeners as array
	 */
	public WebcamMotionListener[] getMotionListeners() {
		return listeners.toArray(new WebcamMotionListener[listeners.size()]);
	}

	/**
	 * Removes motion listener.
	 *
	 * @param l motion listener to remove
	 * @return true if listener was available on the list, false otherwise
	 */
	public boolean removeMotionListener(WebcamMotionListener l) {
		return listeners.remove(l);
	}

	/**
	 * @return Motion check interval in milliseconds
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * Motion check interval in milliseconds. After motion is detected, it's valid for time which is
	 * equal to value of 2 * interval.
	 *
	 * @param interval the new motion check interval (ms)
	 * @see #DEFAULT_INTERVAL
	 */
	public void setInterval(int interval) {

		if (interval < 100) {
			throw new IllegalArgumentException("Motion check interval cannot be less than 100 ms");
		}

		this.interval = interval;
	}

	/**
	 * Sets pixelThreshold to the underlying detector algorithm, but only if the algorithm is (or
	 * extends) WebcamMotionDetectorDefaultAlgorithm
	 * 
	 * @see WebcamMotionDetectorDefaultAlgorithm#setPixelThreshold(int)
	 * 
	 * @param threshold the pixel intensity difference threshold
	 */
	public void setPixelThreshold(int threshold) {
		((WebcamMotionDetectorDefaultAlgorithm) algorithm).setPixelThreshold(threshold);
	}

	/**
	 * Sets areaThreshold to the underlying detector algorithm, but only if the algorithm is (or
	 * extends) WebcamMotionDetectorDefaultAlgorithm
	 * 
	 * @see WebcamMotionDetectorDefaultAlgorithm#setAreaThreshold(double)
	 * 
	 * @param threshold the percentage fraction of image area
	 */
	public void setAreaThreshold(double threshold) {
		((WebcamMotionDetectorDefaultAlgorithm) algorithm).setAreaThreshold(threshold);
	}

	/**
	 * Set motion inertia (time when motion is valid). If no value specified this is set to 2 *
	 * interval. To reset to default value, {@link #clearInertia()} method must be used.
	 *
	 * @param inertia the motion inertia time in milliseconds
	 * @see #clearInertia()
	 */
	public void setInertia(int inertia) {
		if (inertia < 0) {
			throw new IllegalArgumentException("Inertia time must not be negative!");
		}
		this.inertia = inertia;
	}

	/**
	 * Reset inertia time to value calculated automatically on the base of interval. This value will
	 * be set to 2 * interval.
	 */
	public void clearInertia() {
		this.inertia = -1;
	}

	/**
	 * Get attached webcam object.
	 *
	 * @return Attached webcam
	 */
	public Webcam getWebcam() {
		return webcam;
	}

	public boolean isMotion() {
		if (!running.get()) {
			LOG.warn("Motion cannot be detected when detector is not running!");
		}
		return motion;
	}

	/**
	 * Get percentage fraction of image covered by motion. 0 means no motion on image and 100 means
	 * full image covered by spontaneous motion.
	 *
	 * @return Return percentage image fraction covered by motion
	 */
	public double getMotionArea() {
		return algorithm.getArea();
	}

	/**
	 * Get motion center of gravity. When no motion is detected this value points to the image
	 * center.
	 *
	 * @return Center of gravity point
	 */
	public Point getMotionCog() {
		Point cog = algorithm.getCog();
		if (cog == null) {
			// detectorAlgorithm hasn't been called so far - get image center
			int w = webcam.getViewSize().width;
			int h = webcam.getViewSize().height;
			cog = new Point(w / 2, h / 2);
		}
		return cog;
	}

	/**
	 * @return the detectorAlgorithm
	 */
	public WebcamMotionDetectorAlgorithm getDetectorAlgorithm() {
		return algorithm;
	}

	public void setMaxMotionPoints(int i) {
		algorithm.setMaxPoints(i);
	}

	public int getMaxMotionPoints() {
		return algorithm.getMaxPoints();
	}

	public void setPointRange(int i) {
		algorithm.setPointRange(i);
	}

	public int getPointRange() {
		return algorithm.getPointRange();
	}
}
