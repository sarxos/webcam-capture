import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;


public class PureDeviceTest {

	public static void main(String[] args) {

		WebcamDefaultDriver driver = new WebcamDefaultDriver();
		List<WebcamDevice> devices = driver.getDevices();

		for (WebcamDevice d : devices) {
			System.out.println(d.getName());
			try {
				d.open();
				BufferedImage image = d.getImage();
				ImageIO.write(image, "jpg", new File(System.currentTimeMillis() + ".jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				d.close();
			}
		}

		// finally at the end, don't forget to dispose
		for (WebcamDevice d : devices) {
			d.dispose();
		}
	}

}
