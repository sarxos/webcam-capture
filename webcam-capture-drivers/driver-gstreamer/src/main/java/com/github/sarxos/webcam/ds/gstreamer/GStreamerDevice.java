package com.github.sarxos.webcam.ds.gstreamer;

import static com.github.sarxos.webcam.ds.gstreamer.GStreamerDriver.FORMAT_MJPEG;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bridj.Platform;
import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.gstreamer.Structure;
import org.gstreamer.elements.RGBDataSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamResolution;


public class GStreamerDevice implements WebcamDevice, RGBDataSink.Listener, WebcamDevice.FPSSource {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(GStreamerDevice.class);

	/**
	 * Limit the lateness of frames to no more than 20ms (half a frame at 25fps)
	 */
	private static final long LATENESS = 20; // ms

	/**
	 * Video format to capture.
	 */
	private String format;

	/**
	 * All possible resolutions - populated while initialization phase.
	 */
	private Dimension[] resolutions = null;

	/**
	 * Device name, immutable. Used only on Windows platform.
	 */
	private final String name;
	/**
	 * Device name, immutable. Used only on Linux platform.
	 */
	private final File vfile;

	private final GStreamerDriver driver;

	/* gstreamer stuff */

	private Pipeline pipe = null;
	private Element source = null;
	private Element filter = null;
	private Element jpegpar = null;
	private Element jpegdec = null;
	private Element[] elements = null;
	private RGBDataSink sink = null;

	private Caps caps = null;

	/* logic */

	private AtomicBoolean open = new AtomicBoolean(false);
	private AtomicBoolean disposed = new AtomicBoolean(false);
	private AtomicBoolean starting = new AtomicBoolean(false);
	private AtomicBoolean initialized = new AtomicBoolean(false);
	private Dimension resolution = WebcamResolution.VGA.getSize();
	private BufferedImage image = null;

	/* used to calculate fps */

	private long t1 = -1;
	private long t2 = -1;

	private volatile double fps = 0;

	/**
	 * Create GStreamer webcam device.
	 *
	 * @param name the name of webcam device
	 */
	protected GStreamerDevice(GStreamerDriver driver, String name) {
		this.driver = driver;
		this.name = name;
		this.vfile = null;
	}

	protected GStreamerDevice(GStreamerDriver driver, File vfile) {
		this.driver = driver;
		this.name = null;
		this.vfile = vfile;
	}

