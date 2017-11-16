import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.ds.gstreamer.ScreenCaptureDriver;


public class WebcamPanelExample {

	static {
		Webcam.setDriver(new ScreenCaptureDriver());
	}

	public static void main(String[] args) {

		final JFrame window = new JFrame("Screen Capture Example");
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new FlowLayout());

		for (Webcam webcam : Webcam.getWebcams()) {

			WebcamPanel panel = new WebcamPanel(webcam);
			panel.setFPSDisplayed(true);
			panel.setDrawMode(DrawMode.FIT);
			panel.setImageSizeDisplayed(true);
			panel.setPreferredSize(new Dimension(300, 200));

			window.getContentPane().add(panel);
		}

		window.pack();
		window.setVisible(true);
	}
}
