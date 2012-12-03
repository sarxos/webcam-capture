package com.github.sarxos.webcam.ds.ipcam;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamCompositeDriver;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;


/**
 * Example of how to use internal webcam together with external IP cameras.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamPlusMjpegExample {

	/**
	 * Customized webcam driver.
	 */
	public static class MyMixedDriver extends WebcamCompositeDriver {

		public MyMixedDriver() {
			add(new WebcamDefaultDriver());
			add(new IpCamDriver());
		}
	}

	public static void main(String[] args) throws MalformedURLException {

		// register custom composite driver
		Webcam.registerDriver(MyMixedDriver.class);

		// register IP camera device
		String address = "http://88.37.116.138/mjpg/video.mjpg ";
		IpCamDeviceRegistry.register(new IpCamDevice("Lignano Beach", new URL(address), IpCamMode.PUSH));

		JFrame window = new JFrame("Live Views From Lignano Beach (Italy)");
		window.setLayout(new FlowLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		for (Webcam webcam : Webcam.getWebcams()) {
			webcam.setViewSize(new Dimension(352, 288));
			window.add(new WebcamPanel(webcam));
		}

		window.pack();
		window.setVisible(true);
	}
}
