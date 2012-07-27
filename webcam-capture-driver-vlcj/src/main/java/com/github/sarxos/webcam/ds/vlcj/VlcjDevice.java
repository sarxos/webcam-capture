package com.github.sarxos.webcam.ds.vlcj;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.medialist.MediaListItem;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;


/**
 * NOT STABLE, EXPERIMENTAL STUFF!!!
 * 
 * @author bfiryn
 */
public class VlcjDevice implements WebcamDevice {

	private static final Logger LOG = LoggerFactory.getLogger(VlcjDevice.class);

	/**
	 * Artificial view sizes. In future this should be read from media item.
	 */
	private final static Dimension[] DIMENSIONS = new Dimension[] {
		new Dimension(320, 240),
	};

	private final static String[] VLC_ARGS = {
		// ... have no idea what is this ...
		"--intf",

		// no interface
		"dummy",

		// ... have no idea what is this ...
		"--vout",

		// no video output
		"dummy",

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
	private MediaPlayerFactory factory = null;
	private MediaPlayer player = null;
	private boolean open = false;

	public VlcjDevice(MediaListItem item) {
		this.item = item;
	}

	private String getCapDevice() {

		String os = System.getProperty("os.name").toLowerCase();

		if (os.indexOf("win") >= 0) {
			return "dshow://";
		} else if (os.indexOf("mac") >= 0) {
			throw new RuntimeException("Not implemented");
		} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
			return "v4l2://";
		} else if (os.indexOf("sunos") >= 0) {
			throw new RuntimeException("Not implemented");
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	@Override
	public String getName() {
		return item.name();
	}

	@Override
	public Dimension[] getSizes() {
		return DIMENSIONS;
	}

	@Override
	public Dimension getSize() {
		return size;
	}

	@Override
	public void setSize(Dimension size) {
		this.size = size;
	}

	@Override
	public BufferedImage getImage() {

		if (!open) {
			throw new WebcamException("Cannot get image, player is not open");
		}

		return player.getSnapshot();
	}

	@Override
	public void open() {

		if (open) {
			return;
		}

		LOG.info("Opening");

		factory = new MediaPlayerFactory(VLC_ARGS);
		player = factory.newHeadlessMediaPlayer();

		// for nix systems this should be changed dshow -> ... !!

		String[] options = {
			":dshow-vdev=" + item.name(),
			":dshow-size=" + size.width + "x" + size.height,
			":dshow-adev=none", // no audio device
		};

		player.startMedia(getCapDevice(), options);

		// wait for images

		int max = 0;
		do {

			BufferedImage im = player.getSnapshot(size.width, size.height);
			if (im != null && im.getWidth() > 0) {
				open = true;
				LOG.info("Device has been open: " + getName());
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
	public void close() {

		if (!open) {
			return;
		}

		LOG.info("Closing");

		player.release();
		factory.release();

		open = false;
	}

}
