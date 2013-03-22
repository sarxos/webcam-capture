

import java.io.IOException;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;


/**
 * Detect motion.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class DetectMotionExample implements WebcamMotionListener {

	public DetectMotionExample() {
		WebcamMotionDetector detector = new WebcamMotionDetector(Webcam.getDefault());
		detector.setInterval(100); // one check per 100 ms
		detector.addMotionListener(this);
		detector.start();
	}

	@Override
	public void motionDetected(WebcamMotionEvent wme) {
		System.out.println("Detected motion I, alarm turn on you have");
	}

	public static void main(String[] args) throws IOException {
		new DetectMotionExample();
		System.in.read(); // keep program open
	}
}
