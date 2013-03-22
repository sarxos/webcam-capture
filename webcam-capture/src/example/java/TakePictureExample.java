

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;


/**
 * Example of how to take single picture.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class TakePictureExample {

	public static void main(String[] args) throws IOException {

		// automatically open if webcam is closed
		Webcam.setAutoOpenMode(true);

		// get image
		BufferedImage image = Webcam.getDefault().getImage();

		// save image to PNG file
		ImageIO.write(image, "PNG", new File("test.png"));
	}
}
