import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;


public class WebcamPanelExample {

	public static void main(String[] args) throws InterruptedException {

		JFrame window = new JFrame("Test webcam panel");

		Webcam webcam = Webcam.getDefault();

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true); // display FPS on screen
		panel.setFPSLimited(false); // no FPS limit
		panel.setFillArea(true); // image will be resized with window

		window.add(panel);
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
