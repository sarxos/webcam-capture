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
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.ImageSupplier;
import com.github.sarxos.webcam.WebcamResolution;


/**
 * This example demonstrate how to implement exchange mechanism which will make
 * {@link Webcam#getImageBytes()} to run in parallel without causing FPS drop.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ParallelGetImageBytesExample {

	private static class ByteBufferExchanger extends Exchanger<ByteBuffer> implements AutoCloseable {

		private final AsyncWebcamBuffer owner;

		public ByteBufferExchanger(final AsyncWebcamBuffer owner) {
			this.owner = owner;
		}

		/**
		 * Await for new {@link ByteBuffer} to be ready.
		 */
		public void await() {
			awaitAndGet();
		}

		/**
		 * Await for new {@link ByteBuffer} to be available and return it.
		 *
		 * @return The {@link ByteBuffer}
		 */
		public ByteBuffer awaitAndGet() {
			try {
				return exchange(null);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}

		/**
		 * To be used only from {@link AsyncWebcamBuffer}. Please do not invoke this method from the
		 * other classes.
		 *
		 * @param bb the {@link ByteBuffer} to exchange
		 */
		public void ready(ByteBuffer bb) {
			try {
				exchange(bb, 500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | TimeoutException e) {
				// do nothing, frame is dropped
			}
		}

		@Override
		public void close() {
			owner.dispose(this);
		}
	}

	private static class AsyncWebcamBuffer extends Thread {

		private final Webcam webcam;
		private AtomicReference<ByteBuffer> buffer = new AtomicReference<ByteBuffer>();
		private Set<ByteBufferExchanger> exchangers = Collections.synchronizedSet(new LinkedHashSet<ByteBufferExchanger>());
		private final int length;

		public AsyncWebcamBuffer(Webcam webcam) {
			this.webcam = webcam;
			this.length = getLength(webcam.getViewSize());
			this.setDaemon(true);
			this.start();
		}

		public int getLength(Dimension size) {
			return size.width * size.height * 3;
		}

		public int length() {
			return length;
		}

		public Webcam getWebcam() {
			return webcam;
		}

		@Override
		public void run() {
			while (webcam.isOpen()) {

				// get buffer from webcam (this is direct byte buffer located in off-heap memory)

				final ByteBuffer bb = webcam.getImageBytes();
				bb.rewind();

				buffer.set(bb);

				// notify all exchangers

				for (ByteBufferExchanger exchanger : exchangers) {
					exchanger.ready(bb);
				}
			}
		}

		/**
		 * Be careful when using this reference! It's non synchronized so you have to take special
		 * care to synchronize and maintain position in buffer to avoid
		 * {@link BufferUnderflowException}.
		 *
		 * @return Non synchronized {@link ByteBuffer}
		 */
		public ByteBuffer getByteBuffer() {
			return buffer.get();
		}

		/**
		 * @return New {@link ByteBufferExchanger}
		 */
		public ByteBufferExchanger exchanger() {
			final ByteBufferExchanger exchanger = new ByteBufferExchanger(this);
			exchangers.add(exchanger);
			return exchanger;
		}

		public void dispose(ByteBufferExchanger exchanger) {
			exchangers.remove(exchanger);
		}

		/**
		 * Rewrite {@link ByteBuffer} data to the provided byte[] array.
		 *
		 * @param bytes the byte[] array to rewrite {@link ByteBuffer} into
		 */
		public void read(byte[] bytes) {
			final ByteBuffer buffer = getByteBuffer();
			// all operations on buffer need to be synchronized
			synchronized (buffer) {
				buffer.rewind();
				buffer.get(bytes);
				buffer.rewind();
			}
		}

		/**
		 * Rewrite {@link ByteBuffer} to newly created byte[] array and return it.
		 *
		 * @return Newly created byte[] array with data from {@link ByteBuffer}
		 */
		public byte[] read() {
			final byte[] bytes = new byte[length];
			final ByteBuffer buffer = getByteBuffer();
			// all operations on buffer need to be synchronized
			synchronized (buffer) {
				buffer.rewind();
				buffer.get(bytes);
				buffer.rewind();
			}
			return bytes;
		}

		public boolean isReady() {
			return buffer.get() != null;
		}
	}

	private static class WebcamPanelImageSupplier implements ImageSupplier {

		private final int[] imageOffset = new int[] { 0 };
		private final int[] bandOffsets = new int[] { 0, 1, 2 };
		private final int[] bits = { 8, 8, 8 };
		private final int dataType = DataBuffer.TYPE_BYTE;
		private final Dimension size;
		private final AsyncWebcamBuffer buffer;
		private final ComponentSampleModel sampleModel;
		private final ColorSpace colorSpace;
		private final ComponentColorModel colorModel;

		public WebcamPanelImageSupplier(AsyncWebcamBuffer buffer) {
			this.buffer = buffer;
			this.size = buffer.getWebcam().getViewSize();
			this.sampleModel = new ComponentSampleModel(dataType, size.width, size.height, 3, size.width * 3, bandOffsets);
			this.colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
			this.colorModel = new ComponentColorModel(colorSpace, bits, false, false, Transparency.OPAQUE, dataType);
		}

		@Override
		public BufferedImage get() {

			while (!buffer.isReady()) {
				return null;
			}

			final byte[] bytes = new byte[size.width * size.height * 3];
			final byte[][] data = new byte[][] { bytes };

			buffer.read(bytes);

			final DataBufferByte dataBuffer = new DataBufferByte(data, bytes.length, imageOffset);
			final WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
			final BufferedImage image = new BufferedImage(colorModel, raster, false, null);

			return image;
		}
	}

	public static void main(String[] args) throws InterruptedException {

		final Dimension size = WebcamResolution.VGA.getSize();

		final Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(size);
		webcam.open();

		final AsyncWebcamBuffer buffer = new AsyncWebcamBuffer(webcam);
		final ImageSupplier supplier = new WebcamPanelImageSupplier(buffer);

		final WebcamPanel panel = new WebcamPanel(webcam, size, true, supplier);
		panel.setFPSDisplayed(true);
		panel.setDisplayDebugInfo(true);
		panel.setImageSizeDisplayed(true);
		panel.setMirrored(true);

		final JFrame window = new JFrame("Test webcam panel");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);

		// this thread will get underlying ByteBuffer and perform synchronized op to
		// get rewrite it into bytes[] array

		final Thread t1 = new Thread() {

			@Override
			public void run() {

				// make sure to close exchanger because you will end up with memory leak

				try (final ByteBufferExchanger exchanger = buffer.exchanger()) {

					while (webcam.isOpen()) {

						long t1 = System.currentTimeMillis();
						final ByteBuffer bb = exchanger.awaitAndGet();
						long t2 = System.currentTimeMillis();

						System.out.println(getName() + " : " + 1000 / (t2 - t1 + 1));

						final byte[] bytes = new byte[buffer.length];

						// make sure to synchronize or you will end up

						synchronized (bb) {
							bb.rewind();
							bb.get(bytes);
							bb.rewind();
						}

						// do processing on bytes[] array
					}
				}
			}
		};
		t1.start();

		// this thread will await for underlying ByteBuffer to be ready and perform
		// synchronized op to get rewrite it into new bytes[] array

		final Thread t2 = new Thread() {

			@Override
			public void run() {

				try (final ByteBufferExchanger exchanger = buffer.exchanger()) {
					while (webcam.isOpen()) {

						long t1 = System.currentTimeMillis();
						exchanger.await();
						long t2 = System.currentTimeMillis();

						System.out.println(getName() + " : " + 1000 / (t2 - t1 + 1));

						final byte[] bytes = buffer.read();

						// do processing on bytes[] array
					}
				}
			}
		};
		t2.start();

		// this thread will await for underlying ByteBuffer to be ready and perform
		// synchronized op to get rewrite it into pre-created bytes[] array

		final Thread t3 = new Thread() {

			@Override
			public void run() {

				try (final ByteBufferExchanger exchanger = buffer.exchanger()) {

					final byte[] bytes = new byte[buffer.length()];

					while (webcam.isOpen()) {

						long t1 = System.currentTimeMillis();
						exchanger.await();
						long t2 = System.currentTimeMillis();

						System.out.println(getName() + " : " + 1000 / (t2 - t1 + 1));

						buffer.read(bytes);

						// do processing on bytes[] array
					}
				}
			}
		};
		t3.start();
	}
}
