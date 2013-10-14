import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;


public class GetBitmapFromCamera {

	public static void main(String[] args) throws IOException {
		Webcam webcam = Webcam.getDefault();
		webcam.open();
		ImageIO.write(webcam.getImage(), "JPG", new File(System.currentTimeMillis() + ".jpg"));
		webcam.close();
	}
}
