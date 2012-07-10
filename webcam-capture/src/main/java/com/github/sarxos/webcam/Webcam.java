package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


/**
 * Webcam class.
 * 
 * @author Bartosz Firyn (bfiryn)
 */
public class Webcam {

	private static WebcamDataSource dataSource = null;
	private static List<Webcam> webcams = null;

	/**
	 * Webcam listeners.
	 */
	private List<WebcamListener> listeners = new ArrayList<WebcamListener>();

	private WebcamDevice device = null;
	private boolean open = false;

	/**
	 * Webcam class.
	 * 
	 * @param device - device to be used as webcam
	 */
	public Webcam(WebcamDevice device) {
		this.device = device;
	}

	/**
	 * Open webcam.
	 */
	public synchronized void open() {

		device.open();
		open = true;

		WebcamEvent we = new WebcamEvent(this);
		for (WebcamListener l : listeners) {
			try {
				l.webcamOpen(we);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Close webcam.
	 */
	public synchronized void close() {

		device.close();
		open = false;

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
	 * Is webcam open?
	 * 
	 * @return true if open, false otherwise
	 */
	public synchronized boolean isOpen() {
		return open;
	}

	/**
	 * @return Webcam view size (picture size) in pixels.
	 */
	public Dimension getViewSize() {
		return device.getSize();
	}

	public Dimension[] getViewSizes() {
		return device.getSizes();
	}

	public void setViewSize(Dimension size) {

		// check if dimension is valid one
		boolean ok = false;
		Dimension[] sizes = getViewSizes();
		for (Dimension d : sizes) {
			if (d.width == size.width && d.height == size.height) {
				ok = true;
				break;
			}
		}

		if (!ok) {
			StringBuilder sb = new StringBuilder("Incorrect dimension [");
			sb.append(size.width).append("x").append(size.height).append("] ");
			sb.append("possible ones are ");
			for (Dimension d : sizes) {
				sb.append("[").append(d.width).append("x").append(d.height).append("] ");
			}
			throw new IllegalArgumentException(sb.toString());
		}

		device.setSize(size);
	}

	/**
	 * Capture image from webcam.
	 * 
	 * @return Captured image
	 */
	public synchronized BufferedImage getImage() {
		return device.getImage();
	}

	/**
	 * Get list of webcams to use.
	 * 
	 * @return List of webcams
	 */
	public static List<Webcam> getWebcams() {
		if (webcams == null) {
			webcams = new ArrayList<Webcam>();
			for (WebcamDevice device : dataSource.getDevices()) {
				webcams.add(new Webcam(device));
			}
		}
		return webcams;
	}

	/**
	 * @return Default webcam (first from the list)
	 */
	public static Webcam getDefault() {
		List<Webcam> webcams = getWebcams();
		if (webcams.isEmpty()) {
			throw new WebcamException("No webcam available in the system");
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

	/**
	 * @return Data source currently used by webcam
	 */
	public static WebcamDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Set new data source to be used by webcam.
	 * 
	 * @param ds new data source to use (e.g. Civil, JFM, FMJ, QTJ, etc)
	 */
	public static void setDataSource(WebcamDataSource ds) {
		Webcam.dataSource = ds;
	}
}
