package com.github.sarxos.webcam;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private File lock = null;

	/**
	 * Creates global webcam lock.
	 * 
	 * @param webcam the webcam instance to be locked
	 */
	protected WebcamLock(Webcam webcam) {
		super();
		this.webcam = webcam;
		this.lock = new File(System.getProperty("java.io.tmpdir"), getLockName());
	}

	private String getLockName() {
		return String.format(".webcam-lock-%d", Math.abs(webcam.getName().hashCode()));
	}

	private void write(long value) {

		String name = getLockName();

		File tmp = null;
		DataOutputStream dos = null;

		try {
			tmp = File.createTempFile(name, "");

			dos = new DataOutputStream(new FileOutputStream(tmp));
			dos.writeLong(value);
			dos.flush();

		} catch (IOException e) {
			throw new WebcamException(e);
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					throw new WebcamException(e);
				}
			}
		}

		if (!locked.get()) {
			return;
		}

		if (!tmp.renameTo(lock)) {
			LOG.warn("Ooops, system was not able to rename lock file from {} to {}", tmp, lock);
		}
	}

	private long read() {
		DataInputStream dis = null;
		try {
			return (dis = new DataInputStream(new FileInputStream(lock))).readLong();
		} catch (IOException e) {
			throw new WebcamException(e);
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					throw new WebcamException(e);
				}
			}
		}
	}

	private void update() {
		write(System.currentTimeMillis());
	}

	/**
	 * Lock webcam.
	 */
	public void lock() {

		if (isLocked()) {
			throw new WebcamLockException(String.format("Webcam %s has already been locked", webcam.getName()));
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

		write(-1);

		if (!lock.delete()) {
			lock.deleteOnExit();
		}
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

		if (!lock.exists()) {
			return false;
		}

		long now = System.currentTimeMillis();
		long tsp = read();

		LOG.trace("Lock timestamp {} now {} for {}", tsp, now, webcam);

		if (tsp > now - INTERVAL * 2) {
			return true;
		}

		return false;
	}
}
