import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.dummy.WebcamDummyDriver;


public class DummyDeviceExample {

	static {
		Webcam.setDriver(new WebcamDummyDriver(3));
	}

	public static void main(String[] args) throws InterruptedException {

		Webcam webcam = Webcam.getDefault();
		// webcam.setViewSize(WebcamResolution.VGA.getSize());

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setImageSizeDisplayed(true);
		panel.setMirrored(false);

		JFrame window = new JFrame("Dummy Device Example");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}

}
