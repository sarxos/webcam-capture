package com.github.sarxos.webcam.ds.vlcj;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.util.OsUtils;

import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.medialist.MediaListItem;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.discoverer.MediaDiscoverer;


/**
 * This is capture driver which uses <code>vlcj</code> library to gain access to the camera device.
 * The library can be found at:<br>
 * <br>
 * http://www.capricasoftware.co.uk/projects/vlcj/index.html
 *
 * @author Bartosz Firyn (SarXos)
 */
public class VlcjDriver implements WebcamDriver, WebcamDiscoverySupport {

	/**
	 * I'm the logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(VlcjDriver.class);

	static {
		if ("true".equals(System.getProperty("webcam.debug"))) {
			System.setProperty("vlcj.log", "DEBUG");
		}
	}

	/**
	 * Are natives initialized.
	 */
	private static final AtomicBoolean initialized = new AtomicBoolean();

	/**
	 * Native library discoverer.
	 */
	private static NativeDiscovery nativeDiscovery;

	/**
	 * The scan interval.
	 */
	private long scanInterval = -1;

	/**
	 * Preconfigured media list items.
	 */
	private final List<MediaListItem> mediaListItems;

	public VlcjDriver() {
		this(null);
	}

	public VlcjDriver(List<MediaListItem> mediaListItems) {
		this.mediaListItems = mediaListItems;
		initialize();
	}

	/**
	 * Initialize natives.
	 */
	protected static void initialize() {
		initialize(true);
	}

	/**
	 * Initialize natives. If argument is true the natives are being loaded. In case of false this
	 * method do nothing. It's used mostly in unit tests.
	 *
	 * @param load the control to decide whether to load natives or ignore them
	 */
	protected static void initialize(boolean load) {
		if (load && initialized.compareAndSet(false, true)) {
			boolean nativeFound = getNativeDiscovery().discover();
			if (!nativeFound) {
				throw new IllegalStateException("The libvlc native library has not been found");
			}
			// Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		}
	}

	@Override
	public List<WebcamDevice> getDevices() {

		LOG.debug("Searching devices");

		if (OsUtils.getOS() == OsUtils.WIN) {
			System.err.println("WARNING: VLCj does not support webcam devices discovery on Windows platform");
		}

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		if (mediaListItems != null) {

			for (MediaListItem item : mediaListItems) {
				devices.add(mediaListItemToDevice(item));
			}

		} else {

			MediaPlayerFactory mediaPlayerFactory = createMediaPlayerFactory();
			MediaDiscoverer videoMediaDiscoverer = mediaPlayerFactory.newVideoMediaDiscoverer();
			MediaList videoDeviceList = videoMediaDiscoverer.getMediaList();
			List<MediaListItem> videoDevices = videoDeviceList.items();

			for (MediaListItem item : videoDevices) {

				LOG.debug("Found item {}", item);

				devices.add(mediaListItemToDevice(item));
			}

			videoDeviceList.release();
			videoMediaDiscoverer.release();
			mediaPlayerFactory.release();
		}

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
	 * Set new scan interval. Value must be positive number. If negative or zero is used, then the
	 * corresponding getter will return default scan interval value.
	 *
	 * @param scanInterval the new scan interval in milliseconds
	 * @see VlcjDriver#DEFAULT_SCAN_INTERVAL
	 */
	public void setScanInterval(long scanInterval) {
		this.scanInterval = scanInterval;
	}

	@Override
	public boolean isScanPossible() {
		return OsUtils.getOS() != OsUtils.WIN;
	}

	protected static NativeDiscovery getNativeDiscovery() {
		if (nativeDiscovery == null) {
			nativeDiscovery = new NativeDiscovery();
		}
		return nativeDiscovery;
	}
}
