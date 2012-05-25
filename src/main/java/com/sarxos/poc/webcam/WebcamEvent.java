package com.sarxos.poc.webcam;

import java.util.EventObject;


public class WebcamEvent extends EventObject {

	private static final long serialVersionUID = 7701762815832038598L;

	public WebcamEvent(Webcam w) {
		super(w);
	}

	@Override
	public Webcam getSource() {
		return (Webcam) super.getSource();
	}

}
