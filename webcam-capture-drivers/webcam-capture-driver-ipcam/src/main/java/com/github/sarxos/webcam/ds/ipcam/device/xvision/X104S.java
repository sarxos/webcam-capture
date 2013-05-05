package com.github.sarxos.webcam.ds.ipcam.device.xvision;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;

import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;


/**
 * Speed Dome X104S IP Camera by XVision.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class X104S extends IpCamDevice {

	public static final Dimension SIZE_SXGA = new Dimension(1280, 1024);
	public static final Dimension SIZE_VGA = new Dimension(640, 480);
	public static final Dimension SIZE_QVGA = new Dimension(320, 240);
	public static final Dimension SIZE_QQVGA = new Dimension(160, 128);

	//@formatter:off
	private static final Dimension[] SIZES = new Dimension[] { 
		SIZE_SXGA, 
		SIZE_VGA, 
		SIZE_QVGA, 
		SIZE_QQVGA,
	};
	//@formatter:on

	private URL base = null;

	public X104S(String name, String urlBase) {
		this(name, toURL(urlBase));
	}

	public X104S(String name, URL base) {
		super(name, (URL) null, IpCamMode.PUSH);
		this.base = base;
	}

	@Override
	public Dimension[] getResolutions() {
		return SIZES;
	}

	@Override
	public void setResolution(Dimension size) {

		int index = -1;
		for (int i = 0; i < SIZES.length; i++) {
			if (SIZES[i].equals(size)) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			throw new IllegalArgumentException(String.format("Incorrect size %s", size));
		}

		super.setResolution(size);
	}

	@Override
	public URL getURL() {

		int index = -1;
		for (int i = 0; i < SIZES.length; i++) {
			if (SIZES[i].equals(getResolution())) {
				index = i;
				break;
			}
		}

		String r = "";
		switch (index) {
			case 0:
				r = "sxga";
				break;
			case 1:
				r = "vga";
				break;
			case 2:
				r = "qvga";
				break;
			case 3:
				r = "qqvga";
				break;
		}

		String url = String.format("%s/video.cgi?resolution=%s&random=0.%s", base, r, System.currentTimeMillis());

		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new WebcamException(String.format("Incorrect URL %s", url), e);
		}
	}

}
