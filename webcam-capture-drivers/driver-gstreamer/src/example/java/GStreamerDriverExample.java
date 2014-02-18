import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.gstreamer.GStreamerDriver;


public class GStreamerDriverExample {

	static {
		Webcam.setDriver(new GStreamerDriver());
	}

	public static void main(String[] args) {

		WebcamPanel panel = new WebcamPanel(Webcam.getWebcams().get(0));
		panel.setFPSDisplayed(true);

		JFrame frame = new JFrame("GStreamer Webcam Capture Driver Demo");
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}
