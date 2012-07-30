package com.github.sarxos.webcam.ds.ipcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.ipcam.http.IpCamAuth;
import com.github.sarxos.webcam.ds.ipcam.http.IpCamHttpClient;
import com.github.sarxos.webcam.ds.ipcam.http.IpCamMode;


/**
 * IP camera device.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class IpCamDevice implements WebcamDevice {

	private String name = null;
	private URL url = null;
	private IpCamMode mode = null;
	private IpCamAuth auth = null;
	private IpCamHttpClient client = IpCamHttpClient.getIstance();

	public IpCamDevice(String name, URL url) {
		this(name, url, IpCamMode.PULL);
	}

	public IpCamDevice(String name, URL url, IpCamMode mode) {
		this(name, url, mode, IpCamAuth.NONE);
	}

	public IpCamDevice(String name, URL url, IpCamMode mode, IpCamAuth auth) {
		if (name == null) {
			throw new IllegalArgumentException("Name cannot be null");
		}
		if (url == null) {
			throw new IllegalArgumentException("URL cannot be null");
		}
		this.name = name;
		this.url = url;
		this.mode = mode;
		this.auth = auth;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Dimension[] getSizes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension getSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSize(Dimension size) {
		// TODO Auto-generated method stub

	}

	@Override
	public BufferedImage getImage() {
		HttpGet get = null;
		try {
			get = new HttpGet(url.toURI());
			HttpResponse respone = client.execute(get);
			HttpEntity entity = respone.getEntity();
			return ImageIO.read(entity.getContent());
		} catch (Exception e) {
			throw new WebcamException("Cannot download image", e);
		} finally {
			if (get != null) {
				get.releaseConnection();
			}
		}
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	public URL getUrl() {
		return url;
	}

	public IpCamMode getMode() {
		return mode;
	}

	public IpCamAuth getAuth() {
		return auth;
	}
}
