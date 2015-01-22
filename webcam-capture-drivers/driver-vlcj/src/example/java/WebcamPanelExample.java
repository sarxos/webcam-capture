import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.vlcj.VlcjDriver;


public class WebcamPanelExample {

	static {
		Webcam.setDriver(new VlcjDriver());
	}

	public static void main(String[] args) throws InterruptedException {

		Webcam webcam = Webcam.getWebcams().get(0);
		webcam.setViewSize(WebcamResolution.VGA.getSize());

		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setImageSizeDisplayed(true);

		JFrame window = new JFrame("Webcam Panel");
		window.add(panel);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
