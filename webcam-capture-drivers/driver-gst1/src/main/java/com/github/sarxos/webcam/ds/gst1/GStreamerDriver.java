package com.github.sarxos.webcam.ds.gst1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.interfaces.PropertyProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.util.NixVideoDevUtils;
import com.sun.jna.Platform;


public class GStreamerDriver implements WebcamDriver {

	private static final Logger LOG = LoggerFactory.getLogger(GStreamerDriver.class);

	private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
	private static final CountDownLatch LATCH = new CountDownLatch(1);

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

	public GStreamerDriver() {
		if (INITIALIZED.compareAndSet(false, true)) {
			init();
			LATCH.countDown();
		} else {
			try {
				LATCH.await();
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private static final void init() {
		String[] args = new String[] {};
		Gst.init(GStreamerDriver.class.getSimpleName(), args);
		Runtime.getRuntime().addShutdownHook(new GStreamerShutdownHook());
	}

	@Override
	public List<WebcamDevice> getDevices() {

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		String srcname = null;
		if (Platform.isWindows()) {
			srcname = "dshowvideosrc";
		} else if (Platform.isLinux()) {
			srcname = "v4l2src";
		} else if (Platform.isMac()) {
			srcname = "qtkitvideosrc";
		}

		Element src = ElementFactory.make(srcname, "source");

		try {
			if (Platform.isWindows()) {
				PropertyProbe probe = PropertyProbe.wrap(src);
				for (Object name : probe.getValues("device-name")) {
					devices.add(new GStreamerDevice(name.toString()));
				}
			} else if (Platform.isLinux()) {
				for (File vfile : NixVideoDevUtils.getVideoFiles()) {
					devices.add(new GStreamerDevice(vfile));
				}
			} else {
				throw new RuntimeException("Platform unsupported by GStreamer capture driver");
			}
		} finally {
			if (src != null) {
				src.dispose();
			}
		}

		return devices;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	public static void main(String[] args) {
		for (WebcamDevice d : new GStreamerDriver().getDevices()) {
			System.out.println(d);
		}
	}
}
