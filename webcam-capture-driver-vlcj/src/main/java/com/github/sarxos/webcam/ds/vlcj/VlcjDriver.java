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
import com.github.sarxos.webcam.WebcamDriver;
import com.sun.jna.Native;


/**
 * NOT STABLE, EXPERIMENTAL STUFF!!!
 * 
 * @author bfiryn
 */
public class VlcjDriver implements WebcamDriver {

	static {
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
	}

	private static List<WebcamDevice> devices = null;

	private static boolean loaded = false;

	private static MediaPlayerFactory mediaPlayerFactory = null;
	private static MediaDiscoverer videoMediaDiscoverer = null;
	private static MediaList videoDeviceList = null;

	@Override
	public List<WebcamDevice> getDevices() {

		if (!loaded) {
			mediaPlayerFactory = new MediaPlayerFactory();
			videoMediaDiscoverer = mediaPlayerFactory.newVideoMediaDiscoverer();
			videoDeviceList = videoMediaDiscoverer.getMediaList();
			loaded = true;
		}

		if (devices == null) {

			devices = new ArrayList<WebcamDevice>();

			List<MediaListItem> videoDevices = videoDeviceList.items();
			for (MediaListItem item : videoDevices) {
				devices.add(new VlcjDevice(item));
			}
		}

		return devices;
	}
}
