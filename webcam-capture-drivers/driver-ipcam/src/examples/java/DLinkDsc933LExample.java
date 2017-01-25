import java.net.MalformedURLException;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.device.dlink.DSC933L;


public class DLinkDsc933LExample {

	static {
		Webcam.setDriver(new IpCamDriver());
	}

	public static void main(String[] args) throws MalformedURLException {

		final String name = "D-Link 993L Camera";
		final String user = "{username}"; // change to your own username
		final String pass = "{password}"; // change to your own password
		final String url = "http://{ip-address-or-domain-name}"; // camera's IP address or domain

		IpCamDeviceRegistry.register(new DSC933L(name, url, user, pass));

		final Webcam webcam = Webcam.getDefault();
		final WebcamPanel panel = new WebcamPanel(webcam);

		JFrame f = new JFrame(name);
		f.add(panel);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
