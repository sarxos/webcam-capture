package com.github.sarxos.webcam.ds.ipcam.device.dlink;

import java.net.MalformedURLException;
import java.net.URL;

import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.ipcam.IpCamAuth;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;


/**
 * This is webcam device abstraction to handle MJPEG video stream from D-Link DSC-933L IP camera.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class DSC933L extends IpCamDevice {

	/**
	 * Path used by this camera model to expose MJPEG video feed.
	 */
	private static final String MJPEG_PATH = "video/mjpg.cgi";

	/**
	 * @param name the camera name, e.g. 'Bedroom Camera'
	 * @param url the camera address, e.g. 'http://192.168.0.12'
	 * @param user the user name configured in camera
	 * @param password the password for the user
	 * @throws MalformedURLException if camera address is invalid
	 */
	public DSC933L(String name, String url, String user, String password) throws MalformedURLException {
		this(name, url, IpCamMode.PUSH, new IpCamAuth(user, password));
	}

	private DSC933L(String name, String url, IpCamMode mode, IpCamAuth auth) throws MalformedURLException {
		super(name, url, mode, auth);
	}

	@Override
	public URL getURL() {

		final String base = super.getURL().toString();
		final String address = base + (base.endsWith("/") ? MJPEG_PATH : ("/" + MJPEG_PATH));

		try {
			return new URL(address);
		} catch (MalformedURLException e) {
			throw new WebcamException("Invalid URL " + base);
		}
	}
}
