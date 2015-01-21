

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import com.sun.jna.Memory;


public class VlcjDirectTest {

	private static class TestBufferFormatCallback implements BufferFormatCallback {

		@Override
		public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
			return new RV32BufferFormat(sourceWidth, sourceHeight);
		}
	}

	/**
	 * RGB offsets.
	 */
	private static final int[] BAND_OFFSETS = new int[] { 2, 1, 0 };
	/**
	 * Number of bytes in each pixel.
	 */
	private static final int[] BITS = { 8, 8, 8 };

	/**
	 * Image color space.
	 */
	private static final ColorSpace COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_sRGB);

	/**
	 * Data type used in image.
	 */
	private static final int DATA_TYPE = DataBuffer.TYPE_BYTE;

	private final static String[] VLC_ARGS = {

		// VLC args by Andrew Davison:
		// http://fivedots.coe.psu.ac.th/~ad/jg/nui025/snapsWithoutJMF.pdf

		// no interface
		"--intf", "dummy",

		// no video output
		"--vout", "dummy",

		// no audio decoding
		"--no-audio",

		// do not display title
		"--no-video-title-show",

		// no stats
		"--no-stats",

		// no subtitles
		"--no-sub-autodetect-file",

		// no snapshot previews
		"--no-snapshot-preview",

		// reduce capture lag/latency
		"--live-caching=50",

		// turn off warnings
		"--quiet",
	};

	public static void main(String[] args) throws IOException, InterruptedException {

		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(VLC_ARGS);

		DirectMediaPlayer mediaPlayer = mediaPlayerFactory.newDirectMediaPlayer(
			new TestBufferFormatCallback(),
			new RenderCallback() {

				int i = 0;

				@Override
				public void display(DirectMediaPlayer player, Memory[] buffers, BufferFormat format) {

					final int width = format.getWidth();
					final int height = format.getHeight();

					System.out.println(width + " x " + height);

					ComponentSampleModel smodel = new ComponentSampleModel(
						DataBuffer.TYPE_BYTE,
						width,
						height,
						4, // pixel stride
						width * 4, // scanline stride
						BAND_OFFSETS);

					ComponentColorModel cmodel = new ComponentColorModel(
						COLOR_SPACE, BITS, false, false, Transparency.BITMASK, DATA_TYPE);

					if (buffers.length == 0) {
						System.err.println("No memory elements found!");
						return;
					}

					Memory memory = buffers[0];

					if (memory == null) {
						System.err.println("Null memory!");
						return;
					}

					System.out.println("meme " + memory.size());

					ByteBuffer buffer = memory.getByteBuffer(0, memory.size());

					byte[] bytes = new byte[width * height * 4];
					byte[][] data = new byte[][] { bytes };

					buffer.get(bytes);

					DataBufferByte dbuf = new DataBufferByte(data, bytes.length, new int[] { 0 });
					WritableRaster raster = Raster.createWritableRaster(smodel, dbuf, null);

					BufferedImage bi = new BufferedImage(cmodel, raster, false, null);
					bi.flush();

					try {
						ImageIO.write(bi, "JPG", new File((i++) + "-test.jpg"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					System.out.println("display " + i);
				}
			});

		// Options setup.
		String mrl = "v4l2:///dev/video0"; // Linux

		String[] options = new String[] {
			":v4l-vdev=/dev/video0",
			":v4l-width=640",
			":v4l-height=480",
			":v4l-fps=30",
			":v4l-quality=20",
			":v4l-adev=none", // no audio device
		};

		// Start preocessing.
		mediaPlayer.startMedia(mrl, options);

		Thread.sleep(5000);

		// Stop precessing.
		mediaPlayer.stop();
		mediaPlayer = null;

		System.out.println("Finish!");
	}
}
