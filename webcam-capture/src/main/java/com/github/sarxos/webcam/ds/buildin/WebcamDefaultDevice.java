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

import org.bridj.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class WebcamDefaultDevice implements WebcamDevice {

	private static final Logger LOG = LoggerFactory.getLogger(WebcamDefaultDevice.class);

	/**
	 * Artificial view sizes. I'm really not sure if will fit into other webcams
	 * but hope that OpenIMAJ can handle this.
	 */
	//@formatter:off
	private final static Dimension[] DIMENSIONS = new Dimension[] {
		new Dimension(176, 144),
		new Dimension(320, 240),
		new Dimension(352, 288),
		new Dimension(640, 400),
		new Dimension(640, 480),
	};
	//@formatter:on

	private static final int[] BAND_OFFSETS = new int[] { 0, 1, 2 };
	private static final int[] BITS = { 8, 8, 8 };
	private static final int[] OFFSET = new int[] { 0 };
	private static final int DATA_TYPE = DataBuffer.TYPE_BYTE;

	private static final ColorSpace COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_sRGB);

	private OpenIMAJGrabber grabber = null;
	private Device device = null;
	private Dimension size = null;
	private ComponentSampleModel sampleModel = null;
	private ColorModel colorModel = null;
	private volatile boolean open = false;

	protected WebcamDefaultDevice(Device device) {
		this.device = device;
	}

	@Override
	public String getName() {
		return device.getNameStr() + " " + device.getIdentifierStr();
	}

	@Override
	public Dimension[] getSizes() {
		return DIMENSIONS;
	}

	@Override
	public Dimension getSize() {
		return size;
	}

	@Override
	public void setSize(Dimension size) {
		this.size = size;
	}

	@Override
	public BufferedImage getImage() {

		if (!open) {
			throw new WebcamException("Cannot get image when webcam device is not open");
		}

		grabber.nextFrame();

		Pointer<Byte> image = grabber.getImage();
		if (image == null) {
			return null;
		}

		byte[] bytes = image.getBytes(size.width * size.height * 3);
		byte[][] data = new byte[][] { bytes };

		DataBufferByte buffer = new DataBufferByte(data, bytes.length, OFFSET);
		WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);

		BufferedImage bi = new BufferedImage(colorModel, raster, false, null);
		bi.flush();

		return bi;
	}

	@Override
	public synchronized void open() {

		if (open) {
			return;
		}

		if (size == null) {
			size = getSizes()[0];
		}

		grabber = new OpenIMAJGrabber();

		boolean started = grabber.startSession(size.width, size.height, 0, Pointer.pointerTo(device));
		if (!started) {
			throw new WebcamException("Cannot start video data grabber!");
		}

		int w1 = size.width;
		int w2 = grabber.getWidth();
		int h1 = size.height;
		int h2 = grabber.getHeight();

		if (w1 != w2 || h1 != h2) {
			LOG.warn("Different size obtained vs requested - [" + w1 + "x" + h1 + "] vs [" + w2 + "x" + h2 + "]. Setting correct one. New size is [" + w2 + "x" + h2 + "]");
			size = new Dimension(w2, h2);
		}

		sampleModel = new ComponentSampleModel(DATA_TYPE, size.width, size.height, 3, size.width * 3, BAND_OFFSETS);
		colorModel = new ComponentColorModel(COLOR_SPACE, BITS, false, false, Transparency.OPAQUE, DATA_TYPE);

		int i = 0;
		do {
			grabber.nextFrame();
			grabber.getImage();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.error("Nasty interrupted exception", e);
			}
		} while (i++ < 3);

		open = true;
	}

	@Override
	public void close() {

		if (!open) {
			return;
		}

		grabber.stopSession();
		open = false;
	}

}
