package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
 * @author Bartosz Firyn (SarXos)
 */
public class TakePictureExample {

	public static void main(String[] args) throws IOException {
		Webcam webcam = Webcam.getDefault();
		BufferedImage image = webcam.getImage();
		ImageIO.write(image, "PNG", new File("test.png"));
	}
}
