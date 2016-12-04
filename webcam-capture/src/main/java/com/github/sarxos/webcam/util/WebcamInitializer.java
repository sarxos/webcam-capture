package com.github.sarxos.webcam.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.webcam.WebcamException;


/**
 * Atomic webcam initializer.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamInitializer {

	private final Initializable initializable;
	private final AtomicBoolean initialized = new AtomicBoolean(false);
	private final CountDownLatch latch = new CountDownLatch(1);

	public WebcamInitializer(Initializable initializable) {
		this.initializable = initializable;
	}

	public void initialize() {
		if (initialized.compareAndSet(false, true)) {
			try {
				initializable.initialize();
			} catch (Exception e) {
				throw new WebcamException(e);
			} finally {
				latch.countDown();
			}
		} else {
			try {
				latch.await();
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public void teardown() {
		if (initialized.compareAndSet(true, false)) {
			try {
				initializable.teardown();
			} catch (Exception e) {
				throw new WebcamException(e);
			}
		}
	}

	public boolean isInitialized() {
		return initialized.get();
	}
}
