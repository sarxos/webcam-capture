
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.javacv.JavaCvDriver;

public class WebcamPanelExample {
	static {
		Webcam.setDriver(new JavaCvDriver());
	}

	public static void main(String[] args) throws InterruptedException {
		final JFrame window = new JFrame("JavaCV Capture Example");
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new FlowLayout());

		//final Dimension resolution = WebcamResolution.VGA.getSize();

		for (final Webcam webcam : Webcam.getWebcams()) {
			//webcam.setCustomViewSizes(resolution);
			//webcam.setViewSize(resolution);
			webcam.open();

			final WebcamPanel panel = new WebcamPanel(webcam);
			panel.setDisplayDebugInfo(true);
			panel.setFPSDisplayed(true);
			panel.setDrawMode(DrawMode.FILL);
			panel.setImageSizeDisplayed(true);
			//panel.setPreferredSize(resolution);

			window.getContentPane().add(panel);
		}
		window.pack();
		window.setVisible(true);
	}
}