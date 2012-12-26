package com.github.sarxos.webcam;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class WebcamUtils {

	public static final void capture(Webcam webcam, File file) {
		try {
			ImageIO.write(webcam.getImage(), "PNG", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final void capture(Webcam webcam, String filename) {
		capture(webcam, new File(filename + ".png"));
	}

}
