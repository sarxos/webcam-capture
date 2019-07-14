package com.github.sarxos.webcam;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamPanelExample {
	static {
		//Webcam.setDriver(new RaspividDriver());
	}

	public static void main(String[] args) throws InterruptedException {
		final JFrame window = new JFrame("Raspberrypi Capture Example");
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new FlowLayout());

		final Dimension resolution = WebcamResolution.QVGA.getSize();

		for (final Webcam webcam : Webcam.getWebcams()) {
			webcam.setCustomViewSizes(resolution);
			webcam.setViewSize(resolution);
			webcam.open();

			final WebcamPanel panel = new WebcamPanel(webcam);
			panel.setDisplayDebugInfo(true);
			panel.setFPSDisplayed(true);
			panel.setDrawMode(DrawMode.FIT);
			panel.setImageSizeDisplayed(true);
			panel.setPreferredSize(resolution);

			window.getContentPane().add(panel);
		}
		window.pack();
		window.setVisible(true);
	}
}