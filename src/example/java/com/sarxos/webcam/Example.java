package com.sarxos.webcam;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sarxos.webcam.Webcam;


/**
 * Proof of concept of how to handle webcam video stream from Java
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class Example {

	public static void main(String[] args) throws IOException {

		Webcam webcam = Webcam.getWebcams().get(0);
		webcam.open();

		ImageIO.write(webcam.getImage(), "PNG", new File("test.png"));

		webcam.close();
	}

}
