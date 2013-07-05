package com.github.sarxos.webcam;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;


/**
 * Used internally.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamExceptionHandler implements UncaughtExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamExceptionHandler.class);

	private static final WebcamExceptionHandler INSTANCE = new WebcamExceptionHandler();

	private WebcamExceptionHandler() {
		// singleton
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		Object context = LoggerFactory.getILoggerFactory();
		if (context instanceof NOPLoggerFactory) {
			System.err.println(String.format("Exception in thread %s", t.getName()));
			e.printStackTrace();
		} else {
			LOG.error(String.format("Exception in thread %s", t.getName()), e);
		}
	}

	public static void handle(Throwable e) {
		INSTANCE.uncaughtException(Thread.currentThread(), e);
	}

	public static final WebcamExceptionHandler getInstance() {
		return INSTANCE;
	}
}
