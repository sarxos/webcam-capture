package com.github.sarxos.webcam.ds.ipcam;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;


/**
 * Example of how to stream MJPEG with Webcam Capture.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class MjpegExample {

	public static void main(String[] args) throws MalformedURLException {

		String address = "http://88.37.116.138/mjpg/video.mjpg ";
		IpCamDevice livecam = new IpCamDevice("Lignano Beach", new URL(address), IpCamMode.PUSH);

		IpCamDriver driver = new IpCamDriver();
		driver.register(livecam);

		Webcam.setDriver(driver);

		WebcamPanel panel = new WebcamPanel(Webcam.getWebcams().get(0));
		panel.setFPS(1);

		JFrame f = new JFrame("Live Views From Lignano Beach (Italy)");
		f.add(panel);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
