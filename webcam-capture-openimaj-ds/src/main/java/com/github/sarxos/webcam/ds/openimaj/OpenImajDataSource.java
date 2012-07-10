package com.github.sarxos.webcam.ds.openimaj;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

import com.github.sarxos.webcam.WebcamDataSource;
import com.github.sarxos.webcam.WebcamDevice;


public class OpenImajDataSource implements WebcamDataSource {

	List<WebcamDevice> webcamDevices = null;

	@Override
	public List<WebcamDevice> getDevices() {

		if (webcamDevices == null) {
			webcamDevices = new ArrayList<WebcamDevice>();
			List<Device> devices = VideoCapture.getVideoDevices();
			for (Device device : devices) {
				webcamDevices.add(new OpenImajWebcamDevice(device));
			}
		}

		return webcamDevices;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		WebcamDevice d = new OpenImajDataSource().getDevices().get(0);
		d.setSize(d.getSizes()[0]);
		d.open();
		for (int i = 0; i < 5; i++) {
			ImageIO.write(d.getImage(), "JPG", new File(System.currentTimeMillis() + ".jpg"));
			Thread.sleep(1000);
		}

		d.close();
	}
}
