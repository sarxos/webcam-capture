package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
 * Example of how to take single picture.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class TakePictureExample {

	public static void main(String[] args) throws IOException {
		BufferedImage image = Webcam.getDefault().getImage();
		ImageIO.write(image, "PNG", new File("test.png"));
	}
}
