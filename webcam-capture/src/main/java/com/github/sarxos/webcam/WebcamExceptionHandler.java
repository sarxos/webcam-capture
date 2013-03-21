package com.github.sarxos.webcam;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebcamExceptionHandler implements UncaughtExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamExceptionHandler.class);

	private static final WebcamExceptionHandler INSTANCE = new WebcamExceptionHandler();

	private WebcamExceptionHandler() {
		// singleton
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOG.error(String.format("Exception in thread %s", t.getName()), e);
		System.err.println(String.format("Exception in thread %s", t.getName()));
		e.printStackTrace();
	}

	protected static final WebcamExceptionHandler getInstance() {
		return INSTANCE;
	}
}
