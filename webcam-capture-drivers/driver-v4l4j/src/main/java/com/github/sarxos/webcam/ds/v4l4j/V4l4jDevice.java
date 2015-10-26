package com.github.sarxos.webcam.ds.v4l4j;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.DeviceInfo;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.ImageFormat;
import au.edu.jcu.v4l4j.ImageFormatList;
import au.edu.jcu.v4l4j.ResolutionInfo;
import au.edu.jcu.v4l4j.ResolutionInfo.DiscreteResolution;
import au.edu.jcu.v4l4j.ResolutionInfo.StepwiseResolution;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.StateException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;


public class V4l4jDevice implements WebcamDevice, CaptureCallback, WebcamDevice.FPSSource {

	private static final Logger LOG = LoggerFactory.getLogger(V4l4jDevice.class);

	private static final String[] BEST_FORMATS = new String[] {

		// MJPEG and JPEG are the best match because there is no need to
		// recalculate too much, hardware will deliver what we need

		"MJPEG", "JPEG",

		// next are YUV formats where every 2 pixels can be written in 4 bytes

		"YU", "UY", "YV", "UV",

		// 24-bit formats where every pixel can be stored in 3 bytes

		"BGR24", "RGB24",

		// 32-bit formats where every pixel can be stored in 4 bytes

		"BGR32", "RGB32"
	};

	private final File videoFile;
	private final VideoDevice videoDevice;
	// private final DeviceInfo videoDeviceInfo;
	private final ImageFormat videoBestImageFormat;

	private FrameGrabber grabber = null;
	private final List<Dimension> videoResolutions;

	// private final List<ImageFormat> formats;
	private Dimension resolution = null;

	private AtomicBoolean open = new AtomicBoolean(false);
	private AtomicBoolean disposed = new AtomicBoolean(false);

	private final Exchanger<BufferedImage> exchanger = new Exchanger<BufferedImage>();
	private volatile V4L4JException exception = null;

	/* used to calculate fps */

	private long t1 = -1;
	private long t2 = -1;

	private volatile double fps = 0;

	public V4l4jDevice(File file) {

		if (file == null) {
			throw new IllegalArgumentException("Video file cannot be null!");
		}

		videoFile = file;
		videoDevice = getVideoDevice(file);
		videoBestImageFormat = getVideoBestImageFormat(videoDevice);
		videoResolutions = getVideoResolutions(videoBestImageFormat);
	}

	/**
	 * Create video device from file.
	 *
	 * @param file the video descriptor file
	 * @return The {@link VideoDevice}
	 */
	private static VideoDevice getVideoDevice(File file) {

		LOG.debug("Creating V4L4J device from file {}", file);

		try {
			return new VideoDevice(file.getAbsolutePath());
		} catch (V4L4JException e) {
			throw new WebcamException("Cannot instantiate V4L4J device from " + file, e);
		}
	}

	private static DeviceInfo getVideoDeviceInfo(VideoDevice device) {

		LOG.trace("Get video device info");

		LOG.trace("Support BGR conversion {}", device.supportBGRConversion());
		LOG.trace("Support JPG conversion {}", device.supportJPEGConversion());
		LOG.trace("Support RGB conversion {}", device.supportRGBConversion());
		LOG.trace("Support YUV conversion {}", device.supportYUVConversion());
		LOG.trace("Support YVU conversion {}", device.supportYVUConversion());

		DeviceInfo info = null;
		try {
			info = device.getDeviceInfo();
		} catch (V4L4JException e) {
			throw new WebcamException("Cannot get V4L4J device info from " + device, e);
		}

		if (info == null) {
			throw new WebcamException("Cannot get device info from device");
		}

		return info;
	}

	private static ImageFormat getVideoBestImageFormat(VideoDevice device) {

		if (device == null) {
			throw new IllegalArgumentException("Device must not be null!");
		}

		ImageFormatList formatsList = getVideoDeviceInfo(device).getFormatList();
		List<ImageFormat> formats = formatsList.getJPEGEncodableFormats();

		int min = Integer.MAX_VALUE;
		ImageFormat bestFormat = null;

		for (ImageFormat format : formats) {

			ResolutionInfo info = format.getResolutionInfo();
			ResolutionInfo.Type type = info.getType();
			String name = format.getName();

			// skip unsupported resolution type

			switch (type) {
				case UNSUPPORTED:
				case DISCRETE:
				case STEPWISE:
					break;
				default:
					throw new WebcamException("Unknown resolution type " + type);
			}

			LOG.trace("Testing {} ({})", name, type);

			for (int i = 0; i < BEST_FORMATS.length; i++) {
				if (name.startsWith(BEST_FORMATS[i]) && i < min) {
					min = i;
					bestFormat = format;
				}
			}
		}

		LOG.debug("Best image format match {}", bestFormat);

		if (bestFormat == null) {
			throw new WebcamException("No suitable image format detected");
		}

		return bestFormat;
	}

