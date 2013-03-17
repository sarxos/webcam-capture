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
	void webcamOpen(WebcamEvent we);

	/**
	 * Webcam has been closed
	 * 
	 * @param we a webcam event
	 */
	void webcamClosed(WebcamEvent we);

	/**
	 * Webcam has been disposed
	 * 
	 * @param we a webcam event
	 */
	void webcamDisposed(WebcamEvent we);

	/**
	 * Webcam image has been obtained.
	 * 
	 * @param we a webcam event
	 */
	void webcamImageObtained(WebcamEvent we);
}
