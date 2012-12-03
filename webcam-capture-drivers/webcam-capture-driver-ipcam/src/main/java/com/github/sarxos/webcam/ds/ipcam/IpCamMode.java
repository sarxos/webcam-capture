package com.github.sarxos.webcam.ds.ipcam;

/**
 * How to obtain new images from IP cameras.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public enum IpCamMode {

	/**
	 * Device will pull image from IP camera.
	 */
	PULL,

	/**
	 * IP camera HTTP server will push new image to the device.
	 */
	PUSH,

}
