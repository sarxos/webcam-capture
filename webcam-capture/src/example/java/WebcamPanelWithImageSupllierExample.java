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
import java.nio.ByteBuffer;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.ImageSupplier;
import com.github.sarxos.webcam.WebcamResolution;


/**
 * This example demonstrates a possibility to provide custom {@link ImageSupplier} which will be
 * used to obtain image displayed on the {@link WebcamPanel}. In this specific example we will
 * implement it in such a way that it won't be using {@link Webcam#getImage()} but
 * {@link Webcam#getImageBytes(ByteBuffer)} instead. This may be required in some cases if you need
 * very fast access to underlying buffer.
 */
public class WebcamPanelWithImageSupllierExample {

	public static void main(String[] args) throws InterruptedException {

		final Dimension size = WebcamResolution.VGA.getSize();

		final Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(size);
		webcam.open();

		final ImageSupplier supplier = new ImageSupplier() {

			private final int[] imageOffset = new int[] { 0 };
			private final int[] bandOffsets = new int[] { 0, 1, 2 };
			private final int[] bits = { 8, 8, 8 };
			private final int dataType = DataBuffer.TYPE_BYTE;
			private final int length = size.width * size.height * 3;
			private final ComponentSampleModel sampleModel = new ComponentSampleModel(dataType, size.width, size.height, 3, size.width * 3, bandOffsets);
			private final ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
			private final ComponentColorModel colorModel = new ComponentColorModel(colorSpace, bits, false, false, Transparency.OPAQUE, dataType);
			private final ByteBuffer buffer = ByteBuffer.allocateDirect(length);

			@Override
			public BufferedImage get() {

				webcam.getImageBytes(buffer);

				final byte[] bytes = new byte[length];
				final byte[][] data = new byte[][] { bytes };

				buffer.get(bytes);
				buffer.clear();

				final DataBufferByte dataBuffer = new DataBufferByte(data, bytes.length, imageOffset);
				final WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
				final BufferedImage image = new BufferedImage(colorModel, raster, false, null);

				return image;
			}
		};

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
	}
}
