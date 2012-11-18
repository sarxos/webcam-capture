package com.github.sarxos.webcam;

import javax.swing.JApplet;


public class WebcamAppletExample extends JApplet {

	private static final long serialVersionUID = 3517366452510566924L;

	private Webcam webcam = null;

	public WebcamAppletExample() {
		super();
	}

	@Override
	public void start() {
		webcam = Webcam.getDefault();
		add(new WebcamPanel(webcam));
	}

	@Override
	public void destroy() {
		webcam.close();
	}
}
