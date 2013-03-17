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
	 * Webcam event.
	 * 
	 * @param w the webcam object
	 */
	public WebcamEvent(Webcam w) {
		this(w, null);
	}

	/**
	 * Webcam event.
	 * 
	 * @param w the webcam object
	 * @param image the image acquired from webcam
	 */
	public WebcamEvent(Webcam w, BufferedImage image) {
		super(w);
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
}
