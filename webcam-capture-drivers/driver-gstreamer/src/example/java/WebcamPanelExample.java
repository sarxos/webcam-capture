import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.gstreamer.GStreamerDriver;


public class WebcamPanelExample {

	static {
		Webcam.setDriver(new GStreamerDriver());
	}

	public static void main(String[] args) {

		WebcamResolution resolution = WebcamResolution.HD;
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(resolution.getSize());

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setDrawMode(DrawMode.FIT);
		panel.setImageSizeDisplayed(true);

		JFrame window = new JFrame(webcam + " @ " + resolution);
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
