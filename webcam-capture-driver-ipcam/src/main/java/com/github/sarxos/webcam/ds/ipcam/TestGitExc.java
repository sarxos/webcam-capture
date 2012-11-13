package com.github.sarxos.webcam.ds.ipcam;

import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.ipcam.http.IpCamHttpClient;
import com.github.sarxos.webcam.ds.ipcam.http.IpCamMode;


public class TestGitExc {

	public static void main(String[] args) throws IOException, InterruptedException {

		System.setProperty(IpCamHttpClient.PROXY_HOST_KEY, "global.proxy.lucent.com");
		System.setProperty(IpCamHttpClient.PROXY_PORT_KEY, "8000");

		// WebcamLogConfigurator.configure("src/test/resources/logback.xml");

		// IpCamDevice da = new X104S("X104S", "bikersschool.dyndns.org");
		// da.setSize(X104S.SIZE_VGA);
		//
		// IpCamDevice db = new IPRobocam641("IPRobocam641",
		// "iprobocam.marmitek.com");
		// db.setAuth(new IpCamAuth("user", "user"));
		//
		// IpCamDevice dc = new B7210("B7210", "114.32.216.24");
		// dc.setAuth(new IpCamAuth("demo", "demo"));
		// dc.setSize(B7210.SIZE_QVGA);

		String address = "http://88.37.116.138/mjpg/video.mjpg ";
		IpCamDevice livecam = new IpCamDevice("Lignano Beach", new URL(address), IpCamMode.PUSH);

		IpCamDriver driver = new IpCamDriver();
		driver.register(livecam);

		Webcam.setDriver(driver);

		WebcamPanel panel = new WebcamPanel(Webcam.getWebcams().get(0));
		panel.setFPS(1);

		JFrame f = new JFrame("Live Views From Lignano Beach (Italy)");
		f.add(panel);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
