package com.github.sarxos.webcam.ds.jmf;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
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

import com.github.sarxos.webcam.WebcamDevice;


/**
 * Webcam device - JMF and FMJ implementation.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class JmfDevice implements WebcamDevice {

	private static final Logger LOG = LoggerFactory.getLogger(JmfDevice.class);

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

	private volatile boolean disposed = false;

	private PlayerStarter starter = null;
	private Semaphore semaphore = new Semaphore(0, true);

	private CaptureDeviceInfo cdi = null;
	private List<Dimension> dimensions = null;
	private Dimension dimension = null;

	private CaptureDeviceInfo device = null;
	private MediaLocator locator = null;
	private Player player = null;
	private VideoFormat format = null;
	private FormatControl control = null;
	private FrameGrabbingControl grabber = null;
	private BufferToImage converter = null;

	private Dimension viewSize = null;

	public JmfDevice(CaptureDeviceInfo cdi) {
		this.cdi = cdi;
	}

	@Override
	public String getName() {
		return cdi.getName();
	}

	private VideoFormat createFormat(Dimension size) {
		if (size == null) {
			return getLargestVideoFormat();
		} else {
			return getSizedVideoFormat(size);
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
	 * Get video format for size.
	 * 
	 * @param device device to get format from
	 * @param size specific size to search
	 * @return VideoFormat
	 */
	private VideoFormat getSizedVideoFormat(Dimension size) {

		Format[] formats = device.getFormats();
		VideoFormat format = null;

		for (Format f : formats) {

			if (!"RGB".equalsIgnoreCase(f.getEncoding()) || !(f instanceof VideoFormat)) {
				continue;
			}

			Dimension d = ((VideoFormat) f).getSize();
			if (d.width == size.width && d.height == size.height) {
				format = (VideoFormat) f;
				break;
			}
		}

		return format;
	}

	/**
	 * Get suitable video format to use (the largest one by default, but this
	 * can be easily changed).
	 * 
	 * @param device device to get video format for
	 * @return Suitable video format
	 */
	private VideoFormat getLargestVideoFormat() {

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

	@Override
	public Dimension[] getResolutions() {

		if (dimensions == null) {
			dimensions = new ArrayList<Dimension>();

			Format[] formats = cdi.getFormats();
			for (Format format : formats) {
				if ("RGB".equalsIgnoreCase(format.getEncoding())) {
					dimensions.add(((VideoFormat) format).getSize());
				}
			}

			Collections.sort(dimensions, new Comparator<Dimension>() {

				@Override
				public int compare(Dimension a, Dimension b) {
					int apx = a.width * a.height;
					int bpx = b.width * b.height;
					if (apx > bpx) {
						return 1;
					} else if (apx < bpx) {
						return -1;
					} else {
						return 0;
					}
				}
			});
		}

		return dimensions.toArray(new Dimension[dimensions.size()]);
	}

	@Override
	public Dimension getResolution() {
		return dimension;
	}

	@Override
	public void setResolution(Dimension size) {
		this.dimension = size;
	}

	@Override
	public BufferedImage getImage() {

		if (!open) {
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

	@Override
	public void open() {

		if (disposed) {
			LOG.warn("Cannot open device because it's already disposed");
			return;
		}

		if (open) {
			LOG.debug("Opening webcam - already open!");
			return;
		}

		LOG.debug("Opening webcam");

		locator = device.getLocator();
		format = createFormat(viewSize);
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
		} catch (InterruptedException e) {
		}

		do {
			BufferedImage image = getImage();
			if (image != null) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		} while (true);

		open = started && available;
	}

	@Override
	public void close() {

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
