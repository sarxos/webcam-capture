import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.mjpeg.MjpegCaptureDriver;


public class WebcamPanelExample {

	static {
		Webcam.setDriver(new MjpegCaptureDriver()
			.withUri("tcp://127.0.0.1:5000") // this is your local host
			.withUri("tcp://192.168.1.12:5000")); // this is some remote host
	}

	public static void main(String[] args) throws InterruptedException {

		// run this from your terminal (as a single line, and remove dollar sign):

		// $ gst-launch-1.0 -v v4l2src device=/dev/video0 ! capsfilter caps="video/x-raw, width=320,
		// height=240" ! decodebin name=dec ! videoconvert ! jpegenc ! multipartmux ! tcpserversink
		// host=0.0.0.0 port=5000

		final Webcam webcam = Webcam.getDefault();

		final WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setImageSizeDisplayed(true);
		panel.setMirrored(true);

		final JFrame window = new JFrame("MJPEG Streaming From TCP Socket");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
