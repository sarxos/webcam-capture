package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.ds.cgt.WebcamReadImageTask;


public class WebcamUpdater implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamUpdater.class);

	private static final int TARGET_FPS = 50;

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();;
	private final AtomicReference<BufferedImage> image = new AtomicReference<BufferedImage>();

	private Webcam webcam = null;
	private long delay = 0;
	private long time = 0;

	private volatile double fps = 0;
	private volatile boolean running = false;

	public WebcamUpdater(Webcam webcam) {
		this.webcam = webcam;
	}

	public void start() {
		running = true;
		image.set(new WebcamReadImageTask(Webcam.getDriver(), webcam.getDevice()).getImage());
		executor.execute(this);

		LOG.debug("Webcam updater has been started");
	}

	public void stop() {
		running = false;
		LOG.debug("Webcam updater has been stopped");
	}

	@Override
	public void run() {

		if (!running) {
			return;
		}

		time = System.currentTimeMillis();
		image.set(new WebcamReadImageTask(Webcam.getDriver(), webcam.getDevice()).getImage());
		time = System.currentTimeMillis() - time;

		delay = Math.max((1000 / TARGET_FPS) - time, 0);
		fps = (4 * fps + 1000 / (double) time) / 5;

		executor.schedule(this, delay, TimeUnit.MILLISECONDS);
	}

	public BufferedImage getImage() {
		while (image.get() == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return image.get();
	}

	public double getFPS() {
		return fps;
	}
}
