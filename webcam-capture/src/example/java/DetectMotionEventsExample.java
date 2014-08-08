import java.io.IOException;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamResolution;


/**
 * The goal of this example is to demonstrate the idea behind detecting that
 * motion has stopped.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class DetectMotionEventsExample {

	Webcam webcam;
	WebcamMotionDetector detector;

	public DetectMotionEventsExample() {

		webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());

		detector = new WebcamMotionDetector(webcam);
		detector.setInterval(200); // one check per 200 ms
		detector.setInertia(2000); // keep "motion" state for 2 seconds
		detector.start();

		Thread t = new Thread("motion-printer") {

			@Override
			public void run() {

				boolean motion = false;
				long now = 0;

				while (true) {
					now = System.currentTimeMillis();
					if (detector.isMotion()) {
						if (!motion) {
							motion = true;
							System.out.println(now + " MOTION STARTED");
						}
					} else {
						if (motion) {
							motion = false;
							System.out.println(now + " MOTION STOPPED");
						}
					}
					try {
						Thread.sleep(50); // must be smaller than interval
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		t.setDaemon(true);
		t.start();
	}

	public static void main(String[] args) throws IOException {
		new DetectMotionEventsExample();
		System.in.read(); // keep program open
	}
}
