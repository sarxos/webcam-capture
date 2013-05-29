package com.github.sarxos.webcam;

import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used as a global (system) lock preventing other processes from
 * using the same camera while it's open.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamLock {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamLock.class);

	/**
	 * The preferences instance used to store global variables.
	 */
	private static final Preferences PREFS = Preferences.systemNodeForPackage(WebcamLock.class);;

	/**
	 * And the Webcam we will be locking.
	 */
	private final Webcam webcam;

	/**
	 * Creates global webcam lock.
	 * 
	 * @param webcam the webcam instance to be locked
	 */
	protected WebcamLock(Webcam webcam) {
		super();
		this.webcam = webcam;
	}

	/**
	 * Lock webcam.
	 */
	public void lock() {
		LOG.debug("Lock {}", webcam);
		PREFS.putBoolean(webcam.getName(), true);
	}

	/**
	 * Unlock webcam.
	 */
	public void unlock() {
		LOG.debug("Unlock {}", webcam);
		PREFS.putBoolean(webcam.getName(), false);
	}

	/**
	 * Check if webcam is locked.
	 * 
	 * @return True if webcam is locked, false otherwise
	 */
	public boolean isLocked() {
		return PREFS.getBoolean(webcam.getName(), false);
	}
}
