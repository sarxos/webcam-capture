package com.github.sarxos.webcam;

/**
 * Webcam listener.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public interface WebcamListener {

	/**
	 * Webcam has been open.
	 * 
	 * @param we a webcam event
	 */
	public void webcamOpen(WebcamEvent we);

	/**
	 * Webcam has been closed
	 * 
	 * @param we a webcam event
	 */
	public void webcamClosed(WebcamEvent we);

}
