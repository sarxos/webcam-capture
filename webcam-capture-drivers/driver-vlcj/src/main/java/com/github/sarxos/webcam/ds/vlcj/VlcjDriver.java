package com.github.sarxos.webcam.ds.vlcj;

import java.util.ArrayList;
import java.util.List;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.medialist.MediaListItem;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.discoverer.MediaDiscoverer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.sun.jna.Native;


/**
 * This is capture driver which uses <code>vlcj</code> library to gain access to
 * the camera device.
 * 
 * @author Bartosz Firyn (SarXos)
 * @see http://www.capricasoftware.co.uk/projects/vlcj/index.html
 */
public class VlcjDriver implements WebcamDriver, WebcamDiscoverySupport {

	static {
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	}

	public VlcjDriver() {
		if (OS.getOS() == OS.WIN) {
			System.err.println(String.format("WARNING: %s does not support Windows platform", getClass().getSimpleName()));
		}
	}

	@Override
	public List<WebcamDevice> getDevices() {

		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
		MediaDiscoverer videoMediaDiscoverer = mediaPlayerFactory.newVideoMediaDiscoverer();
		MediaList videoDeviceList = videoMediaDiscoverer.getMediaList();

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		List<MediaListItem> videoDevices = videoDeviceList.items();
		for (MediaListItem item : videoDevices) {
			devices.add(new VlcjDevice(item));
		}

		videoDeviceList.release();
		videoMediaDiscoverer.release();
		mediaPlayerFactory.release();

		return devices;
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
		return 3000;
	}

	@Override
	public boolean isScanPossible() {
		return true;
	}
}
