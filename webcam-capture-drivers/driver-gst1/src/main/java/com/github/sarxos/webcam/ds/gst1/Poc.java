package com.github.sarxos.webcam.ds.gst1;

import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pad;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.elements.AppSink;


public class Poc {

	static {

		String[] args = new String[] {};
		Gst.init(Gst1Driver.class.getSimpleName(), args);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			@Override
			public void run() {
				Gst.deinit();
			}
		}));
	}

	static class AppSinkNewSampleListener implements AppSink.NEW_SAMPLE {

		@Override
		public FlowReturn newSample(AppSink elem) {
			System.out.println("New sample");
			return FlowReturn.OK;
		}
	}

	public static void main(String[] args) throws InterruptedException {

		System.out.println("Create source");

		// Bin source = Bin.launch("autovideosrc ! videoconvert", true);

		// Bin source = Bin.launch("v4l2src device=/dev/video0 ! videoconvert", true);
		// autovideosrc ! videoconvert

		Element source = ElementFactory.make("v4l2src", "source");
		source.set("device", "/dev/video0");

		Element videoconvert = ElementFactory.make("videoconvert", "VideoConverter");

		System.out.println("Create sink");
		AppSink sink = new AppSink("ApplicationSinkElement");
		System.out.println("Sink set emit signals to true");
		sink.set("emit-signals", true);
		System.out.println("Sink set new sample listener");
		sink.connect(new AppSinkNewSampleListener());
		System.out.println("Sink set caps");
		sink.setCaps(new Caps("video/x-raw,pixel-aspect-ratio=1/1,format=BGRx"));

		System.out.println("Create filter");
		Element filter = ElementFactory.make("capsfilter", "filter");
		filter.setCaps(Caps.fromString("video/x-raw,width=640,height=480"));

		System.out.println("Create pipeline");
		Pipeline pipeline = new Pipeline();
		pipeline.addMany(source, videoconvert, filter, sink);
		System.out.println("Link elements");
		Element.linkMany(source, videoconvert, filter, sink);

		System.out.println("Ready");
		pipeline.ready();

		Pad pad = source.getPads().get(0);
		Caps caps = pad.getCaps();

		System.out.println("The following structures are allowed:");
		for (int i = 0; i < caps.size(); i++) {
			System.out.println(caps.getStructure(i));
		}

		System.out.println("Play and wait for new samples");

		pipeline.play();

		Thread.sleep(5000);

		pipeline.stop();
	}
}
