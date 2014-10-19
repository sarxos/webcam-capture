package com.github.sarxos.webcam;

import java.util.List;


/**
 * Webcam drivers abstraction. The webcam driver (or capture driver, as it is
 * often referred) is a factory for specific webcam device implementations.
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
