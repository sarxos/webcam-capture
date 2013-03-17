package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.util.EventObject;


/**
 * Webcam event.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * Image acquired from webcam
	 */
	private BufferedImage image = null;

	/**
	 * Event type.
	 */
	private WebcamEventType type = null;

	/**
	 * Webcam event.
	 * 
	 * @param type the event type
	 * @param w the webcam object
	 */
	public WebcamEvent(WebcamEventType type, Webcam w) {
		this(type, w, null);
	}

	/**
	 * Webcam event.
	 * 
	 * @param type the event type
	 * @param w the webcam object
	 * @param image the image acquired from webcam
	 */
	public WebcamEvent(WebcamEventType type, Webcam w, BufferedImage image) {
		super(w);
		this.type = type;
		this.image = image;
	}

	@Override
	public Webcam getSource() {
		return (Webcam) super.getSource();
	}

	/**
	 * Return image acquired by webcam. This method will return not-null object
	 * <b>only</b> in case new image acquisition event. For all other events, it
	 * will simply return null.
	 * 
	 * @return Acquired image
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Return event type.
	 * 
	 * @return Event type
	 * @see WebcamEventType
	 */
	public WebcamEventType getType() {
		return type;
	}
}
