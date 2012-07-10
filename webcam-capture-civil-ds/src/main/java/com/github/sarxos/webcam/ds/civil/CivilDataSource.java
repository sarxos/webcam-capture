package com.github.sarxos.webcam.ds.civil;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDataSource;
import com.github.sarxos.webcam.WebcamDevice;
import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureSystem;
import com.lti.civil.CaptureSystemFactory;
import com.lti.civil.DefaultCaptureSystemFactorySingleton;


public class CivilDataSource implements WebcamDataSource {

	private static final Logger LOG = LoggerFactory.getLogger(CivilDataSource.class);

	private static List<WebcamDevice> devices = null;
	private static CaptureSystemFactory factory = null;
	private static CaptureSystem system = null;
	private static boolean initialized = false;

	private static void initialize() {
		factory = DefaultCaptureSystemFactorySingleton.instance();
		try {
			system = factory.createCaptureSystem();
			system.init();
		} catch (CaptureException e) {
			LOG.error("Capture exception", e);
		}
	}

	protected static CaptureSystem getCaptureSystem() {
		if (!initialized) {
			initialize();
		}
		return system;
	}

	public List<WebcamDevice> getDevices() {

		if (!initialized) {
			initialize();
		}

		if (devices == null) {
			devices = new ArrayList<WebcamDevice>();
			try {

				@SuppressWarnings("unchecked")
				List<CaptureDeviceInfo> infos = system.getCaptureDeviceInfoList();

				for (CaptureDeviceInfo cdi : infos) {
					devices.add(new CivilDevice(cdi));
				}
			} catch (CaptureException e) {
				e.printStackTrace();
			}
		}

		return devices;
	}
}
