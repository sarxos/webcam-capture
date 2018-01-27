package com.github.sarxos.webcam.ds.mjpeg;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.mjpeg.tcp.TcpConnectionRegistrar;


/**
 * This is capture driver which returns a list of {@link MjpegCaptureDevice} instances which can be
 * used by {@link Webcam} to stream images feed from. To have {@link MjpegCaptureDevice} instances
 * returned one have to register MJPEG {@link URI}s first.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class MjpegCaptureDriver implements WebcamDriver {

	static {
		TcpConnectionRegistrar.register();
	}

	private final Set<URL> urls = new LinkedHashSet<>();

	public MjpegCaptureDriver() {
	}

	public MjpegCaptureDriver(final String uri) {
		urls.add(toUrl(uri));
	}

	public MjpegCaptureDriver(final Collection<String> uris) {
		for (final String uri : uris) {
			urls.add(toUrl(uri));
		}
	}

	public MjpegCaptureDriver withUri(final String uri) {
		urls.add(toUrl(uri));
		return this;
	}

	@Override
	public List<WebcamDevice> getDevices() {
		final List<WebcamDevice> devices = new ArrayList<>();
		for (final URL url : urls) {
			devices.add(new MjpegCaptureDevice(url));
		}
		return devices;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	private URL toUrl(String uri) {
		try {
			return URI.create(uri).toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Wrong URI " + uri, e);
		}
	}

}
