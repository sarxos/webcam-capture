package com.github.sarxos.webcam;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;



public class WebcamUtils {

	public static final void capture(Webcam webcam, String filename) {
		try {
			ImageIO.write(webcam.getImage(), "PNG", new File(filename + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
