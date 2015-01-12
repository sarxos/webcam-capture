import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.javacv.JavaCvDriver;


public class JavaCvDriverExample {

	static {
		Webcam.setDriver(new JavaCvDriver());
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("JavaCV Webcam Capture Driver Example");
		frame.add(new WebcamPanel(Webcam.getDefault()));
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
