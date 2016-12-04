package com.github.sarxos.webcam.ds.gst1;

import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.interfaces.PropertyProbe;


public class ListUvcDevices {

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

	public static void main(String[] args) throws InterruptedException {
		Element source = ElementFactory.make("v4l2src", "source");
		PropertyProbe probe = PropertyProbe.wrap(source);
		for (Object name : probe.getValues("device")) {
			System.out.println("probe: " + name.toString());
		}
	}
}
