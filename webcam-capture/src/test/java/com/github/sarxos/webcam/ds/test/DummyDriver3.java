package com.github.sarxos.webcam.ds.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;


public class DummyDriver3 implements WebcamDriver {

	private static final List<WebcamDevice> DEVICES = new ArrayList<WebcamDevice>(Arrays.asList(
			new DummyDevice(),
			new DummyDevice(),
			new DummyDevice(),
			new DummyDevice()
	));

	private static DummyDriver3 instance = null;

	public DummyDriver3() {
		if (instance == null) {
			instance = this;
		}
	}

	public static DummyDriver3 getInstance() {
		return instance;
	}

	@Override
	public List<WebcamDevice> getDevices() {
		return DEVICES;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

}
