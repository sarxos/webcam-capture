package com.github.sarxos.webcam;

import java.awt.Point;
import java.util.EventObject;


/**
 * Webcam detected motion event.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamMotionEvent extends EventObject {

	private static final long serialVersionUID = -7245768099221999443L;

	private final double strength;
	private final Point cog;

	/**
	 * Create detected motion event.
	 *
	 * @param detector
	 * @param strength
	 * @param cog center of motion gravity
	 */
	public WebcamMotionEvent(WebcamMotionDetector detector, double strength, Point cog) {

		super(detector);

		this.strength = strength;
		this.cog = cog;
	}

	/**
	 * Get percentage fraction of image covered by motion. 0 is no motion on image, and 100 is full
	 * image covered by motion.
	 *
	 * @return Motion area
	 */
	public double getArea() {
		return strength;
	}

	public Point getCog() {
		return cog;
	}
}
