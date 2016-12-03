package com.github.sarxos.webcam.ds.gst1;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.bridj.Platform;
import org.freedesktop.gstreamer.Bin;
import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.Pad;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.State;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.AppSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.gst1.impl.AppSinkNewSampleListener;
import com.github.sarxos.webcam.util.Initializable;
import com.github.sarxos.webcam.util.WebcamInitializer;


public class Gst1Device implements WebcamDevice, Initializable {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Gst1Device.class);

	/**
	 * Limit the lateness of frames to 20ms (50 FPS max)
	 */
	private static final long LATENESS = 20; // ms

	/**
	 * First formats are better. For example video/x-raw-rgb gives 30 FPS on HD720p where
	 * video/x-raw-yuv only 10 FPS on the same resolution. The goal is to use these "better" formats
	 * first, and then fallback to less efficient when not available.
	 */
	private static final String[] BEST_MIME = {
		// "image/jpeg",
		"video/x-raw",
	};

	private static final String MIME_VIDEO_X_RAW = "video/x-raw";
	private static final String MIME_IMAGE_JPEG = "image/jpeg";

	private static final String[] BEST_FORMAT = {
		// "YUY2",
		// "YV12",
		// "I420",
		// "BGR",
		"RGB"
	};

	private final AtomicReference<BufferedImage> ref = new AtomicReference<>();
	private final WebcamInitializer initializer = new WebcamInitializer(this);
	private final String name;

	private Pipeline pipe;
	private AppSink sink;
	private Element source;
	private Element filter;
	private Element convert;
	private Dimension[] resolutions;
	private String format;

	private Dimension resolution;
	private boolean open;
	private boolean disposed;

	public Gst1Device(File vdevice) {
		this(vdevice.getAbsolutePath());
	}

	public Gst1Device(String name) {
		this.name = name;
	}

	@Override
	public void open() {

		if (disposed) {
			throw new WebcamException("Cannot open device because it has been already disposed");
		}
		if (open) {
			return;
		}

		initializer.initialize();

		final String name = getName();
		final Dimension resolution = getResolution();
		final String str = new StringBuilder(MIME_VIDEO_X_RAW)
			.append(",")
			.append("width=").append(resolution.width).append(",")
			.append("height").append(resolution.height)
			.toString();
		final Caps caps = Caps.fromString(str);

		// LOG.debug("Opening device {} with caps {}", name, caps);

		filter.setCaps(caps);

		// pipeLink();
		pipePlay();

		open = true;
	}

	@Override
	public void initialize() {
		pipeLink();
		pipeReady();
	}

	private Dimension[] findResolutions() {

		// final boolean isLinux = Platform.isLinux();

		// if (isLinux) {
		// pipeLink();
		// pipeReady();
		// }

		try {
			return findResolutions0();
		} finally {
			// if (isLinux) {
			// pipeStop();
			// pipeUnlink();
			// }
		}
	}

	private Dimension[] findResolutions0() {

		final Element source = getSource();
		final List<Pad> pads = source.getPads();

		if (pads.isEmpty()) {
			throw new WebcamException("Cannot find supported resolutions because pads list is empty!");
		}

		final Map<String, Dimension> map = new LinkedHashMap<>();

		final Pad pad = pads.get(0);
		final Caps caps = pad.getCaps();
		final String name = getName();
		final String format = getFormat();

		for (int i = 0; i < caps.size(); i++) {

			final Structure structure = caps.getStructure(i);
			final String f = structure.getString("format");

			LOG.trace("Found device {} caps {}", name, structure);

			if (!Objects.equals(f, format)) {
				continue;
			}

			final Dimension resolution = capsStructureToResolution(structure);

			if (resolution != null) {
				map.put(resolution.width + "x" + resolution.height, resolution);
			}
		}

		Dimension[] resolutions = new ArrayList<Dimension>(map.values()).toArray(new Dimension[map.size()]);

		if (LOG.isDebugEnabled()) {
			for (Dimension d : resolutions) {
				LOG.debug("Resolution detected {}", d);
			}
		}

		return resolutions;
	}

	private void pipeLink() {

		// Bin source = Bin.launch("v4l2src device=/dev/video0", true); // ! videoconvert
		final Bin pipe = getPipeline();
		final Element source = getSource();
		final Element convert = getConvert();
		final Element filter = getFilter();
		final Element sink = getSink();

		pipe.addMany(source, convert, filter, sink);
		// pipe.addMany(bin, filter, sink);

		if (!Element.linkMany(source, convert, filter, sink)) {
			// if (!Element.linkMany(bin, filter, sink)) {
			throw new IllegalStateException("Unable to link elements to pipeline bin");
		}
	}

	private void pipeUnlink() {

		final Bin pipe = getPipeline();
		final Element source = getSource();
		final Element convert = getConvert();
		final Element filter = getFilter();
		final Element sink = getSink();

		Element.unlinkMany(source, convert, filter, sink);
		// Element.unlinkMany(bin, filter, sink);

		pipe.removeMany(source, convert, filter, sink);
		// pipe.removeMany(bin, filter, sink);
	}

	private void pipeReady() {
		setPipelineState(State.READY);
	}

	private void pipeStop() {
		setPipelineState(State.NULL);
	}

	private void pipePlay() {
		setPipelineState(State.PLAYING);
	}

	private void setPipelineState(State state) {

		final String name = getName();
		final Pipeline pipe = getPipeline();

		pipe.setState(state);

		LOG.debug("Device {} pipeline has been set to {}", name, state);
	}

	public Pipeline getPipeline() {
		if (pipe == null) {
			pipe = createPipeline();
		}
		return pipe;
	}

	private Pipeline createPipeline() {
		return new Pipeline(getName());
	}

	public Element getConvert() {
		if (convert == null) {
			convert = createConvert();
		}
		return convert;
	}

	private Element createConvert() {
		return ElementFactory.make("videoconvert", getName() + "-videoconvert");
	}

	public Element getFilter() {
		if (filter == null) {
			filter = createFilter();
		}
		return filter;
	}

	private Element createFilter() {
		return ElementFactory.make("capsfilter", getName() + "-filter");
	}

	public AppSink getSink() {
		if (sink == null) {
			sink = createSink();
		}
		return sink;
	}

	private AppSink createSink() {

		final String format = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "BGRx" : "xRGB";
		final String clazz = getClass().getSimpleName();
		final String name = getName();
		final String identifier = "GstVideoSink-" + name;
		final AppSink videosink = new AppSink(identifier);
		final Caps caps = new Caps("video/x-raw,pixel-aspect-ratio=1/1,format=" + format);

		LOG.debug("Creating video sink with caps {}", caps);

		videosink.set("emit-signals", true);
		videosink.connect(new AppSinkNewSampleListener(this));
		videosink.setCaps(caps);
		videosink.setMaximumLateness(LATENESS, TimeUnit.MILLISECONDS);
		videosink.setQOSEnabled(true);

		LOG.debug("Device {} videosing {} has been created", name, videosink);

		return videosink;
	}

	public Element getSource() {
		if (source == null) {
			source = createSource();
		}
		return source;
	}

	private Element createSource() {

		// if (Platform.isWindows()) {
		// source = ElementFactory.make("dshowvideosrc", "source");
		// source.set("device-name", name);
		// } else if (Platform.isLinux()) {

		final Element source = ElementFactory.make("v4l2src", "source");
		source.set("device", name);

		// source.set("device", name);
		// } else {
		// throw new IllegalStateException("Only Linux and Windows is supported");
		// }

		return source;
	}

	public String getFormat() {
		if (format == null) {
			format = findBestFormat();
		}
		return format;
	}

	private String findBestFormat() {

		final Element source = getSource();
		final Pad pad = source.getPads().get(0);
		final Caps caps = pad.getCaps();

		if (LOG.isDebugEnabled()) {
			for (int i = 0; i < caps.size(); i++) {
				LOG.debug("Device {} has caps structure {}", name, caps.getStructure(i));
			}
		}

		for (String f : BEST_FORMAT) {
			for (int i = 0; i < caps.size(); i++) {

				final Structure struct = caps.getStructure(i);
				final String name = struct.getName();
				final String format = struct.getString("format");

				// TODO add support for mjpeg

				switch (name) {
					case MIME_IMAGE_JPEG:
						LOG.debug("Mime {} is not yet supported", name);
						break;
					case MIME_VIDEO_X_RAW:
						if (f.equals(format)) {
							LOG.debug("Best format is {}", format);
							return f;
						}
						break;
				}
			}
		}

		throw new WebcamException("Cannot find best format");
	}

	private Dimension capsStructureToResolution(Structure structure) {

		LOG.debug("Device {}, read resolution from caps strcuture {}", getName(), structure);

		int w = -1;
		int h = -1;

		if (Platform.isWindows()) {
			w = structure.getRange("width").getMinInt();
			h = structure.getRange("height").getMinInt();
		} else if (Platform.isLinux()) {
			w = structure.getInteger("width");
			h = structure.getInteger("height");
		}

		if (w > 0 && h > 0) {
			return new Dimension(w, h);
		} else {
			return null;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Dimension[] getResolutions() {
		if (resolutions == null) {
			resolutions = findResolutions();
		}
		return resolutions;
	}

	@Override
	public Dimension getResolution() {

		if (resolution == null) {

			final Dimension[] resolutions = getResolutions();
			if (resolutions.length == 0) {
				throw new WebcamException("Supported resolutions has not been detected");
			}

			resolution = resolutions[0];
		}

		return resolution;
	}

	@Override
	public void setResolution(Dimension resolution) {
		this.resolution = resolution;
	}

	@Override
	public BufferedImage getImage() {

		LOG.debug("Device {} get image", getName());

		BufferedImage bi = ref.get();

		while (bi == null) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new WebcamException("Thread has been interrupted", e);
			}

			bi = ref.get();
		}

		return bi;
	}

	@Override
	public void close() {

		if (!open) {
			return;
		}

		pipeStop();
		pipeUnlink();

		open = false;
	}

	@Override
	public void dispose() {

		close();

		if (source != null) {
			source.dispose();
		}
		if (filter != null) {
			filter.dispose();
		}
		if (sink != null) {
			sink.dispose();
		}
		if (pipe != null) {
			pipe.dispose();
		}

		disposed = true;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + getName();
	}

	public AtomicReference<BufferedImage> getRef() {
		return ref;
	}
}
