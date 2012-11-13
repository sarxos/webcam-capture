package com.github.sarxos.webcam.ds.ipcam.impl;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IpCamHttpClient extends DefaultHttpClient {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(IpCamHttpClient.class);

	/**
	 * Key for the proxy host property.
	 */
	public static final String PROXY_HOST_KEY = "http.proxyHost";

	/**
	 * Key for the proxy port number property.
	 */
	public static final String PROXY_PORT_KEY = "http.proxyPort";

	private HttpHost proxy = null;

	public IpCamHttpClient() {

		super(new PoolingClientConnectionManager());

		// configure proxy if any

		String proxyHost = System.getProperty(PROXY_HOST_KEY);
		String proxyPort = System.getProperty(PROXY_PORT_KEY);

		if (proxyHost != null && proxyPort != null) {

			LOG.debug("Setting proxy '{}:{}'", proxyHost, proxyPort);

			proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");

			setProxy(proxy);
		}
	}

	public void setProxy(HttpHost proxy) {
		getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	}
}
