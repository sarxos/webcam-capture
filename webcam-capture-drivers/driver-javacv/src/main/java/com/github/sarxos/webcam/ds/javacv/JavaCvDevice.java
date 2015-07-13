package com.github.sarxos.webcam.ds.javacv;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import org.bytedeco.javacpp.videoInputLib.videoInput;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.util.OsUtils;


/**
 * UNSTABLE, EXPERIMENTALL STUFF !!!
 *
 * @author bfiryn
 */
public class JavaCvDevice implements WebcamDevice {

	private int address = -1;
	private File vfile = null;

	private String name = null;
	private FrameGrabber grabber = null;

	private volatile boolean open = false;
	private volatile boolean disposed = false;

	public JavaCvDevice(int address) {
		this.address = address;
	}

	public JavaCvDevice(File vfile) {
		this.vfile = vfile;
	}

	@Override
	public String getName() {
		if (name == null) {
			switch (OsUtils.getOS()) {
				case WIN:
					name = videoInput.getDeviceName(address).getString();
					break;
				case NIX:
					name = vfile.getAbsolutePath();
					break;
				case OSX:
					throw new UnsupportedOperationException("Mac OS is not supported");
			}
		}
		return name;
	}

	@Override
	public Dimension[] getResolutions() {
		// grabber.get

		throw new WebcamException("Not implemented");
	}

	@Override
	public Dimension getResolution() {
		return WebcamResolution.VGA.getSize();
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

	private FrameGrabber buildGrabber() throws FrameGrabber.Exception {
		switch (OsUtils.getOS()) {
			case WIN:
				return FrameGrabber.createDefault(address);
			case NIX:
				return FrameGrabber.createDefault(vfile);
			case OSX:
			default:
				throw new UnsupportedOperationException("Current OS is not supported");
		}
	}

	public FrameGrabber getGrabber() {
		return grabber;
	}

	@Override
	public void open() {

		if (open || disposed) {
			return;
		}

		try {

			grabber = buildGrabber();
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
