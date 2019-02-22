package com.github.sarxos.webcam.ds.raspberrypi;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

public class TestMultipleRGBDecoder extends TestCase {
	private static final int DATA_TYPE = DataBuffer.TYPE_BYTE;
	private static final ColorSpace COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	private static int[] OFFSET = new int[] { 0 };
	private static final int[] BITS = { 8, 8, 8 };
	private static int[] BAND_OFFSETS = new int[] { 0, 1, 2 };

	public void testDecodeRawRgb() throws IOException {
		int width = 320;
		int height = 320;

		InputStream in = new FileInputStream(new File("src/etc/resources/test.raw"));

		ColorModel cmodel = new ComponentColorModel(COLOR_SPACE, BITS, false, false, Transparency.OPAQUE, DATA_TYPE);
		SampleModel smodel = new ComponentSampleModel(DATA_TYPE, width, height, 3, width * 3, BAND_OFFSETS);

		int i = 0;
		while (in.available() > 0) {
			byte[] bytes = new byte[width * height * 3];// must new each time!
			in.read(bytes, 0, bytes.length);
			byte[][] data = new byte[][] { bytes };

			DataBufferByte dbuf = new DataBufferByte(data, bytes.length, OFFSET);
			WritableRaster raster = Raster.createWritableRaster(smodel, dbuf, null);

			BufferedImage bi = new BufferedImage(cmodel, raster, false, null);
			bi.flush();
			assertNotNull(bi);
			// ImageIO.write(bi, "png", new File("test"+(i++)+".png"));
		}

		in.close();

	}
}
