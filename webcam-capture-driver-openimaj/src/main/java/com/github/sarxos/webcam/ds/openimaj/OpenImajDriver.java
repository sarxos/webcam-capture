package com.github.sarxos.webcam.ds.openimaj;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;

import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamDevice;


public class OpenImajDriver implements WebcamDriver {

	List<WebcamDevice> webcamDevices = null;

	@Override
	public List<WebcamDevice> getDevices() {

		if (webcamDevices == null) {
			webcamDevices = new ArrayList<WebcamDevice>();
			List<Device> devices = VideoCapture.getVideoDevices();
			for (Device device : devices) {
				webcamDevices.add(new OpenImajDevice(device));
			}
		}

		return webcamDevices;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		WebcamDevice d = new OpenImajDriver().getDevices().get(0);
		d.setSize(d.getSizes()[0]);
		d.open();
		for (int i = 0; i < 5; i++) {
			ImageIO.write(d.getImage(), "JPG", new File(System.currentTimeMillis() + ".jpg"));
			Thread.sleep(1000);
		}

		d.close();
	}
}
