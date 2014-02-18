package com.github.sarxos.webcam.ds.gstreamer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.interfaces.PropertyProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.gstreamer.impl.VideoDeviceFilenameFilter;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;


/**
 * GStreamer capture driver.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public class GStreamerDriver implements WebcamDriver {

	private static final Logger LOG = LoggerFactory.getLogger(GStreamerDriver.class);

	private static final class GStreamerShutdownHook extends Thread {

		public GStreamerShutdownHook() {
			super("gstreamer-shutdown-hook");
		}

		@Override
		public void run() {
			LOG.debug("GStreamer deinitialization");
			Gst.deinit();
		}
	}

	private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

	public GStreamerDriver() {
		if (INITIALIZED.compareAndSet(false, true)) {
			init();
		}
	}

	private static final void init() {

		if (!Platform.isWindows() && !Platform.isLinux()) {
			throw new WebcamException(String.format("%s has been designed to work only on Windows and Linux platforms", GStreamerDriver.class.getSimpleName()));
		}

		LOG.debug("GStreamer initialization");

		String gpath = null;

		if (Platform.isWindows()) {

			String path = System.getenv("PATH");

			for (String p : path.split(";")) {
				LOG.trace("Search %PATH% for gstreamer bin {}", p);
				if (p.indexOf("GStreamer\\v0.10.") != -1) {
					gpath = p;
					break;
				}
			}

			if (gpath != null) {
				LOG.debug("Add bin directory to JNA search paths {}", gpath);
				NativeLibrary.addSearchPath("gstreamer-0.10", gpath);
			} else {
				throw new WebcamException(String.format("GStreamer has not been installed or not available in PATH: %s", path));
			}
		}

		//@formatter:off
		String[] args = new String[] {
			// "--gst-plugin-path", new File(".").getAbsolutePath(),
			// "--gst-debug-level=3",
		};
		//@formatter:on

		Gst.init(GStreamerDriver.class.getSimpleName(), args);

		Runtime.getRuntime().addShutdownHook(new GStreamerShutdownHook());
	}

	@Override
	public List<WebcamDevice> getDevices() {

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		String srcname = null;
		if (Platform.isWindows()) {
			srcname = "dshowvideosrc";
		} else {
			srcname = "v4l2src";
		}

		Element dshowsrc = ElementFactory.make(srcname, "source");

		try {
			if (Platform.isWindows()) {
				PropertyProbe probe = PropertyProbe.wrap(dshowsrc);
				for (Object name : probe.getValues("device-name")) {
					devices.add(new GStreamerDevice(name.toString()));
				}
			} else if (Platform.isLinux()) {
				VideoDeviceFilenameFilter vfilter = new VideoDeviceFilenameFilter();
				for (File vfile : vfilter.getVideoFiles()) {
					devices.add(new GStreamerDevice(vfile));
				}
			} else {
				throw new RuntimeException("Platform unsupported by GStreamer capture driver");
			}
		} finally {
			if (dshowsrc != null) {
				dshowsrc.dispose();
			}
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
