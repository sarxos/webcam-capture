import java.net.MalformedURLException;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;


public class DroidCamExample {

	static {
		Webcam.setDriver(new IpCamDriver());
	}

	public static void main(String[] args) throws MalformedURLException {

		IpCamDeviceRegistry.register("DroidCam", "http://192.168.29.65:4747/mjpegfeed?640x480", IpCamMode.PUSH);

		WebcamPanel panel = new WebcamPanel(Webcam.getWebcams().get(0));
		panel.setFPSLimit(1);

		JFrame f = new JFrame("DroidCam Demo");
		f.add(panel);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
