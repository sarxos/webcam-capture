package com.github.sarxos.webcam.ds.gst1;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

import org.bridj.Platform;
import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.AppSink;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.util.Initializable;
import com.github.sarxos.webcam.util.WebcamInitializer;


public class GStreamerDevice implements WebcamDevice, Initializable {

	private class AppSinkListener implements AppSink.NEW_SAMPLE {

		public void rgbFrame(boolean isPrerollFrame, int width, int height, IntBuffer rgb) {
			final BufferedImage renderImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			int[] pixels = ((DataBufferInt) renderImage.getRaster().getDataBuffer()).getData();
			rgb.get(pixels, 0, width * height);
		}

		@Override
		public void newBuffer(AppSink elem) {
			Sample sample = elem.pullSample();
			Structure capsStruct = sample.getCaps().getStructure(0);
			int w = capsStruct.getInteger("width");
			int h = capsStruct.getInteger("height");
			Buffer buffer = sample.getBuffer();
			ByteBuffer bb = buffer.map(false);
			if (bb != null) {
				rgbFrame(false, w, h, bb.asIntBuffer());
				buffer.unmap();
			}
			sample.dispose();
		}
	}

	/**
	 * Limit the lateness of frames to no more than 20ms (50 FPS max)
	 */
	private static final long LATENESS = 20; // ms

	private final WebcamInitializer initializer = new WebcamInitializer(this);

	private AppSink videosink;
	private final String name;

	public GStreamerDevice(File vdevice) {
		this(vdevice.getAbsolutePath());
	}

	public GStreamerDevice(String name) {
		this.name = name;
	}

	@Override
	public void open() {
		initializer.initialize();
	}

	@Override
	public void initialize() {
		videosink = prepareVideoSink();

		// resolutions = parseResolutions(source.getPads().get(0));

	}

	private AppSink prepareVideoSink() {

		String format = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "BGRx" : "xRGB";

		AppSink videosink = new AppSink("GstVideoSink-" + getName());
		videosink.set("emit-signals", true);
		videosink.connect(new AppSinkListener());
		videosink.setCaps(new Caps("video/x-raw,format=" + format));
		videosink.setMaximumLateness(LATENESS, TimeUnit.MILLISECONDS);
		videosink.setQOSEnabled(true);

		return videosink;
	}

	private Element prepareVideoSource() {
		Element source = null;

		if (Platform.isWindows()) {
			source = ElementFactory.make("dshowvideosrc", "source");
			source.set("device-name", name);
		} else if (Platform.isLinux()) {
			source = ElementFactory.make("v4l2src", "source");
			source.set("device", new File(name));
		} else {
			throw new IllegalStateException("Only Linux and Windows is supported");
		}

		return source;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension[] getResolutions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension getResolution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setResolution(Dimension size) {
		// TODO Auto-generated method stub

	}

	@Override
	public BufferedImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}
}
