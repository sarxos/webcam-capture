package com.github.sarxos.webcam.ds.civil;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * Webcam device - implementation for LTI Civil framework.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class CivilDevice implements WebcamDevice, CaptureObserver {

	private static final Logger LOG = LoggerFactory.getLogger(CivilDevice.class);

	private CaptureDeviceInfo cdi = null;
	private List<Dimension> dimensions = null;
	private Dimension size = null;
	private Image image = null;
	private CaptureStream stream = null;
	private boolean open = false;
	private boolean capturing = false;

	public CivilDevice(CaptureDeviceInfo cdi) {
		this.cdi = cdi;
	}

	public String getName() {
		return cdi.getDescription();
	}

	public Dimension[] getSizes() {

		if (dimensions == null) {
			dimensions = new ArrayList<Dimension>();

			CaptureSystem system = CivilDataSource.getCaptureSystem();
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

	public BufferedImage getImage() {
		if (!capturing) {
			return null;
		}
		return AWTImageConverter.toBufferedImage(image);
	}

	public void onError(CaptureStream stream, CaptureException e) {
		LOG.error("Exception in capture stream", e);
	}

	public void onNewImage(CaptureStream stream, Image image) {
		this.image = image;
		this.capturing = true;
	}

	public void open() {
		if (open) {
			return;
		}
		try {
			stream = CivilDataSource.getCaptureSystem().openCaptureDeviceStream(cdi.getDeviceID());
			stream.setVideoFormat(findFormat());
			stream.setObserver(this);
			stream.start();
		} catch (CaptureException e) {
			LOG.error("Capture exception when opening Civil device", e);
		}
		while (true) {
			if (capturing) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
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

	public void close() {
		if (!open) {
			return;
		}
		try {
			stream.stop();
			stream.dispose();
		} catch (CaptureException e) {
			LOG.error("Capture exception when closing Civil device", e);
		}
	}

	public Dimension getSize() {
		return size;
	}

	public void setSize(Dimension d) {
		this.size = d;
	}
}
