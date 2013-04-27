package com.github.sarxos.webcam.ds.gstreamer;

import java.util.List;

import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.interfaces.PropertyProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.sun.jna.NativeLibrary;


public class GStreamerDriver implements WebcamDriver {

	private static final Logger LOG = LoggerFactory.getLogger(GStreamerDriver.class);

	private static final class GStreamerShutdownHook extends Thread {

		public GStreamerShutdownHook() {
			super("gstreamer-shutdown-hook");
		}

		@Override
		public void run() {
			Gst.deinit();
		}
	}

	static {
		NativeLibrary.addSearchPath("gstreamer-0.10", "C:\\Program Files\\OSSBuild\\GStreamer\\v0.10.6\\bin");
		Gst.init(GStreamerDriver.class.getSimpleName(), new String[0]);
		Runtime.getRuntime().addShutdownHook(new GStreamerShutdownHook());
	}

	private static final Pipeline pipe = new Pipeline(GStreamerDriver.class.getSimpleName());

	@Override
	public List<WebcamDevice> getDevices() {

		Element dshowsrc = ElementFactory.make("dshowvideosrc", "source");
		dshowsrc.setState(State.READY);

		PropertyProbe probe = PropertyProbe.wrap(dshowsrc);
		for (Object device : probe.getValues("device-name")) {
			System.out.println(device);
		}

		dshowsrc.setState(State.NULL);

		// final Element videosrc = ElementFactory.make("videotestsrc",
		// "source");
		// final Element videofilter = ElementFactory.make("capsfilter",
		// "filter");
		// videofilter.setCaps(Caps.fromString("video/x-raw-yuv, width=720, height=576"
		// + ", bpp=32, depth=32, framerate=25/1"));

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	public static void main(String[] args) {
		new GStreamerDriver().getDevices();
	}
}
