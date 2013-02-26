package com.github.sarxos.webcam.ds.ipcam;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;


public class JpegExample {

	public static void main(String[] args) throws MalformedURLException {

		// Dasding is a radio in Germany. They have few network cameras
		// available to be viewed online. Here in this example we are creating
		// IP camera device working in PULL mode to request static JPEG images.

		IpCamStorage storage = new IpCamStorage("src/examples/resources/cameras.xml");

		Webcam.setDriver(new IpCamDriver(storage));

		JFrame f = new JFrame("Dasding Studio Live IP Cameras");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new GridLayout(0, 3, 1, 1));

		List<WebcamPanel> panels = new ArrayList<WebcamPanel>();

		for (Webcam webcam : Webcam.getWebcams()) {

			WebcamPanel panel = new WebcamPanel(webcam, new Dimension(256, 144), false);
			panel.setFillArea(true);
			panel.setFPSLimited(true);
			panel.setFPS(0.2); // 0.2 FPS = 1 frame per 5 seconds
			panel.setBorder(BorderFactory.createEmptyBorder());

			f.add(panel);
			panels.add(panel);
		}

		f.pack();
		f.setVisible(true);

		for (WebcamPanel panel : panels) {
			panel.start();
		}
	}
}
