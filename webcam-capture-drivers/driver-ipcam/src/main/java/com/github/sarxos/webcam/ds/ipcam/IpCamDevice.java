package com.github.sarxos.webcam.ds.ipcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDevice.BufferAccess;
import com.github.sarxos.webcam.WebcamDevice.FPSSource;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.util.ImageUtils;
import com.github.sarxos.webcam.util.MjpegInputStream;


/**
 * IP camera device.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class IpCamDevice implements WebcamDevice, FPSSource, BufferAccess {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(IpCamDevice.class);

	private interface ImageReader extends FPSSource {

		BufferedImage readImage() throws InterruptedException;

		void halt();

		void start();
	}

	private final class PushImageReader extends Thread implements ImageReader {

		private final URI uri;
		private volatile boolean running = true;
		private volatile BufferedImage image = null;
		private BufferedImage tmp;
		private volatile double fps = 0;

		public PushImageReader(final URI uri) {
			this.uri = uri;
			this.setDaemon(true);
		}

		private MjpegInputStream request(final URI uri) {
			try {
				return new MjpegInputStream(get(uri, true));
			} catch (Exception e) {
				throw new WebcamException("Cannot download image. " + e.getMessage(), e);
			}
		}

		@Override
		public void run() {

			long t1;
			long t2;

			while (running) {
				try (final MjpegInputStream stream = request(uri)) {
					do {
						t1 = System.currentTimeMillis();
						if ((tmp = stream.readFrame()) != null) {
							image = tmp;
						}
						t2 = System.currentTimeMillis();
						fps = (double) 1000 / (t2 - t1 + 1);
					} while (running && !stream.isClosed());
				} catch (IOException e) {
					if (e instanceof EOFException) { // EOF, ignore error and recreate stream
						continue;
					}
					LOG.error("Cannot read MJPEG frame", e);
				}
			}
		}

		@Override
		public BufferedImage readImage() throws InterruptedException {
			while (running && image == null) {
				Thread.sleep(10);
			}
			return image;
		}

		@Override
		public void halt() {
			running = false;
		}

		@Override
		public double getFPS() {
			return fps;
		}
	}

	private final class PullImageReader implements ImageReader {

		private final URI uri;
		private double fps = 0;

		public PullImageReader(final URI uri) {
			this.uri = uri;
		}

		@Override
		public BufferedImage readImage() throws InterruptedException {

			long t1;
			long t2;

			t1 = System.currentTimeMillis();
			try (final InputStream is = request(uri)) {
				return ImageIO.read(is);
			} catch (IOException e) {
				throw new WebcamException(e);
			} finally {
				t2 = System.currentTimeMillis();
				fps = (double) 1000 / (t2 - t1 + 1);
			}
		}

		private InputStream request(final URI uri) {
			try {
				return get(uri, false);
			} catch (Exception e) {
				throw new WebcamException("Cannot download image", e);
			}
		}

		@Override
		public void halt() {
			// do nothing, no need to stop this reader
		}

		@Override
		public void start() {
			// do nothing, no need to start this one
		}

		@Override
		public double getFPS() {
			return fps;
		}
	}

	private final String name;
	private final URL url;
	private final IpCamMode mode;
	private final IpCamAuth auth;

	private final HttpClient client;
	private final HttpContext context;
	private ImageReader reader;

	private boolean open = false;

	private Dimension[] sizes = null;
	private Dimension size = null;

	public IpCamDevice(String name, String url, IpCamMode mode) throws MalformedURLException {
		this(name, new URL(url), mode, null);
	}

	public IpCamDevice(String name, URL url, IpCamMode mode) {
		this(name, url, mode, null);
	}

	public IpCamDevice(String name, String url, IpCamMode mode, IpCamAuth auth) throws MalformedURLException {
		this(name, new URL(url), mode, auth);
	}

	public IpCamDevice(String name, URL url, IpCamMode mode, IpCamAuth auth) {

		if (name == null) {
			throw new IllegalArgumentException("Name cannot be null");
		}

		this.name = name;
		this.url = url;
		this.mode = mode;
		this.auth = auth;
		this.client = createClient();
		this.context = createContext();

	}

	protected static final URL toURL(String url) {
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new WebcamException(String.format("Incorrect URL '%s'", url), e);
		}
	}

	private static final URI toURI(URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new WebcamException(e);
		}
	}

	public HttpClient getClient() {
		return client;
	}

	private HttpClient createClient() {
		return HttpClientBuilder.create().build();
	}

	private ImageReader createReader() {
		switch (mode) {
			case PULL:
				return new PullImageReader(toURI(url));
			case PUSH:
				return new PushImageReader(toURI(url));
			default:
				throw new WebcamException("Unsupported mode " + mode);
		}
	}

	private HttpContext createContext() {

		final IpCamAuth auth = getAuth();

		if (auth == null) {
			return null;
		}

		final URI uri = toURI(url);
		final HttpHost host = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
		final Credentials credentials = new UsernamePasswordCredentials(auth.getUserName(), auth.getPassword());
		final CredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(AuthScope.ANY, credentials);

		final AuthCache cache = new BasicAuthCache();
		cache.put(host, new BasicScheme());

		final HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(provider);
		context.setAuthCache(cache);

		return context;
	}

	private InputStream get(final URI uri, boolean withoutImageMime) throws UnsupportedOperationException, IOException {

		final HttpGet get = new HttpGet(uri);
		final HttpResponse respone = client.execute(get, context);
		final HttpEntity entity = respone.getEntity();

		// normal jpeg return image/jpeg as opposite to mjpeg

		if (withoutImageMime) {
			final Header contentType = entity.getContentType();
			if (contentType == null) {
				throw new WebcamException("Content Type header is missing");
			}
			if (contentType.getValue().startsWith("image/")) {
				throw new WebcamException("Cannot read images in PUSH mode, change mode to PULL " + contentType);
			}
		}

		return entity.getContent();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Dimension[] getResolutions() {

		if (sizes != null) {
			return sizes;
		}

		if (!open) {
			open();
		}

		int attempts = 0;
		do {
			final BufferedImage img = getImage();
			if (img != null) {
				sizes = new Dimension[] { new Dimension(img.getWidth(), img.getHeight()) };
				break;
			}
		} while (attempts++ < 5);

		close();

		if (sizes == null) {
			sizes = new Dimension[] { new Dimension(0, 0) };
		}

		return sizes;
	}

	protected void setSizes(Dimension[] sizes) {
		this.sizes = sizes;
	}

	@Override
	public Dimension getResolution() {
		if (size == null) {
			size = getResolutions()[0];
		}
		return size;
	}

	@Override
	public void setResolution(Dimension size) {
		this.size = size;
	}

	@Override
	public synchronized BufferedImage getImage() {
		if (!open) {
			return null;
		}
		try {
			return reader.readImage();
		} catch (InterruptedException e) {
			throw new WebcamException(e);
		}
	}

	/**
	 * This method will send HTTP HEAD request to the camera URL to check whether it's online or
	 * offline. It's online when this request succeed and it's offline if any exception occurs or
	 * response code is 404 Not Found.
	 *
	 * @return True if camera is online, false otherwise
	 */
	public boolean isOnline() {
		LOG.debug("Checking online status for {} at {}", getName(), getURL());
		try {
			return client
				.execute(new HttpHead(toURI(getURL())))
				.getStatusLine()
				.getStatusCode() != 404;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void open() {
		if (!open) {

			reader = createReader();
			reader.start();

			try {
				reader.readImage();
			} catch (InterruptedException e) {
				throw new WebcamException(e);
			}
		}
		open = true;
	}

	@Override
	public void close() {
		if (open) {
			reader.halt();
		}
		open = false;
	}

	public URL getURL() {
		return url;
	}

	public IpCamMode getMode() {
		return mode;
	}

	public IpCamAuth getAuth() {
		return auth;
	}

	@Override
	public void dispose() {
		// ignore
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public double getFPS() {
		return reader.getFPS();
	}

	/**
	 * Return image RGB data in form of {@link ByteBuffer}. Please note that {@link ByteBuffer}
	 * returned by this method does not contain original JPEG data bytes, but bytes representing RGB
	 * data of the image constructed from JPEG data.
	 */
	@Override
	public synchronized ByteBuffer getImageBytes() {
		final BufferedImage bi = getImage();
		if (bi == null) {
			return null;
		}
		return ByteBuffer.wrap(ImageUtils.imageToBytes(bi));
	}

	/**
	 * Put image RGB data into the {@link ByteBuffer}. Please note that data from {@link ByteBuffer}
	 * consumed by this method does not contain original JPEG data bytes, but bytes representing RGB
	 * data of the image constructed from JPEG data.
	 */
	@Override
	public void getImageBytes(ByteBuffer buffer) {
		final BufferedImage bi = getImage();
		if (bi != null) {
			buffer.put(ImageUtils.imageToBytes(bi));
		}
	}
}
