package com.github.sarxos.webcam;

import java.awt.Dimension;

import javax.swing.JFrame;


public class WebcamPanelExample {

	public static void main(String[] args) {
		WebcamPanel panel = new WebcamPanel(Webcam.getDefault());
		JFrame jframe = new JFrame("Test webcam panel");
		jframe.setPreferredSize(new Dimension(300, 200));
		jframe.add(panel);
		jframe.pack();
		jframe.setVisible(true);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
