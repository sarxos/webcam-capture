package com.github.sarxos.webcam;

import java.util.List;


/**
 * This is interface for all webcam drivers.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public interface WebcamDriver {

	/**
	 * Return all registered webcam devices.
	 * 
	 * @return List of webcam devices
	 */
	public List<WebcamDevice> getDevices();

}
