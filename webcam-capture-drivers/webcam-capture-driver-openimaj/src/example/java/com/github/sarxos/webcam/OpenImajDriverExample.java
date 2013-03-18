package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.ds.openimaj.OpenImajDriver;


public class OpenImajDriverExample {

	static {
		// set OpenIMAJ driver
		Webcam.setDriver(new OpenImajDriver());
	}

	public static void main(String[] args) throws Throwable {

		// get default camera
		Webcam webcam = Webcam.getDefault();

		// set VGA resolution and open
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.open();

		// get image
		BufferedImage image = webcam.getImage();

		// close camera
		webcam.close();

		// save image to file
		ImageIO.write(image, "PNG", new File("bubu.png"));
	}
}
