package com.github.sarxos.webcam;

import org.openimaj.video.capture.Device;
import org.openimaj.video.capture.VideoCapture;


public class OpenImajFramesCounter {

	public static void main(String[] args) throws Throwable {

		Device d = VideoCapture.getVideoDevices().get(0);
		VideoCapture c = new VideoCapture(640, 480, d);

		c.getNextFrame();
		c.getCurrentFrame();

		int n = 100;
		long t = System.currentTimeMillis();

		for (int i = 0; i < n; i++) {
			c.getNextFrame();
			c.getCurrentFrame();
		}

		System.out.println(1000 * n / (System.currentTimeMillis() - t));

		c.stopCapture();
	}

}
