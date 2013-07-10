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

	private static final Object MUTEX = new Object();

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
			throw new RuntimeException(e);
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (!locked.get()) {
			return;
		}

		if (tmp.renameTo(lock)) {

			// rename operation can fail (mostly on Windows), so we simply jump
			// out the method if it succeed, or try to rewrite content using
			// streams if it fail

			return;
		} else {

			// create lock file if not exist

			if (!lock.exists()) {
				try {
					if (!lock.createNewFile()) {
						throw new RuntimeException("Not able to create file " + lock);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			FileOutputStream fos = null;
			FileInputStream fis = null;

			int k = 0;
			int n = -1;
			byte[] buffer = new byte[8];
			boolean rewritten = false;

			// rewrite temporary file content to lock, try max 5 times

			synchronized (MUTEX) {
				do {
					try {
						fos = new FileOutputStream(lock);
						fis = new FileInputStream(tmp);
						while ((n = fis.read(buffer)) != -1) {
							fos.write(buffer, 0, n);
						}
						rewritten = true;
					} catch (IOException e) {
						LOG.debug("Not able to rewrite lock file", e);
					} finally {
						if (fos != null) {
							try {
								fos.close();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						if (fis != null) {
							try {
								fis.close();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
					if (rewritten) {
						break;
					}
				} while (k++ < 5);
			}

			if (!rewritten) {
				throw new WebcamException("Not able to write lock file");
			}

			// remove temporary file

			if (!tmp.delete()) {
				tmp.deleteOnExit();
			}
		}

	}

	private long read() {
		DataInputStream dis = null;
		try {
			return (dis = new DataInputStream(new FileInputStream(lock))).readLong();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
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
