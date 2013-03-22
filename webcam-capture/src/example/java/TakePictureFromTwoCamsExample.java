

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;


/**
 * @author Bartosz Firyn (SarXos)
 */
public class TakePictureFromTwoCamsExample {

	public static void main(String[] args) throws IOException {

		Webcam webcam1 = Webcam.getWebcams().get(0);
		Webcam webcam2 = Webcam.getWebcams().get(1);

		webcam1.open();
		webcam2.open();

		ImageIO.write(webcam1.getImage(), "PNG", new File("test1.png"));
		ImageIO.write(webcam2.getImage(), "PNG", new File("test2.png"));

		webcam1.close();
		webcam2.close();
	}

}
