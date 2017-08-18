package com.github.sarxos.webcam.ds.javacv;

import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FRAME_WIDTH;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.opencv_videoio.VideoCapture;
import org.bytedeco.javacpp.videoInputLib.videoInput;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.util.OsUtils;


/**
 * This is {@link WebcamDevice} implementation which uses OpenCV (via JavaCV) as the underlying
 * capturing framework.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class JavaCvDevice implements WebcamDevice {

	private final int address;
	private final File vfile;

	private String name = null;
	private OpenCVFrameGrabber grabber = null;

	/**
	 * Lazy loaded list of resolutions supported by camera.
	 */
	private Dimension[] resolutions = null;

	/**
	 * Resolution set by user (or first one from the list of supported ones if not specified)..
	 */
	private Dimension resolution = null;

	private volatile boolean open = false;
	private volatile boolean disposed = false;

	public JavaCvDevice(int address) {
		this.address = address;
		this.vfile = null;
	}

	public JavaCvDevice(File vfile) {
		this.address = -1;
		this.vfile = vfile;
	}

	@Override
	public String getName() {
		if (name == null) {
			name = getWebcamNameForOs();
		}
		return name;
	}

	private String getWebcamNameForOs() {
		switch (OsUtils.getOS()) {
			case WIN:
				return videoInput.getDeviceName(address).getString();
			case OSX:
				return "Webcam " + address; // XXX: any clues how to get webcam name on mac?
			case NIX:
				return vfile.getAbsolutePath();
			default:
				throw new UnsupportedOperationException("Unsupported operating system");
		}
	}

	private VideoCapture getVideoCaptureForOs() {
		switch (OsUtils.getOS()) {
			case WIN:
			case OSX:
				return new VideoCapture(address);
			case NIX:
				return new VideoCapture(vfile.getAbsolutePath());
			default:
				throw new UnsupportedOperationException("Unsupported operating system");
		}
	}

	@Override
	public Dimension[] getResolutions() {

		if (resolutions != null) {
			return resolutions;
		}

		final List<Dimension> supported = new ArrayList<Dimension>();

		VideoCapture vc = null;
		try {
			vc = getVideoCaptureForOs();

			for (WebcamResolution r : WebcamResolution.values()) {

				// this trick is to get resolutions supported by a webcam identified by this device,
				// more details on how it works can be found in this stackoverflow question:
				// https://stackoverflow.com/questions/18458422/query-maximum-webcam-resolution-in-opencv

				final double w1 = r.getWidth();
				final double h1 = r.getHeight();

				vc.set(CV_CAP_PROP_FRAME_WIDTH, w1);
				vc.set(CV_CAP_PROP_FRAME_HEIGHT, h1);

				final double w2 = vc.get(CV_CAP_PROP_FRAME_WIDTH);
				final double h2 = vc.get(CV_CAP_PROP_FRAME_HEIGHT);

				if (w1 == w2 && h1 == h2) {
					supported.add(r.getSize());
				}
			}
		} finally {
			if (vc != null) {
				vc.close();
			}
		}

		resolutions = supported.toArray(new Dimension[0]);

		return resolutions;
	}

	@Override
	public Dimension getResolution() {
		if (resolution == null) {
			resolution = getResolutions()[0];
		}
		return resolution;
	}

	@Override
	public void setResolution(Dimension resolution) {
		this.resolution = resolution;
	}

	@Override
	public BufferedImage getImage() {

		if (!open) {
			throw new WebcamException("Cannot grab image - webcam device is not open");
		}

		Frame frame = null;
		try {
			frame = grabber.grab();
		} catch (Exception e) {
			throw new WebcamException("OpenCV cannot grab image frame", e);
		}
		if (frame == null) {
			throw new WebcamException("OpenCV image frame is null");
		}

		return new Java2DFrameConverter().convert(frame);
	}

	private OpenCVFrameGrabber buildGrabber() throws FrameGrabber.Exception {

		switch (OsUtils.getOS()) {
			case WIN:
			case OSX:
				return OpenCVFrameGrabber.createDefault(address);
			case NIX:
				return OpenCVFrameGrabber.createDefault(vfile);
			default:
				throw new UnsupportedOperationException("Current OS is not supported");
		}
	}

	public OpenCVFrameGrabber getGrabber() {
		return grabber;
	}

	@Override
	public void open() {

		if (open || disposed) {
			return;
		}

		try {

			final Dimension resolution = getResolution();

			grabber = buildGrabber();
			grabber.setImageWidth(resolution.width);
			grabber.setImageHeight(resolution.height);
			grabber.start();

			open = true;

		} catch (FrameGrabber.Exception e) {
			release();
			throw new WebcamException(e);
		}
	}

	private void release() {
		if (grabber != null) {
			try {
				grabber.release();
			} catch (FrameGrabber.Exception e) {
				throw new WebcamException(e);
			}
		}
	}

	@Override
	public void close() {

		if (!open) {
			return;
		}

		try {
			grabber.stop();
		} catch (FrameGrabber.Exception e) {
			throw new WebcamException(e);
		} finally {
			dispose();
		}
	}

	@Override
	public void dispose() {
		disposed = true;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public String toString() {
		return getClass().getName() + "#address=" + address + "#vfile=" + vfile;
	}

}
