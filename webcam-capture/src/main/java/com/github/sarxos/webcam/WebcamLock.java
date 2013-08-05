package com.github.sarxos.webcam;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used as a global (system) lock preventing other processes from
 * using the same camera while it's open. Whenever webcam is open there is a
 * thread running in background which updates the lock once per 2 seconds. Lock
 * is being released whenever webcam is either closed or completely disposed.
 * Lock will remain for at least 2 seconds in case when JVM has not been
 * gracefully terminated (due to SIGSEGV, SIGTERM, etc).
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
	public static final long INTERVAL = 2000;

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
				if (disabled.get()) {
					return;
				}
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

	/**
	 * Updater thread. It will update the lock value in fixed interval.
	 */
	private Thread updater = null;

	/**
	 * Is webcam locked (local, not cross-VM variable).
	 */
	private AtomicBoolean locked = new AtomicBoolean(false);

	/**
	 * Is lock completely disabled.
	 */
	private AtomicBoolean disabled = new AtomicBoolean(false);

	/**
	 * Lock file.
	 */
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
		this.lock.deleteOnExit();
	}

	private String getLockName() {
		return String.format(".webcam-lock-%d", Math.abs(webcam.getName().hashCode()));
	}

	private void write(long value) {

		if (disabled.get()) {
			return;
		}

		String name = getLockName();

		File tmp = null;
		DataOutputStream dos = null;

		try {

			tmp = File.createTempFile(String.format("%s-tmp", name), "");
			tmp.deleteOnExit();

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

			// atomic rename operation can fail (mostly on Windows), so we
			// simply jump out the method if it succeed, or try to rewrite
			// content using streams if it fail

			return;
		} else {

			// create lock file if not exist

			if (!lock.exists()) {
				try {
					if (lock.createNewFile()) {
						LOG.info("Lock file {} for {} has been created", lock, webcam);
					} else {
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

			synchronized (webcam) {
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

		if (disabled.get()) {
			return -1;
		}

		DataInputStream dis = null;

		long value = -1;
		boolean broken = false;

		synchronized (webcam) {

			try {
				value = (dis = new DataInputStream(new FileInputStream(lock))).readLong();
			} catch (EOFException e) {
				LOG.debug("Webcam lock is broken - EOF when reading long variable from stream", e);
				broken = true;
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

			if (broken) {
				LOG.warn("Lock file {} for {} is broken - recreating it", lock, webcam);
				write(-1);
			}
		}

		return value;
	}

	private void update() {

		if (disabled.get()) {
			return;
		}

		write(System.currentTimeMillis());
	}

	/**
	 * Lock webcam.
	 */
	public void lock() {

		if (disabled.get()) {
			return;
		}

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
	 * Completely disable locking mechanism. After this method is invoked, the
	 * lock will not have any effect on the webcam runtime.
	 */
	public void disable() {
		if (disabled.compareAndSet(false, true)) {
			LOG.info("Locking mechanism has been disabled in {}", webcam);
			if (updater != null) {
				updater.interrupt();
			}
		}
	}

	/**
	 * Unlock webcam.
	 */
	public void unlock() {

		// do nothing when lock disabled

		if (disabled.get()) {
			return;
		}

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

		// always return false when lock is disabled

		if (disabled.get()) {
			return false;
		}

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
