package com.github.sarxos.webcam.ds.ipcam;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamException;


/**
 * IP camera driver.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class IpCamDriver implements WebcamDriver {

	private static final List<WebcamDevice> DEVICES = new ArrayList<WebcamDevice>();

	@Override
	public List<WebcamDevice> getDevices() {
		return Collections.unmodifiableList(DEVICES);
	}

	public void registerDevice(IpCamDevice device) {
		DEVICES.add(device);
	}

	public void registerURL(String name, URL url) {
		DEVICES.add(new IpCamDevice(name, url));
	}

	public void registerURL(String name, String url) {
		try {
			DEVICES.add(new IpCamDevice(name, new URL(url)));
		} catch (MalformedURLException e) {
			throw new WebcamException("Incorrect URL", e);
		}
	}

	public static void main(String[] args) throws IOException {
		IpCamDriver driver = new IpCamDriver();
		driver.registerURL("Test", "http://www.dasding.de/ext/webcam/webcam770.php?cam=1");
		WebcamDevice device = driver.getDevices().get(0);
		BufferedImage image = device.getImage();
		ImageIO.write(image, "jpg", new File("ninonap.jpg"));
	}

}
