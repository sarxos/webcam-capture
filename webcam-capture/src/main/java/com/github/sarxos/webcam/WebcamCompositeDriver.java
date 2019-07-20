package com.github.sarxos.webcam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class WebcamCompositeDriver implements WebcamDriver, WebcamDiscoverySupport {

	private List<WebcamDriver> drivers = new ArrayList<WebcamDriver>();

	private int scanInterval = -1;

	public WebcamCompositeDriver(WebcamDriver... drivers) {
		this.drivers.addAll(Arrays.asList(drivers));
	}

	public void add(WebcamDriver driver) {
		drivers.add(driver);
	}

	public List<WebcamDriver> getDrivers() {
		return drivers;
	}

	@Override
	public List<WebcamDevice> getDevices() {
		List<WebcamDevice> all = new ArrayList<WebcamDevice>();
		for (WebcamDriver driver : drivers) {
			all.addAll(driver.getDevices());
		}
		return all;
	}

	@Override
	public boolean isThreadSafe() {
		boolean safe = true;
		for (WebcamDriver driver : drivers) {
			safe &= driver.isThreadSafe();
			if (!safe) {
				break;
			}
		}
		return safe;
	}

	public void setScanInterval(int scanInterval) {
		this.scanInterval = scanInterval;
	}

	@Override
	public long getScanInterval() {
		if (scanInterval <= 0) {
			return DEFAULT_SCAN_INTERVAL;
		}
		return scanInterval;
	}

	@Override
	public boolean isScanPossible() {
		return true;
	}
}
