
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;


public class CustomResolutionExample {

	public static void main(String[] args) {

		/**
		 * When you set custom resolutions you have to be sure that your webcam
		 * device will handle them!
		 */

		//@formatter:off
		Dimension[] nonStandardResolutions = new Dimension[] {
			WebcamResolution.PAL.getSize(),
			WebcamResolution.HD720.getSize(),
			new Dimension(2000, 1000),
			new Dimension(1000, 500),
		};
		//@formatter:on

		// your camera have to support HD720p to run this code
		Webcam webcam = Webcam.getDefault();
		webcam.setCustomViewSizes(nonStandardResolutions);
		webcam.setViewSize(WebcamResolution.HD720.getSize());
		webcam.open();

		BufferedImage image = webcam.getImage();

		System.out.println(image.getWidth() + "x" + image.getHeight());
	}

}
