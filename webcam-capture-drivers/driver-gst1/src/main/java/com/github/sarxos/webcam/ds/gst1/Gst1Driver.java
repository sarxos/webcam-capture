package com.github.sarxos.webcam.ds.gst1;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.interfaces.PropertyProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.gst1.impl.GsUtils;
import com.github.sarxos.webcam.util.NixVideoDevUtils;
import com.sun.jna.Platform;


public class Gst1Driver implements WebcamDriver {

	private static final Logger LOG = LoggerFactory.getLogger(Gst1Driver.class);

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

	public Gst1Driver() {
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
		Gst.init(Gst1Driver.class.getSimpleName(), args);
		Runtime.getRuntime().addShutdownHook(new GStreamerShutdownHook());
	}

	@Override
	public List<WebcamDevice> getDevices() {

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		final String factory = GsUtils.getCompatibleSourceFactory();
		final Element source = ElementFactory.make(factory, "source");

		try {
			if (Platform.isWindows()) {
				PropertyProbe probe = PropertyProbe.wrap(source);
				for (Object name : probe.getValues("device-name")) {
					devices.add(new Gst1Device(name.toString()));
				}
			} else if (Platform.isLinux()) {
				for (File vfile : NixVideoDevUtils.getVideoFiles()) {
					devices.add(new Gst1Device(vfile));
				}
			} else {
				throw new RuntimeException("Platform unsupported by GStreamer capture driver");
			}
		} finally {
			if (source != null) {
				source.dispose();
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
		return "Driver " + getClass().getName();
	}

	public static void main(String[] args) throws IOException {
		for (WebcamDevice d : new Gst1Driver().getDevices()) {
			System.out.println(d);
			d.getResolutions();
			d.open();
			ImageIO.write(d.getImage(), "JPG", new File("a.jpg"));
			d.close();
		}

	}
}
