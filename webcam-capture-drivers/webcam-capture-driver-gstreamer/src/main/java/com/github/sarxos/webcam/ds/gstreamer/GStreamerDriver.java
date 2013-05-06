package com.github.sarxos.webcam.ds.gstreamer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.interfaces.PropertyProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamPanel;
import com.sun.jna.NativeLibrary;


public class GStreamerDriver implements WebcamDriver {

	private static final Logger LOG = LoggerFactory.getLogger(GStreamerDriver.class);

	static {
		init();
	}

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

	private static final void init() {
		LOG.debug("GStreamer initialization");
		NativeLibrary.addSearchPath("gstreamer-0.10", "C:\\Program Files\\OSSBuild\\GStreamer\\v0.10.6\\bin");
		Gst.init(GStreamerDriver.class.getSimpleName(), new String[0]);
		Runtime.getRuntime().addShutdownHook(new GStreamerShutdownHook());
	}

	@Override
	public List<WebcamDevice> getDevices() {

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		Element dshowsrc = ElementFactory.make("dshowvideosrc", "source");
		PropertyProbe probe = PropertyProbe.wrap(dshowsrc);

		for (Object name : probe.getValues("device-name")) {
			devices.add(new GStreamerDevice(name.toString()));
		}

		dshowsrc.dispose();

		return devices;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	public static void main(String[] args) {
		// WebcamDriver driver = new GStreamerDriver();
		// for (WebcamDevice device : driver.getDevices()) {
		// System.out.println(device.getName());
		// for (Dimension d : device.getResolutions()) {
		// System.out.println(d);
		// }
		// }

		Webcam.setDriver(new GStreamerDriver());
		JFrame frame = new JFrame();
		frame.add(new WebcamPanel(Webcam.getWebcams().get(1)));
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
