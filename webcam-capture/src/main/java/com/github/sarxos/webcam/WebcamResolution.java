package com.github.sarxos.webcam;

import java.awt.Dimension;


/**
 * Various resolutions.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public enum WebcamResolution {

	/**
	 * Size 176x144
	 */
	QQVGA(176, 144),

	/**
	 * Size 320x240
	 */
	QVGA(320, 240),

	/**
	 * Size 352x288
	 */
	CIF(352, 288),

	/**
	 * Size 480x400
	 */
	HVGA(480, 400),

	/**
	 * Size 640x480
	 */
	VGA(640, 480),

	/**
	 * Size 768x576
	 */
	PAL(768, 576),

	/**
	 * Size 800x600
	 */
	SVGA(800, 600),

	/**
	 * 1024x768
	 */
	XGA(1024, 768),

	/**
	 * Size 1280x720 also known as HD 720p.
	 */
	HD720(1280, 720),

	/**
	 * Size 1280x768
	 */
	WXGA(1280, 768),

	/**
	 * Size 1280x1024
	 */
	SXGA(1280, 1024),

	/**
	 * Size 1600x1200
	 */
	UXGA(1600, 1200),

	/**
	 * Size 2048x1536
	 */
	QXGA(2048, 1536),

	/**
	 * Size 2560x1440
	 */
	WQHD(2560, 1440),

	/**
	 * Size 2560x1600
	 */
	WQXGA(2560, 1600),

	;

	/**
	 * Resolution size.
	 */
	private Dimension size = null;

	/**
	 * 
	 * @param width the resolution width
	 * @param height the resolution height
	 */
	private WebcamResolution(int width, int height) {
		this.size = new Dimension(width, height);
	}

	/**
	 * Get resolution size.
	 * 
	 * @return Dimension object
	 */
	public Dimension getSize() {
		return size;
	}
}
