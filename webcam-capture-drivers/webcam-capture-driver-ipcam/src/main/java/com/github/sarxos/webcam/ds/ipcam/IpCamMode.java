package com.github.sarxos.webcam.ds.ipcam;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * How to obtain new images from IP cameras.
 * 
 * @author Bartosz Firyn (SarXos)
 */
@XmlEnum
public enum IpCamMode {

	/**
	 * Device will pull image from IP camera.
	 */
	@XmlEnumValue("pull")
	PULL,

	/**
	 * IP camera HTTP server will push new image to the device.
	 */
	@XmlEnumValue("pull")
	PUSH,

}
