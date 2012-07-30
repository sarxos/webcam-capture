package com.github.sarxos.webcam.ds.ipcam.http;

/**
 * HTTP authentication type.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public enum IpCamAuth {

	/**
	 * No authentication.
	 */
	NONE,

	/**
	 * Basic auth.
	 */
	BASIC,

	/**
	 * WSSE auth.
	 */
	WSSE,

}
