package com.github.sarxos.webcam.ds.civil;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.lti.civil.CaptureDeviceInfo;
import com.lti.civil.CaptureException;
import com.lti.civil.CaptureObserver;
import com.lti.civil.CaptureStream;
import com.lti.civil.CaptureSystem;
import com.lti.civil.Image;
import com.lti.civil.VideoFormat;
import com.lti.civil.awt.AWTImageConverter;


/**
 * Webcam device - LTI-CIVIL framework compatible implementation.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class LtiCivilDevice implements WebcamDevice, CaptureObserver, WebcamDevice.FPSSource {

	private static final Logger LOG = LoggerFactory.getLogger(LtiCivilDevice.class);

	private CaptureDeviceInfo cdi = null;
	private List<Dimension> dimensions = null;
	private Dimension size = null;
	private Image image = null;
	private CaptureStream stream = null;

	private AtomicBoolean open = new AtomicBoolean(false);

	private volatile boolean capturing = false;
	private volatile boolean disposed = false;

	private long t1 = -1;
	private long t2 = -1;

	private volatile double fps = 0;

	protected LtiCivilDevice(CaptureDeviceInfo cdi) {
		this.cdi = cdi;
	}

	@Override
	public String getName() {
		return cdi.getDescription();
	}

	@Override
	public Dimension[] getResolutions() {

		if (dimensions == null) {
			dimensions = new ArrayList<Dimension>();

			CaptureSystem system = LtiCivilDriver.getCaptureSystem();
			Set<Dimension> set = new HashSet<Dimension>();

			try {

				stream = system.openCaptureDeviceStream(cdi.getDeviceID());

				for (VideoFormat format : stream.enumVideoFormats()) {
					if (format.getFormatType() == VideoFormat.RGB24) {
						set.add(new Dimension(format.getWidth(), format.getHeight()));
					}
				}

				stream.dispose();

			} catch (CaptureException e) {
				LOG.error("Capture exception when collecting formats dimension", e);
			}

			dimensions.addAll(set);

			Collections.sort(dimensions, new Comparator<Dimension>() {

				@Override
				public int compare(Dimension a, Dimension b) {
					int apx = a.width * a.height;
					int bpx = b.width * b.height;
					if (apx > bpx) {
						return 1;
					} else if (apx < bpx) {
						return -1;
					} else {
						return 0;
					}
				}
			});
		}

		return dimensions.toArray(new Dimension[dimensions.size()]);
	}

	@Override
	public BufferedImage getImage() {
		if (!capturing) {
			return null;
		}
		return AWTImageConverter.toBufferedImage(image);
	}

	@Override
	public void onError(CaptureStream stream, CaptureException e) {
		LOG.error("Exception in capture stream", e);
	}

	@Override
	public void onNewImage(CaptureStream stream, Image image) {

		if (t1 == -1 || t2 == -1) {
			t1 = System.currentTimeMillis();
			t2 = System.currentTimeMillis();
		}

		this.image = image;
		this.capturing = true;

		t1 = t2;
		t2 = System.currentTimeMillis();

		fps = (4 * fps + 1000 / (t2 - t1 + 1)) / 5;
	}

	@Override
	public void open() {

		if (disposed) {
			return;
		}

		if (open.compareAndSet(false, true)) {

			try {
				stream = LtiCivilDriver.getCaptureSystem().openCaptureDeviceStream(cdi.getDeviceID());
				stream.setVideoFormat(findFormat());
				stream.setObserver(this);
				stream.start();
			} catch (CaptureException e) {
				LOG.error("Capture exception when opening Civil device", e);
			}
		}

		while (true) {
			if (capturing) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private VideoFormat findFormat() {
		if (stream == null) {
			throw new RuntimeException("Stream is null");
		}
		if (size == null) {
			throw new RuntimeException("Size is not set");
		}
		try {
			for (VideoFormat format : stream.enumVideoFormats()) {
				if (format.getFormatType() == VideoFormat.RGB24) {
					boolean xok = size.width == format.getWidth();
					boolean yok = size.height == format.getHeight();
					if (xok && yok) {
						return format;
					}
				}
			}
		} catch (CaptureException e) {
			LOG.error("Capture exception when iterating thru video formats", e);
		}
		throw new RuntimeException("Cannot find RGB24 video format for size [" + size.width + "x" + size.height + "]");
	}

	@Override
	public void close() {
		if (open.compareAndSet(true, false)) {
			try {
				stream.stop();
				stream.dispose();
			} catch (CaptureException e) {
				LOG.error("Capture exception when closing Civil device", e);
			}
		}
	}

	@Override
	public Dimension getResolution() {
		return size;
	}

	@Override
	public void setResolution(Dimension d) {
		this.size = d;
	}

	@Override
	public void dispose() {
		disposed = true;
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}

	@Override
	public double getFPS() {
		return fps;
	}
}
