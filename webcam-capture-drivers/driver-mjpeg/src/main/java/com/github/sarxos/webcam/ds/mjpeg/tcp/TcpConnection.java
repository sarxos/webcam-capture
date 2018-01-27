package com.github.sarxos.webcam.ds.mjpeg.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is {@link URLConnection} implementation which support reading from plain TCP {@link Socket}.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class TcpConnection extends URLConnection implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(TcpConnection.class);

	/**
	 * The underlying {@link Socket} used to read from TCP.
	 */
	private Socket socket;

	public TcpConnection(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {

		if (connected) {
			return;
		}

		socket = new Socket(url.getHost(), url.getPort());
		connected = true;
	}

	@Override
	public InputStream getInputStream() throws IOException {

		if (!connected) {
			connect();
		}

		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {

		if (!connected) {
			connect();
		}

		return socket.getOutputStream();
	}

	/**
	 * Disconnect this connection and close underlying {@link Socket}.
	 */
	public void disconnect() {

		if (!connected) {
			return;
		}

		try {
			socket.close();
		} catch (IOException e) {
			LOG.debug("I/O exception when closing socket", e);
		}

		this.connected = false;
	}

	@Override
	public void close() throws Exception {
		disconnect();
	}
}