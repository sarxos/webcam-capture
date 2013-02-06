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
	List<WebcamDevice> getDevices();

	/**
	 * Is driver thread-safe. Thread safe drivers operations does not have to be
	 * synchronized.
	 * 
	 * @return True in case if driver is thread-safe, false otherwise
	 */
	boolean isThreadSafe();

}
