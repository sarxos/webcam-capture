package com.github.sarxos.webcam.ds.dummy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;


public class WebcamDummyDriver implements WebcamDriver, WebcamDiscoverySupport {

	private int count;

	public WebcamDummyDriver(int count) {
		this.count = count;
	}

	@Override
	public long getScanInterval() {
		return 10000;
	}

	@Override
	public boolean isScanPossible() {
		return true;
	}

	@Override
	public List<WebcamDevice> getDevices() {
		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		for (int i = 0; i < count; i++) {
			devices.add(new WebcamDummyDevice(i));
		}
		return Collections.unmodifiableList(devices);
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}
}
