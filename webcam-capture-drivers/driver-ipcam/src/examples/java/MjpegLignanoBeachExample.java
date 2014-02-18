import java.net.MalformedURLException;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;


/**
 * Example of how to stream MJPEG with Webcam Capture.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class MjpegLignanoBeachExample {

	/**
	 * Remember to add IP camera driver JAR to the application classpath!
	 * Otherwise you will not be able to use IP camera driver features. Driver
	 * has to be set at the very beginning, before any webcam-capture related
	 * method is being invoked.
	 */
	static {
		Webcam.setDriver(new IpCamDriver());
	}

	public static void main(String[] args) throws MalformedURLException {

		IpCamDeviceRegistry.register("Lignano", "http://88.37.116.138/mjpg/video.mjpg", IpCamMode.PUSH);

		WebcamPanel panel = new WebcamPanel(Webcam.getWebcams().get(0));
		panel.setFPSLimit(1);

		JFrame f = new JFrame("Live Views From Lignano Beach");
		f.add(panel);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
