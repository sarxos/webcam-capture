package com.github.sarxos.webcam.ds.vlcj;

import java.awt.Dimension;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDevice.FPSSource;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.util.OsUtils;
import com.sun.jna.Memory;

import uk.co.caprica.vlcj.medialist.MediaListItem;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;


/**
 * This is capture driver which uses <a href="http://caprica.github.io/vlcj/">vlcj</a> library to
 * access webcam hardware.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class VlcjDevice implements WebcamDevice, BufferFormatCallback, RenderCallback, FPSSource {

	/**
	 * This class is to convert vlcj {@link Memory} to {@link BufferedImage}.
	 *
	 * @author Bartosz Firyn (sarxos)
	 */
	private static class Converter {

		/**
		 * Converters are cached and created on demand for every width-height tuple.
		 */
		private static final Map<String, Converter> CONVERTERS = new HashMap<String, Converter>();

		/**
		 * The buffer data type (buffer consist of 4 byte values for every pixel).
		 */
		final int dataType = DataBuffer.TYPE_BYTE;

		/**
		 * Number of bytes per pixel.
		 */
		final int pixelStride = 4;

		/**
		 * Number of bytes per line.
		 */
		final int scanlineStride;

		/**
		 * Band offsets for BGR components (B = 2, G = 1, R = 0).
		 */
		final int[] bgrBandOffsets = new int[] { 2, 1, 0 };

		/**
		 * Number of bits per component.
		 */
		final int[] bits = { 8, 8, 8 };

		/**
		 * Offset between pixels.
		 */
		final int[] offsets = new int[] { 0 };

		/**
		 * Transparency type (opaque since there is no transparency in the image).
		 */
		final int transparency = Transparency.OPAQUE;

		/**
		 * Color space, a standard default color space for the Internet - sRGB.
		 */
		final ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);

		/**
		 * Image sample model.
		 */
		final ComponentSampleModel sampleModel;

		/**
		 * Image color model.
		 */
		final ComponentColorModel colorModel = new ComponentColorModel(colorSpace, bits, false, false, transparency, dataType);

		private Converter(int width, int height) {
			this.scanlineStride = width * pixelStride;
			this.sampleModel = new ComponentSampleModel(dataType, width, height, pixelStride, scanlineStride, bgrBandOffsets);
		}

		/**
		 * Get memory converter for given width-height tuple.
		 *
		 * @param width the image width
		 * @param height the image height
		 * @return Converter
		 */
		public static Converter getConverter(int width, int height) {
			String key = key(width, height);
			Converter converter = CONVERTERS.get(key);
			if (converter == null) {
				converter = new Converter(width, height);
				CONVERTERS.put(key, converter);
			}
			return converter;
		}

		/**
		 * Use width and height to create map key.
		 *
		 * @param width the image width
		 * @param height the image height
		 * @return Map key
		 */
		private static String key(int width, int height) {
			return width + "x" + height;
		}

		/**
		 * Converts {@link Memory} into {@link BufferedImage}.
		 *
		 * @param buffers the {@link Memory} buffers
		 * @param format the image format
		 * @return {@link BufferedImage} created from {@link Memory}
		 */
		public BufferedImage convert(Memory[] buffers, BufferFormat format) {

			// sanity, check if buffers is not empty

			if (buffers.length == 0) {
				throw new RuntimeException("No memory elements found!");
			}

			// sanity check if buffer is not null

			final Memory memory = buffers[0];
			if (memory == null) {
				throw new RuntimeException("Null memory!");
			}

			// transfer bytes into array

			final byte[] bytes = new byte[scanlineStride * format.getHeight()];
			final byte[][] data = new byte[][] { bytes };

			memory
				.getByteBuffer(0, memory.size())
				.get(bytes);

			// create image

			DataBufferByte dataBuffer = new DataBufferByte(data, bytes.length, offsets);
			WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
			BufferedImage image = new BufferedImage(colorModel, raster, false, null);

			// flush reconstructable resources to free memory

			image.flush();

			// return image

			return image;
		}
	}

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(VlcjDevice.class);

	/**
	 * Artificial view sizes. The vlcj is not able to detect resolutions supported by the webcam. If
	 * you would like to detect resolutions and have high-quality with good performance images
	 * streaming, you should rather use gstreamer or v4lvj capture drivers.
	 */
	private final static Dimension[] RESOLUTIONS = new Dimension[] {
		WebcamResolution.QQVGA.getSize(),
		WebcamResolution.QVGA.getSize(),
		WebcamResolution.VGA.getSize(),
	};

	/**
	 * VLC args by Andrew Davison:<br>
	 * http://fivedots.coe.psu.ac.th/~ad/jg/nui025/snapsWithoutJMF.pdf
	 */
	private final static String[] VLC_ARGS = {
		"--no-video-title-show", // do not display title
		"--no-stats", // no stats
		"--no-sub-autodetect-file", // no subtitles
		"--no-snapshot-preview", // no snapshot previews
		"--live-caching=50", // reduce capture lag/latency
		"--quiet", // turn off warnings
	};

	/**
	 * Used to calculate FPS.
	 */
	private long t1 = -1;

	/**
	 * Used to calculate FPS.
	 */
	private long t2 = -1;

	/**
	 * Image exchange reference.
	 */
	private final AtomicReference<BufferedImage> imageRef = new AtomicReference<BufferedImage>();

	/**
	 * Current FPS.
	 */
	private final AtomicReference<Double> fps = new AtomicReference<Double>((double) 0);

	private final MediaListItem item;
	private final MediaListItem sub;

	/**
	 * Factory for media player instances.
	 */
	private MediaPlayerFactory factory;

	/**
	 * Specification for a media player that provides direct access to the video frame data.
	 */
	private DirectMediaPlayer player;

	/**
	 * Is in opening phase?
	 */
	private final AtomicBoolean opening = new AtomicBoolean();

	/**
	 * Is open?
	 */
	private final AtomicBoolean open = new AtomicBoolean();

	/**
	 * Is disposed?
	 */
	private final AtomicBoolean disposed = new AtomicBoolean();

	/**
	 * Current resolution.
	 */
	private Dimension resolution = null;

	protected VlcjDevice(MediaListItem item) {

		if (item == null) {
			throw new IllegalArgumentException("Media list item cannot be null!");
		}

		this.item = item;
		this.sub = item.subItems().isEmpty() ? item : item.subItems().get(0);

		LOG.trace("New device created {}", this);
	}

	/**
	 * Get capture device protocol. This will be:
	 * <ul>
	 * <li><code>dshow://</code> for Windows</li>
	 * <li><code>qtcapture://</code> for Mac</li>
	 * <li><code>v4l2://</code> for linux</li>
	 * </ul>
	 *
	 * @return Capture device protocol
	 * @throws WebcamException in case when there is no support for given operating system
	 */
	public String getCaptureDevice() {
		switch (OsUtils.getOS()) {
			case WIN:
				return "dshow://";
			case OSX:
				return "qtcapture://";
			case NIX:
				return "v4l2://";
			default:
				throw new WebcamException("Capture device not supported on " + OsUtils.getOS());
		}
	}

	public MediaListItem getMediaListItem() {
		return item;
	}

	public MediaListItem getMediaListItemSub() {
		return sub;
	}

	@Override
	public String getName() {
		return sub.name();
	}

	public String getMRL() {
		return sub.mrl();
	}

	public String getVDevice() {
		return getMRL().replace(getCaptureDevice(), "");
	}

	@Override
	public String toString() {
		return String.format("%s[%s (%s)]", getClass().getSimpleName(), getName(), getMRL());
	}

	@Override
	public Dimension[] getResolutions() {
		return RESOLUTIONS;
	}

	@Override
	public Dimension getResolution() {
		return resolution;
	}

	@Override
	public void setResolution(Dimension resolution) {
		this.resolution = resolution;
	}

	@Override
	public BufferedImage getImage() {

		if (!open.get()) {
			throw new WebcamException("Cannot get image, webcam device is not open");
		}

		BufferedImage image = null;

		// wait for image

		while ((image = imageRef.getAndSet(null)) == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return null;
			}
		}

		return image;
	}

	@Override
	public synchronized void open() {

		if (disposed.get()) {
			LOG.warn("Cannot open device because it has been already disposed");
			return;
		}

		if (open.get()) {
			return;
		}

		if (!opening.compareAndSet(false, true)) {
			return;
		}

		factory = new MediaPlayerFactory(VLC_ARGS);
		player = factory.newDirectMediaPlayer(this, this);

		LOG.info("Opening webcam device");

		String[] options = null;

		switch (OsUtils.getOS()) {
			case WIN:
				LOG.debug("Open VLC device {}", getName());
				options = new String[] {
					":dshow-vdev=" + getName(),
					":dshow-size=" + resolution.width + "x" + resolution.height,
					":dshow-adev=none", // no audio device
				};
				break;
			case NIX:
				LOG.debug("Open VLC device {}", getVDevice());
				options = new String[] {
					":v4l2-vdev=" + getVDevice(),
					":v4l2-width=" + resolution.width,
					":v4l2-height=" + resolution.height,
					":v4l2-fps=30",
					":v4l2-adev=none", // no audio device
				};
				break;
			case OSX:
				LOG.debug("Open VLC device {}", getVDevice());
				options = new String[] {
					":qtcapture-vdev=" + getVDevice(),
					":qtcapture-width=" + resolution.width,
					":qtcapture-height=" + resolution.height,
					":qtcapture-adev=none", // no audio device
				};
				break;
		}

		player.startMedia(getMRL(), options);

		// wait for the first image

		long wait = 100; // ms
		int max = 100;
		int count = 0;

		while (imageRef.get() == null) {

			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
				return;
			}

			if (count++ > max) {

				LOG.error("Unable to open in {} ms", wait * max);
				opening.set(false);

				return;
			}
		}

		open.set(true);
		opening.set(false);
	}

	@Override
	public synchronized void close() {

		LOG.info("Closing device {}", this);

		if (open.compareAndSet(true, false)) {
			player.stop();
		}
	}

	@Override
	public synchronized void dispose() {

		if (!disposed.compareAndSet(false, true)) {
			return;
		}

		LOG.debug("Release resources (player={}, factory={})", player, factory);

		player.release();
		factory.release();
	}

	@Override
	public boolean isOpen() {
		return open.get();
	}

	public MediaPlayer getPlayer() {
		return player;
	}

	@Override
	public BufferFormat getBufferFormat(int width, int height) {
		return new RV32BufferFormat(width, height);
	}

	@Override
	public void display(DirectMediaPlayer player, Memory[] buffers, BufferFormat format) {

		LOG.trace("Direct media player display invoked with format {}", format);

		// convert memory to image

		Converter converter = Converter.getConverter(format.getWidth(), format.getHeight());
		BufferedImage image = converter.convert(buffers, format);
		imageRef.set(image);

		// calculate fps

		if (t1 == -1 || t2 == -1) {
			t1 = System.currentTimeMillis();
			t2 = System.currentTimeMillis();
		}

		t1 = t2;
		t2 = System.currentTimeMillis();

		fps.set((4 * fps.get() + 1000 / (t2 - t1 + 1)) / 5);
	}

	@Override
	public double getFPS() {
		return fps.get();
	}
}