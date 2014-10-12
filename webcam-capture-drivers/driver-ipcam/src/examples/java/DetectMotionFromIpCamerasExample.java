import java.awt.Dimension;
import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamMotionDetector;
import com.github.sarxos.webcam.WebcamMotionEvent;
import com.github.sarxos.webcam.WebcamMotionListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamStorage;


public class DetectMotionFromIpCamerasExample {

	static {
		Webcam.setDriver(new IpCamDriver(new IpCamStorage("src/examples/resources/movable.xml")));
	}

	public static void main(String[] args) throws MalformedURLException {

		JFrame f = new JFrame("Detect Motion Multiple Cameras");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new GridLayout(0, 3, 1, 1));

		WebcamMotionListener listener = new WebcamMotionListener() {

			@Override
			public void motionDetected(WebcamMotionEvent wme) {
				synchronized (this) {
					System.out.println("motion detected ---------------- ");
					System.out.println("webcam:    " + ((WebcamMotionDetector) wme.getSource()).getWebcam());
					System.out.println("diff area: " + wme.getArea() + "%");
					System.out.println("cog:       [" + wme.getCog().getX() + "," + wme.getCog().getY() + "]");
				}
			}
		};

		final List<Webcam> webcams = new ArrayList<Webcam>();
		final List<WebcamPanel> panels = new ArrayList<WebcamPanel>();
		final List<WebcamMotionDetector> detectors = new ArrayList<WebcamMotionDetector>();

		for (Webcam webcam : Webcam.getWebcams()) {

			webcams.add(webcam);

			WebcamPanel panel = new WebcamPanel(webcam, new Dimension(256, 144), false);
			panel.setFitArea(true);
			panel.setFPSLimited(true);
			panel.setFPSLimit(0.5); // 0.5 FPS = 1 frame per 2 seconds
			panel.setBorder(BorderFactory.createEmptyBorder());

			f.add(panel);
			panels.add(panel);

			WebcamMotionDetector detector = new WebcamMotionDetector(webcam);
			detector.addMotionListener(listener);
			detector.setInterval(100); // one motion check per 2 seconds

			detectors.add(detector);
		}

		f.pack();
		f.setVisible(true);

		for (int i = 0; i < webcams.size(); i++) {
			final int x = i;
			Thread t = new Thread() {

				@Override
				public void run() {
					webcams.get(x).open(); // open in asynchronous mode
					panels.get(x).start();
					detectors.get(x).start();
				}
			};
			t.setDaemon(true);
			t.start();
		}
	}
}
