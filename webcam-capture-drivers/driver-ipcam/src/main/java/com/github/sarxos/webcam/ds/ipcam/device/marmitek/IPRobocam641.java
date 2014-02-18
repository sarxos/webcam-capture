package com.github.sarxos.webcam.ds.ipcam.device.marmitek;

import java.net.MalformedURLException;
import java.net.URL;

import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;


public class IPRobocam641 extends IpCamDevice {

	private URL base = null;

	public IPRobocam641(String name, String urlBase) {
		this(name, toURL(urlBase));
	}

	public IPRobocam641(String name, URL base) {
		super(name, (URL) null, IpCamMode.PUSH);
		this.base = base;
	}

	@Override
	public URL getURL() {
		String url = String.format("%s/cgi/mjpg/mjpg.cgi", base);
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new WebcamException(String.format("Incorrect URL %s", url), e);
		}
	}

}
