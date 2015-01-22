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

import javax.imageio.ImageIO;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import com.sun.jna.Memory;


public class VlcjDirectTest {

	private static class DirectBufferFormatCallback implements BufferFormatCallback {

		@Override
		public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
			return new RV32BufferFormat(sourceWidth, sourceHeight);
		}
	}

	private final static String[] VLC_ARGS = {
		"--intf", "dummy",
		"--vout", "dummy",
		"--no-audio",
		"--no-video-title-show",
		"--no-stats",
		"--no-sub-autodetect-file",
		"--no-snapshot-preview",
		"--live-caching=50",
		"--quiet",
	};

	private static BufferedImage convert(Memory[] buffers, BufferFormat format) {

		if (buffers.length == 0) {
			throw new RuntimeException("No memory elements found!");
		}

		final Memory memory = buffers[0];
		if (memory == null) {
			throw new RuntimeException("Null memory!");
		}

		final int width = format.getWidth();
		final int height = format.getHeight();
		final int dataType = DataBuffer.TYPE_BYTE;
		final int pixelStride = 4;
		final int scanlineStride = width * pixelStride;
		final int[] bgrBandOffsets = new int[] { 2, 1, 0 };
		final int[] bits = { 8, 8, 8 };
		final int[] offsets = new int[] { 0 };
		final int transparency = Transparency.OPAQUE;

		final byte[] bytes = new byte[scanlineStride * height];
		final byte[][] data = new byte[][] { bytes };

		memory
			.getByteBuffer(0, memory.size())
			.get(bytes);

		ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ComponentSampleModel sampleModel = new ComponentSampleModel(dataType, width, height, pixelStride, scanlineStride, bgrBandOffsets);
		ComponentColorModel colorModel = new ComponentColorModel(colorSpace, bits, false, false, transparency, dataType);

		DataBufferByte dataBuffer = new DataBufferByte(data, bytes.length, offsets);
		WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
		BufferedImage image = new BufferedImage(colorModel, raster, false, null);

		image.flush();

		return image;
	}

	private static MediaPlayerFactory factory = new MediaPlayerFactory(VLC_ARGS);
	private static DirectMediaPlayer player;

	public static void main(String[] args) throws IOException, InterruptedException {

		player = factory.newDirectMediaPlayer(
			new DirectBufferFormatCallback(),
			new RenderCallback() {

				int i = 0;

				@Override
				public void display(DirectMediaPlayer player, Memory[] buffers, BufferFormat format) {

					if (i++ < 10) {

						BufferedImage bi = convert(buffers, format);
						try {
							ImageIO.write(bi, "JPG", new File(System.currentTimeMillis() + "-test.jpg"));
						} catch (IOException e) {
							e.printStackTrace();
						}

						System.out.println("write " + i);
					}
				}
			});

		String device = "/dev/video0";
		String mrl = "v4l2://" + device;
		String[] options = new String[] {
			":v4l2-vdev=" + device,
			":v4l2-width=320",
			":v4l2-height=240",
			":v4l2-fps=30",
			":v4l2-adev=none",
		};

		player.startMedia(mrl, options);

		Thread.sleep(1000);

		player.stop();
		player.release();
		factory.release();
	}
}
