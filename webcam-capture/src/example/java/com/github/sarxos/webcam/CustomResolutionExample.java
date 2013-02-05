package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;


public class CustomResolutionExample {

	public static void main(String[] args) {

		Dimension[] nonStandardResolutions = new Dimension[] {
		WebcamResolution.PAL.getSize(),
		WebcamResolution.HD720.getSize(),
		new Dimension(2000, 1000),
		new Dimension(1000, 500),
		};

		Webcam webcam = Webcam.getDefault();
		webcam.setCustomViewSizes(nonStandardResolutions);
		webcam.open();

		BufferedImage image = webcam.getImage();

		System.out.println(image.getWidth() + "x" + image.getHeight());
	}

}
