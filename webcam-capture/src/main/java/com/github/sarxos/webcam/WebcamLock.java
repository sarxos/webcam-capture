package com.github.sarxos.webcam;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.BackingStoreException;
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
	private static final Preferences PREFS = Preferences.userNodeForPackage(WebcamLock.class);

	/**
	 * Update interval (ms).
	 */
	private static final long INTERVAL = 2000;

	/**
	 * Used to update lock state.
	 * 
	 * @author sarxos
	 */
	private class LockUpdater extends Thread {

		public LockUpdater() {
			super();
			setName(String.format("webcam-lock-[%s]", webcam.getName()));
			setDaemon(true);
			setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
		}

		@Override
		public void run() {
			do {
				update();
				try {
					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
					LOG.debug("Lock updater has been interrupted");
					return;
				}
			} while (locked.get());
		}

	}

	/**
	 * And the Webcam we will be locking.
	 */
	private final Webcam webcam;

	private Thread updater = null;

	private AtomicBoolean locked = new AtomicBoolean(false);

	/**
	 * Creates global webcam lock.
	 * 
	 * @param webcam the webcam instance to be locked
	 */
	protected WebcamLock(Webcam webcam) {
		super();
		this.webcam = webcam;
	}

	private void update() {
		PREFS.putLong(webcam.getName(), System.currentTimeMillis());
		try {
			PREFS.flush();
		} catch (BackingStoreException e) {
			LOG.warn("Cannot flush lock preferences", e);
		}
	}

	/**
	 * Lock webcam.
	 */
	public void lock() {

		if (isLocked()) {
			throw new WebcamException(String.format("Webcam %s has been locked and cannot be open", webcam.getName()));
		}

		if (!locked.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("Lock {}", webcam);

		update();

		updater = new LockUpdater();
		updater.start();
	}

	/**
	 * Unlock webcam.
	 */
	public void unlock() {

		if (!locked.compareAndSet(true, false)) {
			return;
		}

		LOG.debug("Unlock {}", webcam);

		updater.interrupt();

		PREFS.putLong(webcam.getName(), -1);
	}

	/**
	 * Check if webcam is locked.
	 * 
	 * @return True if webcam is locked, false otherwise
	 */
	public boolean isLocked() {

		// check if locked by current process

		if (locked.get()) {
			return true;
		}

		// check if locked by other process

		long tsp = PREFS.getLong(webcam.getName(), -1);
		long now = System.currentTimeMillis();

		LOG.trace("Lock timestamp {} now {} for ", tsp, now, webcam);

		if (tsp > now - INTERVAL * 2) {
			return true;
		}

		return false;
	}

	public static void main(String[] args) {
		Webcam w = Webcam.getDefault();
		WebcamLock wl = new WebcamLock(w);

		wl.lock();
		wl.isLocked();
	}
}