	private static List<Dimension> getResolutionsDiscrete(ResolutionInfo info) {
		List<Dimension> resolutions = new ArrayList<Dimension>();
		for (DiscreteResolution resolution : info.getDiscreteResolutions()) {
			resolutions.add(new Dimension(resolution.getWidth(), resolution.getHeight()));
		}
		return resolutions;
	}

	private static List<Dimension> getResolutionsStepwise(ResolutionInfo info) {

		List<Dimension> resolutions = new ArrayList<Dimension>();
		StepwiseResolution resolution = info.getStepwiseResolution();

		int minW = resolution.getMinWidth();
		int minH = resolution.getMinHeight();
		int maxW = resolution.getMaxWidth();
		int maxH = resolution.getMaxHeight();
		int stepW = resolution.getWidthStep();
		int stepH = resolution.getHeightStep();

		for (WebcamResolution r : WebcamResolution.values()) {

			Dimension size = r.getSize();

			int w = size.width;
			int h = size.height;

			boolean wok = w <= maxW && w >= minW;
			boolean hok = h <= maxH && h >= minH;
			boolean sok = w % stepW == 0 && h % stepH == 0;

			if (wok && hok && sok) {
				resolutions.add(size);
			}
		}

		return resolutions;
	}

	private static List<Dimension> getResolutionsUnsupported(ResolutionInfo info) {
		List<Dimension> resolutions = new ArrayList<Dimension>();
		resolutions.add(WebcamResolution.QQVGA.getSize());
		resolutions.add(WebcamResolution.QVGA.getSize());
		resolutions.add(WebcamResolution.VGA.getSize());
		return resolutions;
	}

	/**
	 * Get video resolutions from {@link ImageFormat}.
	 *
	 * @param format the {@link ImageFormat} to test
	 * @return List of resolutions supported by given format
	 */
	private static List<Dimension> getVideoResolutions(ImageFormat format) {

		if (format == null) {
			throw new IllegalArgumentException("Image format cannot be null!");
		}

		ResolutionInfo info = format.getResolutionInfo();
		ResolutionInfo.Type type = info.getType();

		switch (type) {
			case DISCRETE:
				return getResolutionsDiscrete(info);
			case STEPWISE:
				return getResolutionsStepwise(info);
			case UNSUPPORTED:
				return getResolutionsUnsupported(info);
			default:
				throw new WebcamException("Unknown resolution type " + type);
		}
	}

	@Override
	public String getName() {
		return videoFile.getAbsolutePath();
	}

	@Override
	public Dimension[] getResolutions() {
		return videoResolutions.toArray(new Dimension[videoResolutions.size()]);
	}

	@Override
	public Dimension getResolution() {
		if (resolution == null) {
			if (videoResolutions.isEmpty()) {
				throw new WebcamException("No valid resolution detected for " + videoFile);
			}
			resolution = videoResolutions.get(0);
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

		V4L4JException ex = this.exception;
		if (ex != null) {
			throw new WebcamException(ex);
		}

		int timeout = 3;
		try {
			return exchanger.exchange(null, timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return null;
		} catch (TimeoutException e) {
			LOG.error("Unable to get image in {} seconds timeout", timeout);
			return null;
		}
	}

	@Override
	public synchronized void open() {

		if (disposed.get()) {
			throw new WebcamException("Cannot open device because it has been already disposed");
		}

		if (!open.compareAndSet(false, true)) {
			return;
		}

		if (!videoDevice.supportJPEGConversion()) {
			throw new WebcamException("Video device does not support JPEG conversion");
		}

		LOG.debug("Opening V4L4J device {}", videoFile);

		Dimension d = getResolution();

		LOG.debug("Constructing V4L4J frame grabber");

		try {
			grabber = videoDevice.getJPEGFrameGrabber(d.width, d.height, 0, 0, 80, videoBestImageFormat);
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

		LOG.debug("Closing V4L4J device {}", videoFile);

		try {
			grabber.stopCapture();
		} catch (StateException e) {
			LOG.trace("State exception on close", e); // ignore
		} finally {
			try {
				exchanger.exchange(null, 1000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				LOG.debug("Exchange interrupted in close");
			} catch (TimeoutException e) {
				LOG.debug("Exchange timeout in close");
			}
		}

		grabber = null;
		videoDevice.releaseFrameGrabber();

		LOG.debug("V4L4J device {} has been closed", videoFile);

	}

	@Override
	public void dispose() {

		if (!disposed.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("Disposing V4L4J device {}", videoFile);

		if (open.get()) {
			close();
		}

		videoDevice.releaseControlList();
		videoDevice.release();

		LOG.debug("V4L4J device {} has been disposed", videoFile);
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}

	@Override
	public void nextFrame(VideoFrame frame) {

		LOG.trace("Next frame {}", frame);

		if (!open.get()) {
			return;
		}

		if (t1 == -1 || t2 == -1) {
			t1 = System.currentTimeMillis();
			t2 = System.currentTimeMillis();
		}

		try {
			exchanger.exchange(frame.getBufferedImage());
		} catch (InterruptedException e) {
			return;
		} finally {
			frame.recycle();
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
