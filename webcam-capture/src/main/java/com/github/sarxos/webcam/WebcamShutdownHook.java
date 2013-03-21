package com.github.sarxos.webcam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Shutdown hook to be executed when JVM exits gracefully. This class intention
 * is to be used internally only.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public final class WebcamShutdownHook extends Thread {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamShutdownHook.class);

	/**
	 * Number of shutdown hook instance.
	 */
	private static int number = 0;

	/**
	 * Webcam instance to be disposed / closed.
	 */
	private Webcam webcam = null;

	/**
	 * Create new shutdown hook instance.
	 * 
	 * @param webcam the webcam for which hook is intended
	 */
	protected WebcamShutdownHook(Webcam webcam) {
		super("shutdown-hook-" + (++number));
		this.webcam = webcam;
		this.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
	}

	@Override
	public void run() {
		LOG.info("Automatic {} deallocation", webcam.getName());
		webcam.dispose();
	}
}
