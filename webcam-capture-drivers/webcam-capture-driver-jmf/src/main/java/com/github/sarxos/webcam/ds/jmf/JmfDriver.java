package com.github.sarxos.webcam.ds.jmf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;

import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamDevice;


public class JmfDriver implements WebcamDriver {

	private static List<WebcamDevice> devices = null;

	public List<WebcamDevice> getDevices() {

		if (devices == null) {

			devices = new ArrayList<WebcamDevice>();

			@SuppressWarnings("unchecked")
			Vector<Object> cdis = CaptureDeviceManager.getDeviceList(new Format("RGB"));
			Iterator<Object> di = cdis.iterator();

			while (di.hasNext()) {
				CaptureDeviceInfo cdi = (CaptureDeviceInfo) di.next();
				devices.add(new JmfDevice(cdi));
			}
		}

		return devices;
	}
}
