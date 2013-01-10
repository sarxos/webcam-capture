package com.github.sarxos.webcam;

import java.util.EventObject;


public class WebcamDiscoveryEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public static final int ADDED = 1;
	public static final int REMOVED = 2;

	public WebcamDiscoveryEvent(Webcam webcam, int type) {
		super(webcam);
	}

	public Webcam getWebcam() {
		return (Webcam) getSource();
	}
}
