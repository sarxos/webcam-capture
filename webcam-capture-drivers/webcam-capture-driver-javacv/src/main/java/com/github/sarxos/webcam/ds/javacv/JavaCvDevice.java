package com.github.sarxos.webcam.ds.javacv;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.videoInputLib.videoInput;


/**
 * UNSTABLE, EXPERIMENTALL STUFF !!!
 * 
 * @author bfiryn
 */
public class JavaCvDevice implements WebcamDevice {

	private int address = -1;
	private String name = null;
	private FrameGrabber grabber = null;

	private volatile boolean open = false;
	private volatile boolean disposed = false;

	public JavaCvDevice(int address) {
		this.address = address;
	}

	@Override
	public String getName() {
		if (name == null) {
			name = videoInput.getDeviceName(address);
		}
		return name;
	}

	@Override
	public Dimension[] getResolutions() {
		throw new WebcamException("Not implemented");
	}

	@Override
	public Dimension getResolution() {
		throw new WebcamException("Not implemented");
	}

	@Override
	public void setResolution(Dimension size) {
		throw new WebcamException("Not implemented");
	}

	@Override
	public BufferedImage getImage() {

		if (!open) {
			throw new WebcamException("Cannot grab image - webcam device is not open");
		}

		try {
			return grabber.grab().getBufferedImage();
		} catch (Exception e) {
			throw new WebcamException("Cannot grab image...");
		}
	}

	@Override
	public void open() {

		if (open || disposed) {
			return;
		}

		// CvCapture capture =
		// opencv_highgui.cvCreateCameraCapture(opencv_highgui.CV_CAP_DSHOW);
		// IplImage image = opencv_highgui.cvQueryFrame(capture);

		try {
			grabber = FrameGrabber.createDefault(address);
			grabber.start();
			open = true;
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
			release();
			throw new WebcamException(e);
		}
	}

	private void release() {
		if (grabber != null) {
			try {
				grabber.release();
			} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
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
		} catch (com.googlecode.javacv.FrameGrabber.Exception e) {
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
}
