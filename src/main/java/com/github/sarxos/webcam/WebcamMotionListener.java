package com.github.sarxos.webcam;

/**
 * Motion listener used to signal motion detection.
 * 
 * @author bartosz Firyn (SarXos)
 */
public interface WebcamMotionListener {

	/**
	 * Will be called after motion is detected.
	 * 
	 * @param wme motion event
	 */
	public void motionDetected(WebcamMotionEvent wme);

}
