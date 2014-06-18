import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.civil.LtiCivilDriver;


public class LtiCivilDriverExample {

	static {
		Webcam.setDriver(new LtiCivilDriver());
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("LTI-CIVIL Webcam Capture Driver Example");
		frame.add(new WebcamPanel(Webcam.getDefault()));
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