	/**
	 * Initialize webcam device.
	 */
	private synchronized void init() {

		if (!initialized.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("GStreamer webcam device initialization");

		pipe = new Pipeline(name);

		if (Platform.isWindows()) {
			source = ElementFactory.make("dshowvideosrc", "dshowvideosrc");
			source.set("device-name", name);
		} else if (Platform.isLinux()) {
			source = ElementFactory.make("v4l2src", "v4l2src");
			source.set("device", vfile.getAbsolutePath());
		}

		sink = new RGBDataSink(name, this);
		sink.setPassDirectBuffer(true);
		sink.getSinkElement().setMaximumLateness(LATENESS, TimeUnit.MILLISECONDS);
		sink.getSinkElement().setQOSEnabled(true);

		filter = ElementFactory.make("capsfilter", "capsfilter");

		jpegpar = ElementFactory.make("jpegparse", "jpegparse");
		jpegdec = ElementFactory.make("jpegdec", "jpegdec");

		// if (Platform.isLinux()) {
		pipelineReady();
		// }

		resolutions = parseResolutions(source.getPads().get(0));

		// if (Platform.isLinux()) {
		pipelineStop();
		// }
	}

	/**
	 * Use GStreamer to get all possible resolutions.
	 *
	 * @param pad the pad to get resolutions from
	 * @return Array of resolutions supported by device connected with pad
	 */
	private Dimension[] parseResolutions(Pad pad) {

		Caps caps = pad.getCaps();

		format = findPreferredFormat(caps);

		LOG.debug("Best format is {}", format);

		Dimension r = null;
		Structure s = null;
		String mime = null;

		final int n = caps.size();
		int i = 0;

		Map<String, Dimension> map = new HashMap<String, Dimension>();

		do {

			s = caps.getStructure(i++);

			LOG.debug("Found format structure {}", s);

			mime = s.getName();

			if (mime.equals(format)) {
				if ((r = capStructToResolution(s)) != null) {
					map.put(r.width + "x" + r.height, r);
				}
			}

		} while (i < n);

		final Dimension[] resolutions = new ArrayList<Dimension>(map.values()).toArray(new Dimension[0]);

		if (LOG.isDebugEnabled()) {
			for (Dimension d : resolutions) {
				LOG.debug("Resolution detected {} with format {}", d, format);
			}
		}

		return resolutions;
	}

	private String findPreferredFormat(Caps caps) {
		for (String f : driver.getPreferredFormats()) {
			for (int i = 0, n = caps.size(); i < n; i++) {
				if (f.equals(caps.getStructure(i).getName())) {
					return f;
				}
			}
		}
		return null;
	}

	private static Dimension capStructToResolution(Structure structure) {

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
		if (Platform.isWindows()) {
			return name;
		} else if (Platform.isLinux()) {
			return vfile.getAbsolutePath();
		} else {
			throw new RuntimeException("Platform not supported by GStreamer capture driver");
		}
	}

	@Override
	public Dimension[] getResolutions() {
		init();
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

		LOG.debug("Opening GStreamer device");

		init();

		starting.set(true);

		Dimension size = getResolution();

		image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		image.setAccelerationPriority(0);
		image.flush();

		if (caps != null) {
			caps.dispose();
		}

		caps = Caps.fromString(String.format("%s,framerate=30/1,width=%d,height=%d", format, size.width, size.height));
		filter.setCaps(caps);

		LOG.debug("Using filter caps: {}", caps);

		pipelinePlay();

		LOG.debug("Wait for device to be ready");

		// wait max 20s for image to appear
		synchronized (this) {
			try {
				this.wait(20000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private void pipelineElementsReset() {
		elements = null;
	}

	private Element[] pipelineElementsPrepare() {
		if (elements == null) {
			if (FORMAT_MJPEG.equals(format)) {
				elements = new Element[] { source, filter, jpegpar, jpegdec, sink };
			} else {
				elements = new Element[] { source, filter, sink };
			}
		}
		return elements;
	}

	private void pipelineElementsLink() {
		final Element[] elements = pipelineElementsPrepare();
		pipe.addMany(elements);
		if (!Element.linkMany(elements)) {
			LOG.warn("Some elements were not successfully linked!");
		}
	}

	private void pipelineElementsUnlink() {
		final Element[] elements = pipelineElementsPrepare();
		Element.unlinkMany(elements);
		pipe.removeMany(elements);
	}

	private void pipelineReady() {
		pipelineElementsLink();
		pipe.setState(State.READY);
	}

	private void pipelinePlay() {
		pipelineElementsReset();
		pipelineElementsLink();
		pipe.setState(State.PLAYING);
	}

	private void pipelineStop() {
		pipe.setState(State.NULL);
		pipelineElementsUnlink();
	}

	@Override
	public void close() {

		if (!open.compareAndSet(true, false)) {
			return;
		}

		LOG.debug("Closing GStreamer device");

		pipelineStop();

		image = null;
	}

	@Override
	public void dispose() {

		if (!disposed.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("Disposing GStreamer device");

		close();

		source.dispose();
		filter.dispose();
		jpegpar.dispose();
		jpegdec.dispose();
		caps.dispose();
		sink.dispose();
		pipe.dispose();
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}

	@Override
	public void rgbFrame(boolean preroll, int width, int height, IntBuffer rgb) {

		LOG.trace("New RGB frame");

		if (t1 == -1 || t2 == -1) {
			t1 = System.currentTimeMillis();
			t2 = System.currentTimeMillis();
		}

		BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		tmp.setAccelerationPriority(0);
		rgb.get(((DataBufferInt) tmp.getRaster().getDataBuffer()).getData(), 0, width * height);
		tmp.flush();

		image = tmp;

		if (starting.compareAndSet(true, false)) {

			synchronized (this) {
				this.notifyAll();
			}

			LOG.debug("GStreamer device ready");
		}

		t1 = t2;
		t2 = System.currentTimeMillis();

		fps = (4 * fps + 1000 / (t2 - t1 + 1)) / 5;
	}

	@Override
	public double getFPS() {
		return fps;
	}

	public Pipeline getPipe() {
		return pipe;
	}

	public Element getSource() {
		return source;
	}

	public Element getFilter() {
		return filter;
	}

	public RGBDataSink getSink() {
		return sink;
	}

	public Caps getCaps() {
		return caps;
	}
}
