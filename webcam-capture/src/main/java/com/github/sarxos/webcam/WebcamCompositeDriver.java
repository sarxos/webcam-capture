package com.github.sarxos.webcam;

import java.util.ArrayList;
import java.util.List;


public class WebcamCompositeDriver implements WebcamDriver {

	private List<WebcamDriver> drivers = new ArrayList<WebcamDriver>();

	public WebcamCompositeDriver(WebcamDriver... drivers) {
		for (WebcamDriver driver : drivers) {
			this.drivers.add(driver);
		}
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
}
