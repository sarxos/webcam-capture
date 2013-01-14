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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.buildin.cgt.CloseSessionTask;
import com.github.sarxos.webcam.ds.buildin.cgt.GetImageTask;
import com.github.sarxos.webcam.ds.buildin.cgt.GetSizeTask;
import com.github.sarxos.webcam.ds.buildin.cgt.NextFrameTask;
import com.github.sarxos.webcam.ds.buildin.cgt.StartSessionTask;
import com.github.sarxos.webcam.ds.buildin.natives.Device;


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

	/**
	 * Synchronous webcam video grabber processor.
	 */
	private final WebcamGrabberProcessor processor = new WebcamGrabberProcessor();

	// synchronous grabber tasks

	private final NextFrameTask frameTask = new NextFrameTask(processor);
	private final GetImageTask imageTask = new GetImageTask(processor);
	private final StartSessionTask sessionTask = new StartSessionTask(processor);
	private final GetSizeTask sizeTask = new GetSizeTask(processor);
	private final CloseSessionTask closeTask = new CloseSessionTask(processor);

	private Device device = null;
	private Dimension size = null;
	private ComponentSampleModel sampleModel = null;
	private ColorModel colorModel = null;
	private boolean failOnSizeMismatch = false;

	private volatile boolean open = false;
	private volatile boolean opening = false;
	private volatile boolean disposed = false;

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

		if (disposed) {
			throw new WebcamException("Cannot get image since device is already disposed");
		}

		if (!open) {
			LOG.error("Cannot get image when device is closed");
			return null;
		}

		frameTask.nextFrame();

		byte[] bytes = imageTask.getImage(size);
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

		if (disposed) {
			throw new WebcamException("Cannot open webcam when device it's already disposed");
		}

		synchronized (device) {

			if (opening) {
				try {
					device.wait();
				} catch (InterruptedException e) {
					throw new WebcamException("Opening wait interrupted");
				} finally {
					opening = false;
				}
			}

			if (open) {
				return;
			} else {
				opening = true;
			}
		}

		if (size == null) {
			size = getSizes()[0];
		}

		try {

			boolean started = sessionTask.startSession(size, device);
			if (!started) {
				throw new WebcamException("Cannot start video data grabber!");
			}

			Dimension size2 = sizeTask.getSize();

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

			int i = 0;
			do {

				frameTask.nextFrame();
				imageTask.getImage(size);

				if (disposed) {
					opening = false;
					return;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOG.error("Nasty interrupted exception", e);
				}
			} while (i++ < 3);

			open = true;
			opening = false;

		} finally {

			// notify all threads which are also waiting for device to be open
			synchronized (device) {
				device.notifyAll();
			}
		}
	}

	@Override
	public void close() {

		if (!open) {
			return;
		}

		synchronized (device) {
			closeTask.closeSession();
			open = false;
		}
	}

	@Override
	public void dispose() {
		if (disposed) {
			return;
		}
		close();
		disposed = true;
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
}
