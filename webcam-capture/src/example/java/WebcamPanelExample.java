import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.log.WebcamLogConfigurator;


public class WebcamPanelExample {

	public static void main(String[] args) throws InterruptedException {

		WebcamLogConfigurator.configure("src/example/resources/logback.xml");

		JFrame window = new JFrame("Test webcam panel");

		final WebcamPanel panel = new WebcamPanel(Webcam.getDefault());
		panel.setFPSDisplayed(true); // display FPS on screen
		panel.setFPSLimited(false); // no FPS limit
		panel.setFillArea(true); // image will be resized with window

		window.add(panel);
		window.pack();
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Thread.sleep(5000);
		panel.stop();
		Thread.sleep(5000);
		panel.start();
		Thread.sleep(5000);
		panel.pause();
		Thread.sleep(5000);
		panel.resume();

	}
}
