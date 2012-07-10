package com.github.sarxos.webcam;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;



/**
 * Detect motion.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class DetectMotionExample2 {

	public static void main(String[] args) throws InterruptedException {

		WebcamMotionDetector detector = new WebcamMotionDetector(Webcam.getDefault());
		detector.setInterval(100); // one check per 100 ms
		detector.start();

		while (true) {
			if (detector.isMotion()) {
				System.out.println("Detected motion I, alarm turn on you have");
			}
			Thread.sleep(500);
		}
	}
}
