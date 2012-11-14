package com.github.sarxos.webcam;

import javax.swing.JFrame;


public class WebcamPanelExample {

	public static void main(String[] args) {

		WebcamPanel panel = new WebcamPanel(Webcam.getDefault());
		panel.setFPS(5);

		JFrame window = new JFrame("Test webcam panel");
		window.add(panel);
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
