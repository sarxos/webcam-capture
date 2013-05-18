package com.github.sarxos.webcam.ds.ipcam;

import java.util.Collections;
import java.util.List;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;


/**
 * IP camera driver.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class IpCamDriver implements WebcamDriver {

	public IpCamDriver() {
		this(null);
	}

	public IpCamDriver(IpCamStorage storage) {
		if (storage != null) {
			storage.open();
		}
	}

	@Override
	public List<WebcamDevice> getDevices() {
		return Collections.unmodifiableList((List<? extends WebcamDevice>) IpCamDeviceRegistry.getIpCameras());
	}

	public void register(IpCamDevice device) {
		IpCamDeviceRegistry.register(device);
	}

	public void unregister(IpCamDevice device) {
		IpCamDeviceRegistry.unregister(device);
	}

	@Override
	public boolean isThreadSafe() {
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
