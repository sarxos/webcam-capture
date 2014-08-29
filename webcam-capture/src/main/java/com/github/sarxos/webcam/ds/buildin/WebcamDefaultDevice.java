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
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bridj.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDevice.BufferAccess;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamExceptionHandler;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamTask;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class WebcamDefaultDevice implements WebcamDevice, BufferAccess, Runnable, WebcamDevice.FPSSource {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(WebcamDefaultDevice.class);

	/**
	 * Artificial view sizes. I'm really not sure if will fit into other webcams
	 * but hope that OpenIMAJ can handle this.
	 */
	private final static Dimension[] DIMENSIONS = new Dimension[] {
		WebcamResolution.QQVGA.getSize(),
		WebcamResolution.QVGA.getSize(),
		WebcamResolution.VGA.getSize(),
	};

	private class NextFrameTask extends WebcamTask {

		private final AtomicInteger result = new AtomicInteger(0);

		public NextFrameTask(WebcamDevice device) {
			super(device);
		}

		public int nextFrame() {
			try {
				process();
			} catch (InterruptedException e) {
				LOG.debug("Image buffer request interrupted", e);
			}
			return result.get();
		}

		@Override
		protected void handle() {

			WebcamDefaultDevice device = (WebcamDefaultDevice) getDevice();
			if (!device.isOpen()) {
				return;
			}

			grabber.setTimeout(timeout);
			result.set(grabber.nextFrame());
			fresh.set(true);
		}
	}

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
	 * Maximum image acquisition time (in milliseconds).
	 */
	private int timeout = 5000;

	private OpenIMAJGrabber grabber = null;
	private Device device = null;
	private Dimension size = null;
	private ComponentSampleModel smodel = null;
	private ColorModel cmodel = null;
	private boolean failOnSizeMismatch = false;

	private final AtomicBoolean disposed = new AtomicBoolean(false);
	private final AtomicBoolean open = new AtomicBoolean(false);

	/**
	 * Is the last image fresh one.
	 */
	private final AtomicBoolean fresh = new AtomicBoolean(false);

	private Thread refresher = null;

	private String name = null;
	private String id = null;
	private String fullname = null;

	private long t1 = -1;
	private long t2 = -1;

	/**
	 * Current FPS.
	 */
	private volatile double fps = 0;

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

	public String getDeviceName() {
		return name;
	}

	public String getDeviceId() {
		return id;
	}

	public Device getDeviceRef() {
		return device;
	}

	@Override
	public Dimension[] getResolutions() {
		return DIMENSIONS;
	}

	@Override
	public Dimension getResolution() {
		if (size == null) {
			size = getResolutions()[0];
		}
		return size;
	}

	@Override
	public void setResolution(Dimension size) {

		if (size == null) {
			throw new IllegalArgumentException("Size cannot be null");
		}

		if (open.get()) {
			throw new IllegalStateException("Cannot change resolution when webcam is open, please close it first");
		}

		this.size = size;
	}

	@Override
	public ByteBuffer getImageBytes() {

		if (disposed.get()) {
			LOG.debug("Webcam is disposed, image will be null");
			return null;
		}
		if (!open.get()) {
			LOG.debug("Webcam is closed, image will be null");
			return null;
		}

		// if image is not fresh, update it

		if (fresh.compareAndSet(false, true)) {
			updateFrameBuffer();
		}

		// get image buffer

		LOG.trace("Webcam grabber get image pointer");

		Pointer<Byte> image = grabber.getImage();
		fresh.set(false);

		if (image == null) {
			LOG.warn("Null array pointer found instead of image");
			return null;
		}

		int length = size.width * size.height * 3;

		LOG.trace("Webcam device get buffer, read {} bytes", length);

		return image.getByteBuffer(length);
	}

	@Override
	public void getImageBytes(ByteBuffer target) {

		if (disposed.get()) {
			LOG.debug("Webcam is disposed, image will be null");
			return;
		}
		if (!open.get()) {
			LOG.debug("Webcam is closed, image will be null");
			return;
		}

		int minSize = size.width * size.height * 3;
		int curSize = target.remaining();

		if (minSize < curSize) {
			throw new IllegalArgumentException(String.format("Not enough remaining space in target buffer (%d necessary vs %d remaining)", minSize, curSize));
		}

		// if image is not fresh, update it

		if (fresh.compareAndSet(false, true)) {
			updateFrameBuffer();
		}

		// get image buffer

		LOG.trace("Webcam grabber get image pointer");

		Pointer<Byte> image = grabber.getImage();
		fresh.set(false);

		if (image == null) {
			LOG.warn("Null array pointer found instead of image");
			return;
		}

		LOG.trace("Webcam device read buffer {} bytes", minSize);

		image = image.validBytes(minSize);
		image.getBytes(target);

	}

	@Override
	public BufferedImage getImage() {

		ByteBuffer buffer = getImageBytes();

		if (buffer == null) {
			LOG.error("Images bytes buffer is null!");
			return null;
		}

		byte[] bytes = new byte[size.width * size.height * 3];
		byte[][] data = new byte[][] { bytes };

		buffer.get(bytes);

		DataBufferByte dbuf = new DataBufferByte(data, bytes.length, OFFSET);
		WritableRaster raster = Raster.createWritableRaster(smodel, dbuf, null);

		BufferedImage bi = new BufferedImage(cmodel, raster, false, null);
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
		if (size == null) {
			throw new RuntimeException("The resolution size cannot be null");
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

			Object[] args = new Object[] { w1, h1, w2, h2, w2, h2 };
			LOG.warn("Different size obtained vs requested - [{}x{}] vs [{}x{}]. Setting correct one. New size is [{}x{}]", args);

			size = new Dimension(w2, h2);
		}

		smodel = new ComponentSampleModel(DATA_TYPE, size.width, size.height, 3, size.width * 3, BAND_OFFSETS);
		cmodel = new ComponentColorModel(COLOR_SPACE, BITS, false, false, Transparency.OPAQUE, DATA_TYPE);

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

		LOG.debug("Webcam device {} is now open", this);

		open.set(true);

		refresher = new Thread(this, String.format("frames-refresher-[%s]", id));
		refresher.setUncaughtExceptionHandler(WebcamExceptionHandler.getInstance());
		refresher.setDaemon(true);
		refresher.start();
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

	/**
	 * Get timeout for image acquisition.
	 * 
	 * @return Value in milliseconds
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Set timeout for image acquisition.
	 * 
	 * @param timeout the timeout value in milliseconds
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Update underlying memory buffer and fetch new frame.
	 */
	private void updateFrameBuffer() {

		LOG.trace("Next frame");

		if (t1 == -1 || t2 == -1) {
			t1 = System.currentTimeMillis();
			t2 = System.currentTimeMillis();
		}

		int result = new NextFrameTask(this).nextFrame();

		t1 = t2;
		t2 = System.currentTimeMillis();

		fps = (4 * fps + 1000 / (t2 - t1 + 1)) / 5;

		if (result == -1) {
			LOG.error("Timeout when requesting image!");
		} else if (result < -1) {
			LOG.error("Error requesting new frame!");
		}
	}

	@Override
	public void run() {

		do {

			if (Thread.interrupted()) {
				LOG.debug("Refresher has been interrupted");
				return;
			}

			if (!open.get()) {
				LOG.debug("Cancelling refresher");
				return;
			}

			updateFrameBuffer();

		} while (open.get());
	}

	@Override
	public double getFPS() {
		return fps;
	}
}
