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

import javax.imageio.ImageIO;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.ipcam.impl.IpCamHttpClient;
import com.github.sarxos.webcam.ds.ipcam.impl.IpCamMJPEGStream;


/**
 * IP camera device.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class IpCamDevice implements WebcamDevice {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(IpCamDevice.class);

	private final class PushImageReader implements Runnable {

		private final Object lock = new Object();
		private IpCamMJPEGStream stream = null;
		private BufferedImage image = null;
		private boolean running = true;
		private WebcamException exception = null;
		private HttpGet get = null;
		private URI uri = null;

		public PushImageReader(URI uri) {
			this.uri = uri;
			stream = new IpCamMJPEGStream(requestStream(uri));
		}

		private InputStream requestStream(URI uri) {

			BasicHttpContext context = new BasicHttpContext();

			IpCamAuth auth = getAuth();
			if (auth != null) {
				AuthCache cache = new BasicAuthCache();
				cache.put(new HttpHost(uri.getHost()), new BasicScheme());
				context.setAttribute(ClientContext.AUTH_CACHE, cache);
			}

			try {
				get = new HttpGet(uri);

				HttpResponse respone = client.execute(get, context);
				HttpEntity entity = respone.getEntity();

				Header ct = entity.getContentType();
				if (ct == null) {
					throw new WebcamException("Content Type header is missing");
				}

				if (ct.getValue().startsWith("image/")) {
					throw new WebcamException("Cannot read images in PUSH mode, change mode to PULL");
				}

				return entity.getContent();

			} catch (Exception e) {
				throw new WebcamException("Cannot download image", e);
			}
		}

		@Override
		public void run() {

			while (running) {

				if (stream.isClosed()) {
					break;
				}

				try {

					LOG.trace("Reading MJPEG frame");

					BufferedImage image = stream.readFrame();

					if (image != null) {
						this.image = image;
						synchronized (lock) {
							lock.notifyAll();
						}
					}

				} catch (IOException e) {

					// case when someone manually closed stream, do not log
					// exception, this is normal behavior

					if (stream.isClosed()) {
						LOG.debug("Stream already closed, returning");
						return;
					}

					if (e instanceof EOFException) {

						LOG.debug("EOF detected, recreating MJPEG stream");

						get.releaseConnection();

						try {
							stream.close();
						} catch (IOException ioe) {
							throw new WebcamException(ioe);
						}

						stream = new IpCamMJPEGStream(requestStream(uri));

						continue;
					}

					LOG.error("Cannot read MJPEG frame", e);

					if (failOnError) {
						exception = new WebcamException("Cannot read MJPEG frame", e);
						throw exception;
					}
				}
			}

			try {
				stream.close();
			} catch (IOException e) {
				LOG.debug("Some nasty exception when closing MJPEG stream", e);
			}

		}

		public BufferedImage getImage() {
			if (exception != null) {
				throw exception;
			}
			if (image == null) {
				try {
					synchronized (lock) {
						lock.wait();
					}
				} catch (InterruptedException e) {
					throw new WebcamException("Reader thread interrupted", e);
				} catch (Exception e) {
					throw new RuntimeException("Problem waiting on lock", e);
				}
			}
			return image;
		}

		public void stop() {
			running = false;
		}
	}

	private String name = null;
	private URL url = null;
	private IpCamMode mode = null;
	private IpCamAuth auth = null;
	private IpCamHttpClient client = new IpCamHttpClient();
	private PushImageReader pushReader = null;
	private boolean failOnError = false;

	private volatile boolean open = false;
	private volatile boolean disposed = false;

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

		if (auth != null) {
			AuthScope scope = new AuthScope(new HttpHost(url.getHost().toString()));
			client.getCredentialsProvider().setCredentials(scope, auth);
		}
	}

	protected static final URL toURL(String url) {

		String base = null;
		if (url.startsWith("http://")) {
			base = url;
		} else {
			base = String.format("http://%s", url);
		}

		try {
			return new URL(base);
		} catch (MalformedURLException e) {
			throw new WebcamException(String.format("Incorrect URL '%s'", url), e);
		}
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
			BufferedImage img = getImage();
			if (img != null) {
				sizes = new Dimension[] { new Dimension(img.getWidth(), img.getHeight()) };
				break;
			}
		} while (attempts++ < 5);

		close();

		if (sizes == null) {
			throw new WebcamException("Cannot get initial image from IP camera device " + getName());
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
			throw new WebcamException("IpCam device not open");
		}

		switch (mode) {
			case PULL:
				return getImagePullMode();
			case PUSH:
				return getImagePushMode();
		}

		throw new WebcamException(String.format("Unsupported mode %s", mode));
	}

	private BufferedImage getImagePushMode() {

		if (pushReader == null) {

			URI uri = null;
			try {
				uri = getURL().toURI();
			} catch (URISyntaxException e) {
				throw new WebcamException(String.format("Incorrect URI syntax '%s'", uri), e);
			}

			pushReader = new PushImageReader(uri);

			// TODO: change to executor

			Thread thread = new Thread(pushReader, String.format("%s-reader", getName()));
			thread.setDaemon(true);
			thread.start();
		}

		return pushReader.getImage();
	}

	private BufferedImage getImagePullMode() {

		synchronized (this) {

			HttpGet get = null;
			URI uri = null;

			try {
				uri = getURL().toURI();
			} catch (URISyntaxException e) {
				throw new WebcamException(String.format("Incorrect URI syntax '%s'", uri), e);
			}

			BasicHttpContext context = new BasicHttpContext();

			IpCamAuth auth = getAuth();
			if (auth != null) {
				AuthCache cache = new BasicAuthCache();
				cache.put(new HttpHost(uri.getHost()), new BasicScheme());
				context.setAttribute(ClientContext.AUTH_CACHE, cache);
			}

			try {
				get = new HttpGet(uri);

				HttpResponse respone = client.execute(get, context);
				HttpEntity entity = respone.getEntity();

				Header ct = entity.getContentType();
				if (ct == null) {
					throw new WebcamException("Content Type header is missing");
				}

				if (ct.getValue().startsWith("multipart/")) {
					throw new WebcamException("Cannot read MJPEG stream in PULL mode, change mode to PUSH");
				}

				InputStream is = entity.getContent();
				if (is == null) {
					return null;
				}

				return ImageIO.read(is);

			} catch (IOException e) {

				// fall thru, it means we closed stream
				if (e.getMessage().equals("closed")) {
					return null;
				}

				throw new WebcamException("Cannot download image", e);

			} catch (Exception e) {
				throw new WebcamException("Cannot download image", e);
			} finally {
				if (get != null) {
					get.releaseConnection();
				}
			}
		}
	}

	/**
	 * This method will send HTTP HEAD request to the camera URL to check
	 * whether it's online or offline. It's online when this request succeed and
	 * it's offline if any exception occurs or response code is 404 Not Found.
	 * 
	 * @return True if camera is online, false otherwise
	 */
	public boolean isOnline() {

		LOG.debug("Checking online status for {} at {}", getName(), getURL());

		URI uri = null;
		try {
			uri = getURL().toURI();
		} catch (URISyntaxException e) {
			throw new WebcamException(String.format("Incorrect URI syntax '%s'", uri), e);
		}

		HttpHead head = new HttpHead(uri);

		HttpResponse response = null;
		try {
			response = client.execute(head);
		} catch (Exception e) {
			return false;
		} finally {
			if (head != null) {
				head.releaseConnection();
			}
		}

		return response.getStatusLine().getStatusCode() != 404;
	}

	@Override
	public void open() {
		if (disposed) {
			LOG.warn("Device cannopt be open because it's already disposed");
			return;
		}
		open = true;
	}

	@Override
	public void close() {

		if (!open) {
			return;
		}

		if (pushReader != null) {
			pushReader.stop();
			pushReader = null;
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

	public void setAuth(IpCamAuth auth) {
		if (auth != null) {
			URL url = getURL();
			AuthScope scope = new AuthScope(url.getHost(), url.getPort());
			client.getCredentialsProvider().setCredentials(scope, auth);
		}
	}

	public void resetAuth() {
		client.getCredentialsProvider().clear();
	}

	public void setFailOnError(boolean failOnError) {
		this.failOnError = failOnError;
	}

	@Override
	public void dispose() {
		disposed = true;
	}

	@Override
	public boolean isOpen() {
		return open;
	}
}
