package com.github.sarxos.webcam.ds.v4l4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.jcu.v4l4j.V4L4J;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.v4l4j.impl.VideoDeviceFilenameFilter;


/**
 * Capture driver for V4L4J framework. For more details on V4L4J please check <a
 * href="http://code.google.com/p/v4l4j">http://code.google.com/p/v4l4j</a>
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class V4l4jDriver implements WebcamDriver {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(V4l4jDriver.class);

	/**
	 * File filter used to find /dev/videoN files.
	 */
	private static final VideoDeviceFilenameFilter VIDEO_FILE_FILTER = new VideoDeviceFilenameFilter();

	/**
	 * Initialize customized V4L4J libraries.
	 */
	static {
		try {
			Class.forName("au.edu.jcu.v4l4j.V4L4J");
			V4L4J.init();
		} catch (ClassNotFoundException e) {
			LOG.warn("Modified V4L4J has not been found in classpath");
		}
	}

	@Override
	public List<WebcamDevice> getDevices() {

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();
		File[] vfiles = VIDEO_FILE_FILTER.getVideoFiles();

		if (LOG.isDebugEnabled()) {
			for (File vfile : vfiles) {
				LOG.debug("Video file detected {}", vfile);
			}
		}

		for (File vfile : vfiles) {
			devices.add(new V4l4jDevice(vfile));
		}

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
}
