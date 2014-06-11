package com.github.sarxos.webcam.ds.vlcj;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.medialist.MediaListItem;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.discoverer.MediaDiscoverer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.vlcj.impl.OS;
import com.sun.jna.Native;


/**
 * This is capture driver which uses <code>vlcj</code> library to gain access to
 * the camera device.
 * 
 * @author Bartosz Firyn (SarXos)
 * @see http://www.capricasoftware.co.uk/projects/vlcj/index.html
 */
public class VlcjDriver implements WebcamDriver, WebcamDiscoverySupport {

	/**
	 * Default webcam discovery scan interval in milliseconds.
	 */
	public static final long DEFAULT_SCAN_INTERVAL = 3000;

	/**
	 * Are natives initialized.
	 */
	private static final AtomicBoolean initialized = new AtomicBoolean();

	/**
	 * The scan interval.
	 */
	private long scanInterval = -1;

	public VlcjDriver() {
		if (OS.getOS() == OS.WIN) {
			System.err.println(String.format("WARNING: %s does not support Windows platform", getClass().getSimpleName()));
		}
		initialize();
	}

	/**
	 * Initialize natives.
	 */
	protected static void initialize() {
		initialize(true);
	}

	/**
	 * Initialize natives. If argument is true the natives are being loaded. In
	 * case of false this method do nothing. It's used mostly in unit tests.
	 * 
	 * @param load the control to decide whether to load natives or ignore them
	 */
	protected static void initialize(boolean load) {
		if (load && initialized.compareAndSet(false, true)) {
			Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		}
	}

	@Override
	public final List<WebcamDevice> getDevices() {

		MediaPlayerFactory mediaPlayerFactory = createMediaPlayerFactory();
		MediaDiscoverer videoMediaDiscoverer = mediaPlayerFactory.newVideoMediaDiscoverer();
		MediaList videoDeviceList = videoMediaDiscoverer.getMediaList();

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		List<MediaListItem> videoDevices = videoDeviceList.items();

		for (MediaListItem item : videoDevices) {
			devices.add(mediaListItemToDevice(item));
		}

		videoDeviceList.release();
		videoMediaDiscoverer.release();
		mediaPlayerFactory.release();

		return devices;
	}

	/**
	 * Converts media list itemn into webcam device.
	 * 
	 * @param item the item to be converted to webcam device instance
	 * @return Webcam device created from media list item
	 */
	protected WebcamDevice mediaListItemToDevice(MediaListItem item) {
		return new VlcjDevice(item);
	}

	/**
	 * Creates media player factory.
	 * 
	 * @return New media player factory
	 */
	protected MediaPlayerFactory createMediaPlayerFactory() {
		return new MediaPlayerFactory();
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
	public long getScanInterval() {
		if (scanInterval <= 0) {
			return DEFAULT_SCAN_INTERVAL;
		}
		return scanInterval;
	}

	/**
	 * Set new scan interval. Value must be positive number. If negative or zero
	 * is used, then the corresponding getter will return default scan interval
	 * value.
	 * 
	 * @param scanInterval the new scan interval in milliseconds
	 * @see VlcjDriver#DEFAULT_SCAN_INTERVAL
	 */
	public void setScanInterval(long scanInterval) {
		this.scanInterval = scanInterval;
	}

	@Override
	public boolean isScanPossible() {
		return true;
	}
}
