package com.github.sarxos.webcam;

import java.util.EventObject;


/**
 * This event is generated when webcam has been found or lost.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamDiscoveryEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * Event type informing about newly connected webcam.
	 */
	public static final int ADDED = 1;

	/**
	 * Event type informing about lately disconnected webcam.
	 */
	public static final int REMOVED = 2;

	/**
	 * Event type (webcam connected / disconnected).
	 */
	private int type = -1;

	/**
	 * Create new webcam discovery event.
	 * 
	 * @param webcam the webcam which has been found or removed
	 * @param type the event type
	 * @see #ADDED
	 * @see #REMOVED
	 */
	public WebcamDiscoveryEvent(Webcam webcam, int type) {
		super(webcam);
		this.type = type;
	}

	/**
	 * Return the webcam which has been found or removed.
	 * 
	 * @return Webcam instance
	 */
	public Webcam getWebcam() {
		return (Webcam) getSource();
	}

	/**
	 * Return event type (webcam connected / disconnected)
	 * 
	 * @return Integer value
	 * @see #ADDED
	 * @see #REMOVED
	 */
	public int getType() {
		return type;
	}
}
