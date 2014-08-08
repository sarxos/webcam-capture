import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.gstreamer.GStreamerDriver;


public class WebcamPanelExample {

	static {
		Webcam.setDriver(new GStreamerDriver());
	}

	public static void main(String[] args) {

		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.HD720.getSize());

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setDisplayDebugInfo(true);
		panel.setFPSDisplayed(true);
		panel.setFillArea(true);

		JFrame window = new JFrame("Test webcam panel");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
