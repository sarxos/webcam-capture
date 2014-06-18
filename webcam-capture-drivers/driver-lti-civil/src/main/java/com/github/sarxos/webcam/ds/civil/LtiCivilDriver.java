package com.github.sarxos.webcam.ds.civil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamException;
import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;


public class LtiCivilDriver implements WebcamDriver {

	// load civil DLL
	static {

	}

	private static final Logger LOG = LoggerFactory.getLogger(LtiCivilDriver.class);

	private static CaptureSystemFactory factory = null;
	private static CaptureSystem system = null;

	private static volatile boolean ready = false;
	private static final AtomicBoolean INIT = new AtomicBoolean(false);

	private static void initialize() {

		if (!INIT.compareAndSet(false, true)) {
			return;
		}

		LtiCivilLoader.load("civil");

		factory = DefaultCaptureSystemFactorySingleton.instance();

		try {
			system = factory.createCaptureSystem();
			system.init();

			ready = true;

		} catch (UnsatisfiedLinkError e) {
			// ignore - it is already loaded
			LOG.debug("Library already loaded");
		} catch (CaptureException e) {
			throw new WebcamException(e);
		}
	}

	protected static CaptureSystem getCaptureSystem() {
		initialize();
		return system;
	}

	@Override
	public List<WebcamDevice> getDevices() {

		initialize();

		int i = 0;
		while (!ready) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return null;
			}

			// wait max 10 seconds for driver to be ready
			if (i++ > 100) {
				throw new RuntimeException("Cannot get devices because capture driver has not become ready for 10 seconds");
			}
		}

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		try {
			for (Object cdi : system.getCaptureDeviceInfoList()) {
				devices.add(new LtiCivilDevice((CaptureDeviceInfo) cdi));
			}
		} catch (CaptureException e) {
			throw new WebcamException(e);
		}

		return devices;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
