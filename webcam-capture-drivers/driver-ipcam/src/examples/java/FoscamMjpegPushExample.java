import java.net.MalformedURLException;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.IpCamAuth;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;


public class FoscamMjpegPushExample {

	static {
		Webcam.setDriver(new IpCamDriver());
	}

	public static void main(String[] args) throws MalformedURLException {

		String name = "Test #255";
		String url = "http://ce3014.myfoscam.org:20054/videostream.cgi";

		IpCamMode mode = IpCamMode.PUSH;
		IpCamAuth auth = new IpCamAuth("username", "password"); // replace with your own username and password

		IpCamDeviceRegistry.register(name, url, mode, auth);

		WebcamPanel panel = new WebcamPanel(Webcam.getWebcams().get(0));
		panel.setFPSLimit(1);

		JFrame f = new JFrame("Test #255 PUSH");
		f.add(panel);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
