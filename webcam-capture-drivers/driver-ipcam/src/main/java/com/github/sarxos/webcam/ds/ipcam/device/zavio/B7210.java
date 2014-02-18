package com.github.sarxos.webcam.ds.ipcam.device.zavio;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;

import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;


/**
 * B7210 2M Bullet IP Camera from Zavio.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class B7210 extends IpCamDevice {

	public static final Dimension SIZE_HD_1080 = new Dimension(1280, 1024);
	public static final Dimension SIZE_43_960 = new Dimension(1280, 960);
	public static final Dimension SIZE_QVGA = new Dimension(320, 240);

	//@formatter:off
	private static final Dimension[] SIZES = new Dimension[] { 
		SIZE_HD_1080, 
		SIZE_43_960, 
		SIZE_QVGA, 
	};
	//@formatter:on

	private URL base = null;

	public B7210(String name, String urlBase) {
		this(name, toURL(urlBase));
	}

	public B7210(String name, URL base) {
		super(name, (URL) null, IpCamMode.PULL);
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

		int res = 0;
		switch (index) {
			case 0:
				res = 0;
				break;
			case 1:
				res = 3;
				break;
			case 2:
				res = 4;
				break;
		}

		long time = System.currentTimeMillis();

		String url = String.format("%s/cgi-bin/view/image?pro_%d&%d", base, res, time);
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new WebcamException(String.format("Incorrect URL %s", url), e);
		}
	}

}
