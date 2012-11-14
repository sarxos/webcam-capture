package com.github.sarxos.webcam;

import javax.swing.JFrame;


public class WebcamPanelExample {

	public static void main(String[] args) {
		JFrame window = new JFrame("Test webcam panel");
		window.add(new WebcamPanel(Webcam.getDefault()));
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
