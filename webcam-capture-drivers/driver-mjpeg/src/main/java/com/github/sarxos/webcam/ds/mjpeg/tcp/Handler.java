package com.github.sarxos.webcam.ds.mjpeg.tcp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


/**
 * This class has to be named Handler. Otherwise it won't work. See this link for more details:<br>
 * <br>
 * https://accu.org/index.php/journals/1434
 *
 * @author Bartosz Firyn (sarxos)
 */
public class Handler extends URLStreamHandler {

	private static final String PKGS_PROPERTY = "java.protocol.handler.pkgs";

	protected static void register() {

		final Class<?> clazz = Handler.class;
		final String pkgs = System.getProperty(PKGS_PROPERTY, "");
		final String pkg = clazz.getPackage().getName();
		final int index = pkg.lastIndexOf('.');

		System.setProperty(PKGS_PROPERTY, pkg.substring(0, index) + (pkgs.isEmpty() ? "" : "|" + pkgs));
	}

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		return new TcpConnection(url);
	}
}