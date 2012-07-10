package com.github.sarxos.webcam;

import java.util.EventObject;


/**
 * Webcam detected motion event.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamMotionEvent extends EventObject {

	private static final long serialVersionUID = -7245768099221999443L;

	private int strength = 0;

	/**
	 * Create detected motion event.
	 * 
	 * @param detector
	 * @param strength
	 */
	public WebcamMotionEvent(WebcamMotionDetector detector, int strength) {
		super(detector);
		this.strength = strength;
	}

	/**
	 * @return Motion strength
	 */
	public int getStrength() {
		return strength;
	}
}
