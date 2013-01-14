package com.github.sarxos.webcam;

import java.awt.Dimension;


public enum WebcamResolution {

	QQVGA(176, 144),
	QVGA(320, 240),
	CIF(352, 288),
	HVGA(480, 400),
	VGA(640, 480),
	PAL(768, 576),
	SVGA(800, 600),
	XGA(1024, 768),
	HD720(1280, 720),
	WXGA(1280, 768),
	SXGA(1280, 1024),
	UXGA(1600, 1200),
	QXGA(2048, 1536);

	private Dimension size = null;

	private WebcamResolution(int width, int height) {
		this.size = new Dimension(width, height);
	}

	public Dimension getSize() {
		return size;
	}
}
