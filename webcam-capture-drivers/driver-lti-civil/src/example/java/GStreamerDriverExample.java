import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.civil.LtiCivilDriver;


public class GStreamerDriverExample {

	static {
		Webcam.setDriver(new LtiCivilDriver());
	}

	public static void main(String[] args) {

		Webcam webcam = Webcam.getWebcams().get(0);
		webcam.setViewSize(WebcamResolution.VGA.getSize());

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);

		JFrame frame = new JFrame("LTI-CIVIL Webcam Capture Driver Demo");
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}
