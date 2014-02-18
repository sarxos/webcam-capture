package com.github.sarxos.webcam;

import javax.swing.JFrame;

import com.github.sarxos.webcam.ds.openimaj.OpenImajDriver;


public class WebcamPanelExample {

	static {
		Webcam.setDriver(new OpenImajDriver());
	}

	public static void main(String[] args) throws InterruptedException {

		Webcam webcam = Webcam.getDefault();

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);

		JFrame window = new JFrame("Test webcam panel");
		window.add(panel);
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
