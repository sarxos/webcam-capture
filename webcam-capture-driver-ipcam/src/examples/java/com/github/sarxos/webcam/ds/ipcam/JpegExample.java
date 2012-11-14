package com.github.sarxos.webcam.ds.ipcam;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;


public class JpegExample {

	public static void main(String[] args) throws MalformedURLException {

		// Dasding is a radio in Germany. They have few network cameras
		// available to be viewed online. Here in this example we are creating
		// IP camera device working in PULL mode to request static JPEG images.

		String address = "http://www.dasding.de/ext/webcam/webcam770.php?cam=1";
		IpCamDevice livecam = new IpCamDevice("dasding", new URL(address), IpCamMode.PULL);

		IpCamDriver driver = new IpCamDriver();
		driver.register(livecam);

		Webcam.setDriver(driver);

		WebcamPanel panel = new WebcamPanel(Webcam.getDefault());
		panel.setFPS(0.2); // 1 frame per 5 seconds

		JFrame f = new JFrame("Dasding Studio Live IP Camera");
		f.add(panel);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}
