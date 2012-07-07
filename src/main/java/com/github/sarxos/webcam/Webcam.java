package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.ResourceUnavailableEvent;
import javax.media.StartEvent;
import javax.media.control.FormatControl;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Webcam class.
 * 
 * @author Bartosz Firyn (bfiryn)
 */
public class Webcam {

	private static final Logger LOG = LoggerFactory.getLogger(Webcam.class);

	/**
	 * Control to control format.
	 */
	private final static String FORMAT_CTRL = "javax.media.control.FormatControl";

	/**
	 * Control to grab frames.
	 */
	private final static String GRABBING_CTRL = "javax.media.control.FrameGrabbingControl";

	/**
	 * Player starter.
	 */
	private class PlayerStarter extends Thread implements ControllerListener {

		public PlayerStarter(Player player) {
			setDaemon(true);
			player.addControllerListener(this);
			player.start();
		}

		@Override
		public void run() {

			// wait for start
			while (!started) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				started = player.getState() == Controller.Started;
				if (started) {
					break;
				}
			}

			// try to grab single image (wait 10 seconds)
			for (int i = 0; i < 100; i++) {
				Buffer buffer = grabber.grabFrame();
				if (buffer.getLength() == 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			semaphore.release();
		}

		@Override
		public void controllerUpdate(ControllerEvent ce) {
			if (ce instanceof StartEvent) {
				available = true;
				semaphore.release();
			}
			if (ce instanceof ResourceUnavailableEvent) {
				available = false;
				semaphore.release();
			}
		}
	}

	/**
	 * Webcam listeners.
	 */
	private List<WebcamListener> listeners = new ArrayList<WebcamListener>();

	/**
	 * Is webcam open.
	 */
	private volatile boolean open = false;

	/**
	 * Is player available.
	 */
	private volatile boolean available = false;

	/**
	 * Is player started.
	 */
	private volatile boolean started = false;

	private CaptureDeviceInfo device = null;
	private MediaLocator locator = null;
	private Player player = null;
	private VideoFormat format = null;
	private FormatControl control = null;
	private FrameGrabbingControl grabber = null;
	private BufferToImage converter = null;

	private PlayerStarter starter = null;
	private Semaphore semaphore = new Semaphore(0, true);

	/**
	 * Webcam class.
	 * 
	 * @param device - device to be used as webcam
	 */
	public Webcam(CaptureDeviceInfo device) {
		this.device = device;
	}

	/**
	 * Open webcam.
	 */
	public synchronized void open() {

		if (isOpen()) {
			LOG.debug("Opening webcam - already open!");
			return;
		}

		LOG.debug("Opening webcam");

		locator = device.getLocator();
		format = getVideoFormat(device);
		converter = new BufferToImage(format);

		try {
			player = Manager.createRealizedPlayer(locator);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		control = (FormatControl) getControl(FORMAT_CTRL);
		grabber = (FrameGrabbingControl) getControl(GRABBING_CTRL);

		if (control.setFormat(format) == null) {
			throw new RuntimeException("Cannot change video format");
		}

		starter = new PlayerStarter(player);
		starter.start();

		try {
			semaphore.acquire(2);
			starter.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		open = started && available;

		WebcamEvent we = new WebcamEvent(this);

		synchronized (listeners) {
			Iterator<WebcamListener> li = listeners.iterator();
			while (li.hasNext()) {
				WebcamListener l = li.next();
				try {
					l.webcamOpen(we);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Close webcam.
	 */
	public synchronized void close() {

		if (!started && !available && !open) {
			LOG.debug("Cannot close webcam");
			return;
		}

		LOG.debug("Closing webcam");

		open = false;

		if (started || available) {

			started = false;
			available = false;

			player.stop();
			player.close();
			player.deallocate();
		}

		WebcamEvent we = new WebcamEvent(this);
		for (WebcamListener l : listeners) {
			try {
				l.webcamClosed(we);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get player control.
	 * 
	 * @param name control name
	 * @return Player control
	 */
	private Control getControl(String name) {
		Control control = player.getControl(name);
		if (control == null) {
			throw new RuntimeException("Cannot find format control " + name);
		}
		return control;
	}

	/**
	 * @return Webcam view size (picture size) in pixels.
	 */
	public Dimension getViewSize() {
		if (!isOpen()) {
			throw new RuntimeException("Webcam has to be open to get video size");
		}
		return format.getSize();
	}

	/**
	 * Get suitable video format to use (the largest one by default, but this
	 * can be easily changed).
	 * 
	 * @param device device to get video format for
	 * @return Suitable video format
	 */
	protected VideoFormat getVideoFormat(CaptureDeviceInfo device) {

		Format[] formats = device.getFormats();
		VideoFormat format = null;
		int area = 0;

		// find the largest picture format
		for (Format f : formats) {

			if (!(f instanceof VideoFormat) || !"RGB".equalsIgnoreCase(f.getEncoding())) {
				continue;
			}

			VideoFormat vf = (VideoFormat) f;
			Dimension dim = vf.getSize();

			int a = dim.width * dim.height;
			if (a > area) {
				area = a;
				format = vf;
			}
		}

		return format;
	}

	/**
	 * @return Capturing device
	 */
	public CaptureDeviceInfo getDevice() {
		return device;
	}

	/**
	 * Is webcam open?
	 * 
	 * @return true if open, false otherwise
	 */
	public synchronized boolean isOpen() {
		return open;
	}

	/**
	 * Capture image from webcam.
	 * 
	 * @return Captured image
	 */
	public synchronized BufferedImage getImage() {

		if (!isOpen()) {
			throw new RuntimeException("Webcam has to be open to get image");
		}

		Buffer buffer = grabber.grabFrame();
		Image image = converter.createImage(buffer);

		if (image == null) {
			throw new RuntimeException("Cannot get image");
		}

		int width = image.getWidth(null);
		int height = image.getHeight(null);
		int type = BufferedImage.TYPE_INT_RGB;

		BufferedImage buffered = new BufferedImage(width, height, type);

		Graphics2D g2 = buffered.createGraphics();
		g2.drawImage(image, null, null);
		g2.dispose();
		buffered.flush();

		return buffered;

	}

	/**
	 * Get list of webcams to use.
	 * 
	 * @return List of webcams
	 */
	public static List<Webcam> getWebcams() {

		@SuppressWarnings("unchecked")
		Vector<Object> devices = CaptureDeviceManager.getDeviceList(new Format("RGB"));

		List<Webcam> webcams = new ArrayList<Webcam>();
		Iterator<Object> di = devices.iterator();
		while (di.hasNext()) {
			CaptureDeviceInfo device = (CaptureDeviceInfo) di.next();
			webcams.add(new Webcam(device));
		}

		return webcams;
	}

	/**
	 * @return Default (first) webcam.
	 */
	public static Webcam getDefault() {
		List<Webcam> webcams = getWebcams();
		if (webcams.isEmpty()) {
			return null;
		}
		return webcams.get(0);
	}

	/**
	 * Get webcam name (actually device name).
	 * 
	 * @return Name
	 */
	public String getName() {
		return device.getName();
	}

	@Override
	public String toString() {
		return "webcam:" + getName();
	}

	/**
	 * Add webcam listener.
	 * 
	 * @param l a listener to add
	 */
	public void addWebcamListener(WebcamListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	/**
	 * @return All webcam listeners
	 */
	public WebcamListener[] getWebcamListeners() {
		synchronized (listeners) {
			return listeners.toArray(new WebcamListener[listeners.size()]);
		}
	}
}
