package com.github.sarxos.webcam.ds.ipcam.http;

import java.awt.image.BufferedImage;
import java.net.URL;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;


public class IpCamHttpClient extends DefaultHttpClient {

	private static IpCamHttpClient istance = new IpCamHttpClient();

	public IpCamHttpClient() {
		super(new PoolingClientConnectionManager());
	}

	public static IpCamHttpClient getIstance() {
		return istance;
	}

	public BufferedImage getImage(URL url) {

		return null;
	}
}
