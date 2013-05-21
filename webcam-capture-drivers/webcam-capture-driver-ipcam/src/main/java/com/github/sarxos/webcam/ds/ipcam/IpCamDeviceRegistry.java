package com.github.sarxos.webcam.ds.ipcam;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoveryService;
import com.github.sarxos.webcam.WebcamException;


/**
 * Class used to register IP camera devices.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class IpCamDeviceRegistry {

	/**
	 * Contains IP cameras.
	 */
	private static final List<IpCamDevice> DEVICES = new ArrayList<IpCamDevice>();

	/**
	 * Register IP camera.
	 * 
	 * @param ipcam the IP camera to be register
	 */
	public static IpCamDevice register(IpCamDevice ipcam) {

		for (WebcamDevice d : DEVICES) {
			String name = ipcam.getName();
			if (d.getName().equals(name)) {
				throw new WebcamException(String.format("Webcam with name '%s' is already registered", name));
			}
		}

		DEVICES.add(ipcam);

		rescan();

		return ipcam;
	}

	public static IpCamDevice register(String name, String url, IpCamMode mode) throws MalformedURLException {
		return register(new IpCamDevice(name, url, mode));
	}

	public static IpCamDevice register(String name, URL url, IpCamMode mode) {
		return register(new IpCamDevice(name, url, mode));
	}

	public static IpCamDevice register(String name, String url, IpCamMode mode, IpCamAuth auth) throws MalformedURLException {
		return register(new IpCamDevice(name, url, mode, auth));
	}

	/**
	 * Register new IP camera device.
	 * 
	 * @param name the name of the device
	 * @param url the URL to be used
	 * @param mode the camera mode to be used
	 * @param auth the optional settings if device supports authentication
	 * @return Return newly created IP camera device object
	 */
	public static IpCamDevice register(String name, URL url, IpCamMode mode, IpCamAuth auth) {
		return register(new IpCamDevice(name, url, mode, auth));
	}

	/**
	 * Is device registered?
	 * 
	 * @param ipcam the IP camera device
	 * @return True if device is registsred, false otherwise
	 */
	public static boolean isRegistered(IpCamDevice ipcam) {

		if (ipcam == null) {
			throw new IllegalArgumentException("IP camera device cannot be null");
		}

		Iterator<IpCamDevice> di = DEVICES.iterator();
		while (di.hasNext()) {
			if (di.next().getName().equals(ipcam.getName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Is device with given name registered?
	 * 
	 * @param name the name of device
	 * @return True if device is registered, false otherwise
	 */
	public static boolean isRegistered(String name) {

		if (name == null) {
			throw new IllegalArgumentException("Device name cannot be null");
		}

		Iterator<IpCamDevice> di = DEVICES.iterator();
		while (di.hasNext()) {
			if (di.next().getName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Is device with given URL registered?
	 * 
	 * @param url the URL used by device
	 * @return True if device is registered, false otherwise
	 */
	public static boolean isRegistered(URL url) {

		if (url == null) {
			throw new IllegalArgumentException("Camera device URL cannot be null");
		}

		try {
			return isRegistered(url.toURI());
		} catch (URISyntaxException e) {
			throw new WebcamException(e);
		}
	}

	/**
	 * Is device with given URL registered?
	 * 
	 * @param url the URL used by device
	 * @return True if device is registered, false otherwise
	 */
	public static boolean isRegistered(URI uri) {

		if (uri == null) {
			throw new IllegalArgumentException("Camera device URI cannot be null");
		}

		for (IpCamDevice d : DEVICES) {

			// URL.euals() method is broken and thus we shall not depend on
			// it, the best w/a is to use URI instead

			try {
				if (d.getURL().toURI().equals(uri)) {
					return true;
				}
			} catch (URISyntaxException e) {
				throw new WebcamException(e);
			}
		}

		return false;
	}

	/**
	 * Unregister IP camera.
	 * 
	 * @param ipcam the IP camera to be unregister
	 */
	public static boolean unregister(IpCamDevice ipcam) {
		try {
			return DEVICES.remove(ipcam);
		} finally {
			rescan();
		}
	}

	/**
	 * Run discovery service once if device has been removed to trigger
	 * disconnected webcam discovery event and keep webcams list up-to-date.
	 */
	private static void rescan() {
		WebcamDiscoveryService discovery = Webcam.getDiscoveryServiceRef();
		if (discovery != null) {
			discovery.scan();
		}
	}

	/**
	 * Unregister IP camera with given name.
	 * 
	 * @param ipcam the IP camera to be unregister
	 */
	public static boolean unregister(String name) {
		Iterator<IpCamDevice> di = DEVICES.iterator();
		while (di.hasNext()) {
			IpCamDevice d = di.next();
			if (d.getName().equals(name)) {
				di.remove();
				rescan();
				return true;
			}
		}
		return false;
	}

	/**
	 * Get all registered IP cameras.
	 * 
	 * @return Collection of registered IP cameras
	 */
	public static List<IpCamDevice> getIpCameras() {
		return Collections.unmodifiableList(DEVICES);
	}

	/**
	 * Removes all registered devices.
	 */
	public static void unregisterAll() {
		DEVICES.clear();
		rescan();
	}
}
