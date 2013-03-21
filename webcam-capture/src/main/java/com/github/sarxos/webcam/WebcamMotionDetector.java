package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.util.jh.JHBlurFilter;
import com.github.sarxos.webcam.util.jh.JHGrayFilter;


/**
 * Webcam motion detector.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamMotionDetector {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamMotionDetector.class);

	public static final int DEFAULT_THREASHOLD = 25;

	/**
	 * Create new threads for detector internals.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private static final class DetectorThreadFactory implements ThreadFactory {

		private static int number = 0;

		@Override
		public Thread newThread(Runnable runnable) {
			Thread t = new Thread(runnable, "motion-detector-" + (++number));
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
			running = true;
			while (running && webcam.isOpen()) {
				detect();
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Change motion to false after specified number of seconds.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	private class Changer implements Runnable {

		@Override
		public void run() {
			int time = inertia == 0 ? interval + interval / 2 : inertia;
			LOG.debug("Motion change has been sheduled in " + time + "ms");
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			synchronized (mutex) {
				motion = false;
			}
		}
	}

	private List<WebcamMotionListener> listeners = new ArrayList<WebcamMotionListener>();

	private Object mutex = new Object();

	private boolean running = false;

	/**
	 * Is motion?
	 */
	private boolean motion = false;

	/**
	 * Previously captured image.
	 */
	private BufferedImage previous = null;

	/**
	 * Webcam to be used to detect motion.
	 */
	private Webcam webcam = null;

	/**
	 * Motion check interval (1000 ms by default).
	 */
	private int interval = 1000;

	/**
	 * Pixel intensity threshold (0 - 255).
	 */
	private int threshold = 10;

	/**
	 * How long motion is valid.
	 */
	private int inertia = 10000;

	/**
	 * Motion strength (0 = no motion).
	 */
	private int strength = 0;

	/**
	 * Blur filter instance.
	 */
	private JHBlurFilter blur = new JHBlurFilter(3, 3, 1);

	/**
	 * Grayscale filter instance.
	 */
	private JHGrayFilter gray = new JHGrayFilter();

	/**
	 * Thread factory.
	 */
	private ThreadFactory threadFactory = new DetectorThreadFactory();

	/**
	 * Executor.
	 */
	private ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

	/**
	 * Create motion detector. Will open webcam if it is closed.
	 * 
	 * @param webcam web camera instance
	 * @param threshold intensity threshold (0 - 255)
	 * @param inertia for how long motion is valid (seconds)
	 */
	public WebcamMotionDetector(Webcam webcam, int threshold, int inertia) {
		this.webcam = webcam;
		this.threshold = threshold;
		this.inertia = inertia;
	}

	/**
	 * Create motion detector with default parameter inertia = 0.
	 * 
	 * @param webcam web camera instance
	 * @param threshold intensity threshold (0 - 255)
	 */
	public WebcamMotionDetector(Webcam webcam, int threshold) {
		this(webcam, threshold, 0);
	}

	/**
	 * Create motion detector with default parameters - threshold = 25, inertia
	 * = 0.
	 * 
	 * @param webcam web camera instance
	 */
	public WebcamMotionDetector(Webcam webcam) {
		this(webcam, DEFAULT_THREASHOLD, 0);
	}

	public void start() {
		if (!webcam.isOpen()) {
			webcam.open();
		}
		LOG.debug("Starting motion detector");
		executor.submit(new Runner());
	}

	public void stop() {
		running = false;
		if (webcam.isOpen()) {
			webcam.close();
		}
	}

	protected void detect() {

		if (LOG.isDebugEnabled()) {
			LOG.debug(WebcamMotionDetector.class.getSimpleName() + ".detect()");
		}

		if (motion) {
			LOG.debug("Motion detector still in inertia state, no need to check");
			return;
		}

		BufferedImage current = webcam.getImage();

		current = blur.filter(current, null);
		current = gray.filter(current, null);

		if (previous != null) {

			int w = current.getWidth();
			int h = current.getHeight();

			int strength = 0;

			synchronized (mutex) {
				for (int i = 0; i < w; i++) {
					for (int j = 0; j < h; j++) {

						int c = current.getRGB(i, j);
						int p = previous.getRGB(i, j);

						int rgb = combinePixels(c, p);

						int cr = (rgb & 0x00ff0000) >> 16;
						int cg = (rgb & 0x0000ff00) >> 8;
						int cb = (rgb & 0x000000ff);

						int max = Math.max(Math.max(cr, cg), cb);

						if (max > threshold) {

							if (!motion) {
								executor.submit(new Changer());
								motion = true;
							}

							strength++; // unit = 1 / px^2
						}
					}
				}

				this.strength = strength;

				if (motion) {
					notifyMotionListeners();
				}
			}
		}

		previous = current;
	}

	/**
	 * Will notify all attached motion listeners.
	 */
	private void notifyMotionListeners() {
		WebcamMotionEvent wme = new WebcamMotionEvent(this, strength);
		for (WebcamMotionListener l : listeners) {
			try {
				l.motionDetected(wme);
			} catch (Exception e) {
				e.printStackTrace();
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

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public Webcam getWebcam() {
		return webcam;
	}

	public boolean isMotion() {
		if (!running) {
			LOG.warn("Motion cannot be detected when detector is not running!");
		}
		return motion;
	}

	public int getMotionStrength() {
		return strength;
	}

	private static int combinePixels(int rgb1, int rgb2) {

		int a1 = (rgb1 >> 24) & 0xff;
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = rgb1 & 0xff;
		int a2 = (rgb2 >> 24) & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = rgb2 & 0xff;

		r1 = clamp(Math.abs(r1 - r2));
		g1 = clamp(Math.abs(g1 - g2));
		b1 = clamp(Math.abs(b1 - b2));

		if (a1 != 0xff) {
			a1 = a1 * 0xff / 255;
			int a3 = (255 - a1) * a2 / 255;
			r1 = clamp((r1 * a1 + r2 * a3) / 255);
			g1 = clamp((g1 * a1 + g2 * a3) / 255);
			b1 = clamp((b1 * a1 + b2 * a3) / 255);
			a1 = clamp(a1 + a3);
		}

		return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
	}

	/**
	 * Clamp a value to the range 0..255
	 */
	private static int clamp(int c) {
		if (c < 0) {
			return 0;
		}
		if (c > 255) {
			return 255;
		}
		return c;
	}
}
