package com.github.sarxos.webcam.ds.fswebcam;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.fswebcam.impl.VideoDeviceFilenameFilter;


public class FsWebcamDriver implements WebcamDriver, WebcamDiscoverySupport {

	private static final Logger LOG = LoggerFactory.getLogger(FsWebcamDriver.class);

	private static final VideoDeviceFilenameFilter VFFILTER = new VideoDeviceFilenameFilter();

	private long scanInterval;
	
	@Override
	public List<WebcamDevice> getDevices() {

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		for (File vfile : VFFILTER.getVideoFiles()) {
			LOG.info("Found video file {}", vfile);
			devices.add(new FsWebcamDevice(vfile));
		}

		return devices;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	/**
	 * @see FsWebcamDriver#getScanInterval()
	 */
	public void setScanInterval(long scanInterval) {
		this.scanInterval = scanInterval;
	}

	@Override
	public long getScanInterval() {
		return scanInterval;
	}

	@Override
	public boolean isScanPossible() {
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
