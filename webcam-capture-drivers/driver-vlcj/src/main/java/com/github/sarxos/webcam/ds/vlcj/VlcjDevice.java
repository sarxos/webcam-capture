package com.github.sarxos.webcam.ds.vlcj;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.medialist.MediaListItem;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;


/**
 * Just a simple enumeration with supported (not yet confirmed) operating
 * systems.
 * 
 * @author Bartosz Firyn (sarxos)
 */
enum OS {

	/**
	 * Microsoft Windows
	 */
	WIN,

	/**
	 * Linux or UNIX.
	 */
	NIX,

	/**
	 * Mac OS X
	 */
	OSX;

	private static OS os = null;

	/**
	 * Get operating system.
	 * 
	 * @return OS
	 */
	public static final OS getOS() {
		if (os == null) {
			String osp = System.getProperty("os.name").toLowerCase();
			if (osp.indexOf("win") >= 0) {
				os = WIN;
			} else if (osp.indexOf("mac") >= 0) {
				os = OSX;
			} else if (osp.indexOf("nix") >= 0 || osp.indexOf("nux") >= 0) {
				os = NIX;
			} else {
				throw new RuntimeException(osp + " is not supported");
			}
		}
		return os;
	}

}

/**
 * Capture driver which use vlcj project API to fetch images from camera. It
 * should not be used when you need performance since vlcj saves snapshot image
 * to disk prior it is returned - this affects performance and drop FPS rate
 * down. In my case (HP Elitebook 8460p, 4 cores, 4 GB RAM, fast SSD disk) it
 * was about ~12 FPS, which is very low when you compare it to the other capture
 * drivers.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class VlcjDevice implements WebcamDevice {

	private static final Logger LOG = LoggerFactory.getLogger(VlcjDevice.class);

	/**
	 * Artificial view sizes. The vlcj is not able to detect resolutions
	 * supported by the webcam. If you would like to detect resolutions and have
	 * high-quality with good performance images streaming, you should rather
	 * use gstreamer or v4lvj capture drivers.
	 */
	private final static Dimension[] DIMENSIONS = new Dimension[] {
		WebcamResolution.QQVGA.getSize(),
		WebcamResolution.QVGA.getSize(),
		WebcamResolution.VGA.getSize(),
	};

	private final static String[] VLC_ARGS = {

		// VLC args by Andrew Davison:
		// http://fivedots.coe.psu.ac.th/~ad/jg/nui025/snapsWithoutJMF.pdf

		// no interface
		"--intf", "dummy",

		// no video output
		"--vout", "dummy",

		// no audio decoding
		"--no-audio",

		// do not display title
		"--no-video-title-show",

		// no stats
		"--no-stats",

		// no subtitles
		"--no-sub-autodetect-file",

		// no snapshot previews
		"--no-snapshot-preview",

		// reduce capture lag/latency
		"--live-caching=50",

		// turn off warnings
		"--quiet",
	};

	private Dimension size = null;
	private MediaListItem item = null;
	private MediaListItem sub = null;
	private MediaPlayerFactory factory = null;
	private MediaPlayer player = null;

	private volatile boolean open = false;
	private volatile boolean disposed = false;

	protected VlcjDevice(MediaListItem item) {

		if (item == null) {
			throw new IllegalArgumentException("Media list item cannot be null!");
		}

		List<MediaListItem> subs = item.subItems();

		if (subs.isEmpty()) {
			throw new RuntimeException("Implementation does not support media list items which are empty!");
		}

		this.item = item;
		this.sub = subs.get(0);
	}

	public String getCaptureDevice() {
		switch (OS.getOS()) {
			case WIN:
				return "dshow://";
			case OSX:
				return "qtcapture://";
			case NIX:
				return "v4l2://";
			default:
				throw new RuntimeException("Capture device not supported on " + OS.getOS());
		}
	}

	public MediaListItem getMediaListItem() {
		return item;
	}

	public MediaListItem getMediaListItemSub() {
		return sub;
	}

	@Override
	public String getName() {
		return sub.name();
	}

	public String getMRL() {
		return sub.mrl();
	}

	public String getVDevice() {
		return getMRL().replace(getCaptureDevice(), "");
	}

	@Override
	public String toString() {
		return String.format("%s[%s (%s)]", getClass().getSimpleName(), getName(), getMRL());
	}

	@Override
	public Dimension[] getResolutions() {
		return DIMENSIONS;
	}

	@Override
	public Dimension getResolution() {
		return size;
	}

	@Override
	public void setResolution(Dimension size) {
		this.size = size;
	}

	@Override
	public BufferedImage getImage() {
		if (!open) {
			throw new WebcamException("Cannot get image, webcam device is not open");
		}
		return player.getSnapshot();
	}

	@Override
	public synchronized void open() {

		if (disposed) {
			LOG.warn("Cannot open device because it has been disposed");
			return;
		}

		if (open) {
			return;
		}

		LOG.info("Opening webcam device");

		factory = new MediaPlayerFactory(VLC_ARGS);
		player = factory.newHeadlessMediaPlayer();

		// for nix systems this should be changed dshow -> ... !!

		String[] options = null;

		switch (OS.getOS()) {
			case WIN:
				options = new String[] {
					":dshow-vdev=" + getName(),
					":dshow-size=" + size.width + "x" + size.height,
					":dshow-adev=none", // no audio device
				};
				break;
			case NIX:
				options = new String[] {
					":v4l-vdev=" + getVDevice(),
					":v4l-width=" + size.width,
					":v4l-height=" + size.height,
					":v4l-fps=30",
					":v4l-quality=20",
					":v4l-adev=none", // no audio device
				};
				break;
			case OSX:
				options = new String[] {
					":qtcapture-vdev=" + getVDevice(),
					":qtcapture-width=" + size.width,
					":qtcapture-height=" + size.height,
					":qtcapture-adev=none", // no audio device
				};
				break;
		}

		player.startMedia(getMRL(), options);

		// wait for images

		int max = 0;
		do {

			BufferedImage im = player.getSnapshot(size.width, size.height);
			if (im != null && im.getWidth() > 0) {
				open = true;
				LOG.info("Webcam device is now open: " + getName());
				return;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

		} while (max++ < 10);

		player.release();
		factory.release();

		open = false;
	}

	@Override
	public synchronized void close() {

		if (!open) {
			return;
		}

		LOG.info("Closing");

		player.release();
		factory.release();

		open = false;
	}

	@Override
	public synchronized void dispose() {
		disposed = true;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	public MediaPlayer getPlayer() {
		return player;
	}

	public MediaPlayerFactory getFactory() {
		return factory;
	}
}
