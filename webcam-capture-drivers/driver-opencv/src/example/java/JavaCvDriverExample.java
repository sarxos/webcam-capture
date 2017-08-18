import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.javacv.JavaCvDriver;


public class JavaCvDriverExample {

	static {
		Webcam.setDriver(new JavaCvDriver());
	}

	public static void main(String[] args) {

		final Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.HD.getSize());

		final WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setDrawMode(DrawMode.FIT);
		panel.setImageSizeDisplayed(true);

		final JFrame frame = new JFrame("JavaCV Webcam Capture Driver Example");
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
