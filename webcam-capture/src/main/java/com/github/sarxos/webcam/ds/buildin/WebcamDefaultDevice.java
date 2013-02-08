package com.github.sarxos.webcam.ds.buildin;

import java.awt.Dimension;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bridj.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class WebcamDefaultDevice implements WebcamDevice {

	static {
		if (!"true".equals(System.getProperty("webcam.debug"))) {
			System.setProperty("bridj.quiet", "true");
		}
	}

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamDefaultDevice.class);

	/**
	 * Artificial view sizes. I'm really not sure if will fit into other webcams
	 * but hope that OpenIMAJ can handle this.
	 */
	// @formatter:off
	private final static Dimension[] DIMENSIONS = new Dimension[] {
		WebcamResolution.QQVGA.getSize(),
		WebcamResolution.QVGA.getSize(),
		WebcamResolution.CIF.getSize(),
		WebcamResolution.HVGA.getSize(),
		WebcamResolution.VGA.getSize(),
		WebcamResolution.XGA.getSize(),
	};
	// @formatter:on

	/**
	 * RGB offsets.
	 */
	private static final int[] BAND_OFFSETS = new int[] { 0, 1, 2 };

	/**
	 * Number of bytes in each pixel.
	 */
	private static final int[] BITS = { 8, 8, 8 };

	/**
	 * Image offset.
	 */
	private static final int[] OFFSET = new int[] { 0 };

	/**
	 * Data type used in image.
	 */
	private static final int DATA_TYPE = DataBuffer.TYPE_BYTE;

	/**
	 * Image color space.
	 */
	private static final ColorSpace COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_sRGB);

	private OpenIMAJGrabber grabber = null;
	private Device device = null;
	private Dimension size = null;
	private ComponentSampleModel sampleModel = null;
	private ColorModel colorModel = null;
	private boolean failOnSizeMismatch = false;

	private AtomicBoolean disposed = new AtomicBoolean(false);
	private AtomicBoolean open = new AtomicBoolean(false);

	private String name = null;
	private String id = null;
	private String fullname = null;

	protected WebcamDefaultDevice(Device device) {
		this.device = device;
		this.name = device.getNameStr();
		this.id = device.getIdentifierStr();
		this.fullname = String.format("%s %s", this.name, this.id);
	}

	@Override
	public String getName() {
		return fullname;
	}

	@Override
	public Dimension[] getResolutions() {
		return DIMENSIONS;
	}

	@Override
	public Dimension getResolution() {
		return size;
	}

	@Override
	public void setResolution(Dimension size) {
		if (open.get()) {
			throw new IllegalStateException("Cannot change resolution when webcam is open, please close it first");
		}
		this.size = size;
	}

	@Override
	public BufferedImage getImage() {

		if (disposed.get()) {
			LOG.debug("Webcam is disposed, image will be null");
			return null;
		}

		if (!open.get()) {
			LOG.debug("Webcam is closed, image will be null");
			return null;
		}

		LOG.trace("Webcam device get image (next frame)");

		grabber.nextFrame();

		Pointer<Byte> image = grabber.getImage();
		if (image == null) {
			LOG.warn("Null array pointer found instead of image");
			return null;
		}

		int length = size.width * size.height * 3;

		LOG.trace("Webcam device get image (transfer buffer) {} bytes", length);

		byte[] bytes = image.getBytes(length);
		byte[][] data = new byte[][] { bytes };

		if (bytes == null) {
			LOG.error("Images byte array is null!");
			return null;
		}

		DataBufferByte buffer = new DataBufferByte(data, bytes.length, OFFSET);
		WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);

		BufferedImage bi = new BufferedImage(colorModel, raster, false, null);
		bi.flush();

		return bi;
	}

	@Override
	public void open() {

		if (disposed.get()) {
			return;
		}

		LOG.debug("Opening webcam device {}", getName());

		if (size == null) {
			size = getResolutions()[0];
		}

		LOG.debug("Webcam device {} starting session, size {}", device.getIdentifierStr(), size);

		grabber = new OpenIMAJGrabber();

		// NOTE!

		// Following the note from OpenIMAJ code - it seams like there is some
		// issue on 32-bit systems which prevents grabber to find devices.
		// According to the mentioned note this for loop shall fix the problem.

		DeviceList list = grabber.getVideoDevices().get();
		for (Device d : list.asArrayList()) {
			d.getNameStr();
			d.getIdentifierStr();
		}

		boolean started = grabber.startSession(size.width, size.height, 50, Pointer.pointerTo(device));
		if (!started) {
			throw new WebcamException("Cannot start native grabber!");
		}

		LOG.debug("Webcam device session started");

		Dimension size2 = new Dimension(grabber.getWidth(), grabber.getHeight());

		int w1 = size.width;
		int w2 = size2.width;
		int h1 = size.height;
		int h2 = size2.height;

		if (w1 != w2 || h1 != h2) {

			if (failOnSizeMismatch) {
				throw new WebcamException(String.format("Different size obtained vs requested - [%dx%d] vs [%dx%d]", w1, h1, w2, h2));
			}

			LOG.warn("Different size obtained vs requested - [{}x{}] vs [{}x{}]. Setting correct one. New size is [{}x{}]", new Object[] { w1, h1, w2, h2, w2, h2 });
			size = new Dimension(w2, h2);
		}

		sampleModel = new ComponentSampleModel(DATA_TYPE, size.width, size.height, 3, size.width * 3, BAND_OFFSETS);
		colorModel = new ComponentColorModel(COLOR_SPACE, BITS, false, false, Transparency.OPAQUE, DATA_TYPE);

		LOG.debug("Initialize buffer");

		int i = 0;
		do {

			grabber.nextFrame();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.error("Nasty interrupted exception", e);
			}

		} while (++i < 3);

		LOG.debug("Webcam device is now open");

		open.set(true);
	}

	@Override
	public void close() {

		if (!open.compareAndSet(true, false)) {
			return;
		}

		LOG.debug("Closing webcam device");

		grabber.stopSession();
	}

	@Override
	public void dispose() {

		if (!disposed.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("Disposing webcam device {}", getName());

		close();
	}

	/**
	 * Determines if device should fail when requested image size is different
	 * than actually received.
	 * 
	 * @param fail the fail on size mismatch flag, true or false
	 */
	public void setFailOnSizeMismatch(boolean fail) {
		this.failOnSizeMismatch = fail;
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}
}
