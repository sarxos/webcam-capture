import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;


public class WebcamPanelExample {

	public static void main(String[] args) throws InterruptedException {

		Webcam webcam = Webcam.getDefault();

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);

		JFrame window = new JFrame("Test webcam panel");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
