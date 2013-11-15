import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;


/**
 * @author Bartosz Firyn (SarXos)
 */
public class TakeSnapshotFromAllWebcamsExample {

	public static void main(String[] args) throws IOException {

		List<Webcam> webcams = Webcam.getWebcams();

		// NOTE!
		/*
		 * Yes, I know we could do this in one loop, but I wanted to prove here
		 * that it's possible to have many native webcams open in the same time.
		 * I tested this example with 4 webcams simultaneously connected to the
		 * USB bus - 1 x PC embedded device, and 3 x UVC devices connected to
		 * the USB concentration hub, which was connected to the USB 2.0 port.
		 * It's working like a charm.
		 */

		// USB BANDWIDTH!
		/*
		 * As you probably know the USB has limited bandwidth and therefore it
		 * may not be possible to transfer images from as many cameras as you
		 * would like to wish. This example works when I'm using QQVGA (176x144)
		 * but fails with the error message when I want to fetch VGA (640x480).
		 */

		// open all at once (this is the most time-consuming operation, all
		// others are executed instantly)
		for (Webcam webcam : webcams) {
			System.out.format("Opening %s\n", webcam.getName());
			webcam.open();
		}

		// capture picture from all of them
		for (int i = 0; i < webcams.size(); i++) {
			Webcam webcam = webcams.get(i);
			System.out.format("Capturing %s\n", webcam.getName());
			ImageIO.write(webcam.getImage(), "PNG", new File(String.format("test-%d.png", i)));
		}

		// close all
		for (Webcam webcam : webcams) {
			System.out.format("Closing %s\n", webcam.getName());
			webcam.close();
		}
	}

}
