package com.github.sarxos.webcam.ds.gstreamer;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.Structure;
import org.gstreamer.elements.RGBDataSink;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamResolution;


public class GStreamerDevice implements WebcamDevice, RGBDataSink.Listener {

	/**
	 * Limit the lateness of frames to no more than 20ms (half a frame at 25fps)
	 */
	private static final long LATENESS = 20; // ms

	private static final String FORMAT = "video/x-raw-yuv";

	private final String name;
	private final Dimension[] resolutions;

	private Pipeline pipe = null;
	private Element source = null;
	private Element filter = null;
	private RGBDataSink sink = null;
	private BufferedImage image = null;
	private Caps caps = null;

	private AtomicBoolean open = new AtomicBoolean(false);
	private AtomicBoolean disposed = new AtomicBoolean(false);
	private Dimension resolution = WebcamResolution.VGA.getSize();

	protected GStreamerDevice(String name) {

		this.name = name;

		pipe = new Pipeline(name);

		sink = new RGBDataSink(name, this);
		sink.setPassDirectBuffer(true);
		sink.getSinkElement().setMaximumLateness(LATENESS, TimeUnit.MILLISECONDS);
		sink.getSinkElement().setQOSEnabled(true);

		source = ElementFactory.make("dshowvideosrc", "source");
		source.set("device-name", name);

		filter = ElementFactory.make("capsfilter", "filter");

		resolutions = parseResolutions(source.getPads().get(0));
	}

	private static final Dimension[] parseResolutions(Pad pad) {

		List<Dimension> dimensions = new ArrayList<Dimension>();

		Caps caps = pad.getCaps();

		Structure structure = null;
		String format = null;

		int n = caps.size();
		int i = 0;

		int w = -1;
		int h = -1;

		do {

			structure = caps.getStructure(i++);
			format = structure.getName();

			if (format.equals(FORMAT)) {
				w = structure.getRange("width").getMinInt();
				h = structure.getRange("height").getMinInt();
				dimensions.add(new Dimension(w, h));
			}

		} while (i < n);

		return dimensions.toArray(new Dimension[dimensions.size()]);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Dimension[] getResolutions() {
		return resolutions;
	}

	@Override
	public Dimension getResolution() {
		return resolution;
	}

	@Override
	public void setResolution(Dimension size) {
		this.resolution = size;
	}

	@Override
	public BufferedImage getImage() {
		return image;
	}

	@Override
	public void open() {

		if (!open.compareAndSet(false, true)) {
			return;
		}

		Dimension size = getResolution();

		image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		image.setAccelerationPriority(0);
		image.flush();

		if (caps != null) {
			caps.dispose();
		} else {
			caps = Caps.fromString(String.format("%s,width=%d,height=%d", FORMAT, size.width, size.height));
		}

		filter.setCaps(caps);

		pipe.addMany(source, filter, sink);
		Element.linkMany(source, filter, sink);
		pipe.setState(State.PLAYING);
	}

	@Override
	public void close() {

		if (!open.compareAndSet(true, false)) {
			return;
		}

		pipe.setState(State.NULL);
		Element.unlinkMany(source, filter, sink);
		pipe.removeMany(source, filter, sink);
	}

	@Override
	public void dispose() {

		if (!disposed.compareAndSet(false, true)) {
			return;
		}

		close();

		filter.dispose();
		source.dispose();
		sink.dispose();
		pipe.dispose();
		caps.dispose();
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}

	@Override
	public void rgbFrame(boolean preroll, int width, int height, IntBuffer rgb) {

		BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		tmp.setAccelerationPriority(0);
		tmp.flush();

		rgb.get(((DataBufferInt) tmp.getRaster().getDataBuffer()).getData(), 0, width * height);

		image = tmp;
	}
}
