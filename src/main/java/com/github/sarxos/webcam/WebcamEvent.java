package com.github.sarxos.webcam;

import java.util.EventObject;


/**
 * Webcam event.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamEvent extends EventObject {

	private static final long serialVersionUID = 7701762815832038598L;

	/**
	 * Webcam event.
	 * 
	 * @param w - webcam object
	 */
	public WebcamEvent(Webcam w) {
		super(w);
	}

	@Override
	public Webcam getSource() {
		return (Webcam) super.getSource();
	}

}
