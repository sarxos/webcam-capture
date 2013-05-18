package com.github.sarxos.webcam.ds.v4l4j;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.DeviceInfo;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.ImageFormat;
import au.edu.jcu.v4l4j.ImageFormatList;
import au.edu.jcu.v4l4j.ResolutionInfo;
import au.edu.jcu.v4l4j.ResolutionInfo.DiscreteResolution;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.StateException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;


public class V4l4jDevice implements WebcamDevice, CaptureCallback, WebcamDevice.FPSSource {

	private static final Logger LOG = LoggerFactory.getLogger(V4l4jDevice.class);

	private final File vfile;

	private VideoDevice vd = null;
	private DeviceInfo di = null;
	private ImageFormatList ifl = null;
	private FrameGrabber grabber = null;

	private List<ImageFormat> formats = null;
	private List<Dimension> resolutions = new ArrayList<Dimension>();
	private Dimension resolution = null;

	private AtomicBoolean open = new AtomicBoolean(false);
	private AtomicBoolean disposed = new AtomicBoolean(false);
	private CountDownLatch latch = new CountDownLatch(1);

	private volatile BufferedImage image = null;
	private volatile V4L4JException exception = null;

	/* used to calculate fps */

	private long t1 = -1;
	private long t2 = -1;

	private volatile double fps = 0;

	public V4l4jDevice(File vfile) {

		this.vfile = vfile;

		LOG.debug("Creating V4L4J devuce");

		try {
			vd = new VideoDevice(vfile.getAbsolutePath());
		} catch (V4L4JException e) {
			throw new WebcamException(String.format("Cannot instantiate V4L4J device from %s", vfile), e);
		}

		try {
			di = vd.getDeviceInfo();
		} catch (V4L4JException e) {
			throw new WebcamException(String.format("Cannot get V4L4J device info from %s", vfile), e);
		}

		ifl = di.getFormatList();
		formats = ifl.getYUVEncodableFormats();

		for (ImageFormat format : formats) {

			String name = format.getName();
			LOG.debug("Found format {}", name);

			if (name.startsWith("YU")) {

				ResolutionInfo ri = format.getResolutionInfo();
				LOG.debug("Resolution info {} {}", name, ri);

				for (DiscreteResolution dr : ri.getDiscreteResolutions()) {
					resolutions.add(new Dimension(dr.getWidth(), dr.getHeight()));
				}
			}
		}
	}

	@Override
	public String getName() {
		return vfile.getAbsolutePath();
	}

	@Override
	public Dimension[] getResolutions() {
		return resolutions.toArray(new Dimension[resolutions.size()]);
	}

	@Override
	public Dimension getResolution() {
		if (resolution == null) {
			if (resolutions.isEmpty()) {
				throw new WebcamException("No valid resolution detected for " + vfile);
			}
			resolution = resolutions.get(0);
		}
		return resolution;
	}

	@Override
	public void setResolution(Dimension size) {
		resolution = size;
	}

	@Override
	public BufferedImage getImage() {

		if (!open.get()) {
			throw new RuntimeException("Cannot get image from closed device");
		}

		V4L4JException ex = null;
		if (exception != null) {
			throw new WebcamException(ex);
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			LOG.trace("Await has been interrupted", e);
			return null;
		}

		return image;
	}

	@Override
	public synchronized void open() {

		if (disposed.get()) {
			throw new WebcamException("Cannot open device because it has been already disposed");
		}

		if (!open.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("Opening V4L4J device {}", vfile);

		Dimension d = getResolution();

		LOG.debug("Constructing V4L4J frame grabber");

		try {
			grabber = vd.getJPEGFrameGrabber(d.width, d.height, 0, 0, 80);
		} catch (V4L4JException e) {
			throw new WebcamException(e);
		}

		grabber.setCaptureCallback(this);

		int w1 = d.width;
		int h1 = d.height;
		int w2 = grabber.getWidth();
		int h2 = grabber.getHeight();

		if (w1 != w2 || h1 != h2) {
			LOG.error(String.format("Resolution mismatch %dx%d vs %dx%d, setting new one", w1, h1, w2, h2));
			resolution = new Dimension(w2, h2);
		}

		LOG.debug("Starting V4L4J frame grabber");

		try {
			grabber.startCapture();
		} catch (V4L4JException e) {
			throw new WebcamException(e);
		}

		LOG.debug("Webcam V4L4J is now open");
	}

	@Override
	public synchronized void close() {

		if (!open.compareAndSet(true, false)) {
			return;
		}

		LOG.debug("Closing V4L4J device {}", vfile);

		try {
			grabber.stopCapture();
		} catch (StateException e) {
			LOG.trace("State exception on close", e); // ignore
		} finally {
			image = null;
			latch.countDown();
		}

		grabber = null;
		vd.releaseFrameGrabber();

		LOG.debug("V4L4J device {} has been closed", vfile);

	}

	@Override
	public void dispose() {

		if (!disposed.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("Disposing V4L4J device {}", vfile);

		if (open.get()) {
			close();
		}

		vd.releaseControlList();
		vd.release();

		LOG.debug("V4L4J device {} has been disposed", vfile);
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}

	@Override
	public void nextFrame(VideoFrame frame) {

		if (!open.get()) {
			return;
		}

		if (t1 == -1 || t2 == -1) {
			t1 = System.currentTimeMillis();
			t2 = System.currentTimeMillis();
		}

		try {
			image = frame.getBufferedImage();
		} finally {
			try {
				frame.recycle();
			} finally {
				latch.countDown();
			}
		}

		t1 = t2;
		t2 = System.currentTimeMillis();

		fps = (4 * fps + 1000 / (t2 - t1 + 1)) / 5;
	}

	@Override
	public void exceptionReceived(V4L4JException e) {
		e.printStackTrace();
		LOG.error("Exception received from V4L4J", e);
		exception = e;
	}

	@Override
	public double getFPS() {
		return fps;
	}
}
