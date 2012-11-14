package com.github.sarxos.webcam.ds.ipcam;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sarxos.webcam.WebcamDevice;
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
	private static final Set<IpCamDevice> DEVICES = new HashSet<IpCamDevice>();

	/**
	 * Register IP camera.
	 * 
	 * @param ipcam the IP camera to be register
	 */
	public static void register(IpCamDevice ipcam) {
		for (WebcamDevice d : DEVICES) {
			String name = ipcam.getName();
			if (d.getName().equals(name)) {
				throw new WebcamException(String.format("Name '%s' is already in use", name));
			}
		}
		DEVICES.add(ipcam);
	}

	public static void register(String name, URL url, IpCamMode mode) {
		register(new IpCamDevice(name, url, mode));
	}

	public static boolean isRegistered(IpCamDevice ipcam) {
		for (IpCamDevice d : DEVICES) {
			if (d.getName().equals(ipcam.getName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isRegistered(String name) {
		for (IpCamDevice d : DEVICES) {
			if (d.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isRegistered(URL url) {
		for (IpCamDevice d : DEVICES) {
			if (d.getURL().equals(url)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Unregister IP camera.
	 * 
	 * @param ipcam the IP camera to be unregister
	 */
	public static void unregister(IpCamDevice ipcam) {
		DEVICES.remove(ipcam);
	}

	/**
	 * Get all registered IP cameras.
	 * 
	 * @return Collection of registered IP cameras
	 */
	public static List<IpCamDevice> getIpCameras() {
		return new ArrayList<IpCamDevice>(DEVICES);
	}
}
