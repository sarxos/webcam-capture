import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.fswebcam.FsWebcamDriver;


/**
 * Example of how to take single picture.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class TakePictureExample {

	// set capture driver for fswebcam tool
	static {
		Webcam.setDriver(new FsWebcamDriver());
	}

	public static void main(String[] args) throws IOException {

		// get default webcam and open it
		Webcam webcam = Webcam.getDefault();
		webcam.open();

		// get image from webcam device
		BufferedImage image = webcam.getImage();

		// save image to PNG file
		ImageIO.write(image, "PNG", new File("test.png"));

		// close webcam
		webcam.close();
	}
}
