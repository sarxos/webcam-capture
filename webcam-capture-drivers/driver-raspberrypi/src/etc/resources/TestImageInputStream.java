package com.github.sarxos.webcam.ds.raspistill;

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
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.github.sarxos.webcam.ds.raspistill.PNGDecoder.TextureFormat;

import junit.framework.TestCase;

public class TestImageInputStream extends TestCase {
	private static final ColorSpace COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mockMultiImageForStream("png");
		mockMultiImageForStream("bmp");
		mockMultiImageForStream("gif");
		mockMultiImageForStream("jpg");
	}
	
	public void testPNG() throws Exception {
		//testType("jpg");
		//testType("bmp");
		//testType("gif");
		//testType("png");
	}
	/**/
	public void testPNGDecoder() throws Exception {
		
		File f=new File("src/etc/resources/1.png");
		InputStream in = new FileInputStream(f);
		PNGDecoder decoder=new PNGDecoder(in);
		
		BufferedImage bi = decoder.decode();
		bi.flush();
		assertNotNull(bi);
		in.close();
		ImageIO.write(bi, "png", new File("out.png"));
		/*
		ByteBuffer buffer=ByteBuffer.allocate(decoder.getHeight()*decoder.getWidth()*3);
		decoder.decode(buffer, 0, TextureFormat.RGB);
		
		byte[] bytes = buffer.array();
		
		assertEquals(210*111*3, bytes.length);
		byte[][] data = new byte[][] { bytes };
		
		final int[] OFFSET = new int[] { 0 };
		final int[] BAND_OFFSETS = new int[] { 0, 1, 2 };
		final int DATA_TYPE = DataBuffer.TYPE_BYTE;
		final int[] BITS = { 8, 8, 8 };
		SampleModel smodel = new ComponentSampleModel(DATA_TYPE, decoder.getWidth(), decoder.getHeight(), 3, decoder.getWidth() * 3, BAND_OFFSETS);
		
		DataBufferByte dbuf = new DataBufferByte(data, bytes.length, OFFSET);
		WritableRaster raster = Raster.createWritableRaster(smodel, dbuf, null);
		
		ColorModel cmodel = new ComponentColorModel(COLOR_SPACE, BITS, false, false, Transparency.OPAQUE, DATA_TYPE);
		
		BufferedImage bi = new BufferedImage(cmodel, raster, false, null);
		bi.flush();
		assertNotNull(bi);
		in.close();
		ImageIO.write(bi, "png", new File("out.png"));
		*/
	}
	
	/* BMP is too slow, testing one my thinkstation with GForce GPU, one picture costs 1.5s!
	 * using BMP decoder from image4j project https://github.com/imcdonagh/image4j
	public void testBMPDecoder() throws IOException {
		File f=new File("src/etc/resources/kc.bmp");
		InputStream in = new FileInputStream(f);
		byte[] bytes=IOUtils.toByteArray(in);
		in.close();
		
		long acc=0;
		for(int i=0;i<100;i++) {
			System.out.println(i);
			long start=System.currentTimeMillis();
			ByteArrayInputStream bin=new ByteArrayInputStream(bytes);
			BufferedImage bi = BMPDecoder.read(bin);
			bin.close();
			long end=System.currentTimeMillis();
			long onetime=end-start;
			acc+=onetime;
			System.out.printf("onetime=%d", onetime);
			//ImageIO.write(bi, "bmp", new File("out.bmp"));
		}
		System.out.printf("total=%d, avg=%d", acc, acc/1000);
	}
	*/
	private void testType(String type) throws Exception {
		File mutilImage=new File("src/etc/resources/"+type+".chunk");
		
		InputStream in = new FileInputStream(mutilImage);
		byte[] bytes=FileUtils.readFileToByteArray(new File("src/etc/resources/kc."+type));
		byte[] chunkBytes=FileUtils.readFileToByteArray(mutilImage);
		assertEquals(bytes.length, chunkBytes.length/10);
		
		for (int i = 0; i < 10; i++) {
			try {
				PNGImagesInputStream iin=new PNGImagesInputStream(in);
				BufferedImage img = ImageIO.read(iin);
				System.out.println(img);
				assertNotNull(img);
				System.out.println("-------");
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				System.out.println(in.available());
			}
		}
		
		in.close();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteChunk("png");
		deleteChunk("jpg");
		deleteChunk("bmp");
		deleteChunk("gif");
	}
	private static void mockMultiImageForStream(String type) throws IOException {
		File mutilImage = deleteChunk(type);
		byte[] bytes=FileUtils.readFileToByteArray(new File("src/etc/resources/kc."+type));
		for(int i=0;i<10;i++) {
			FileUtils.writeByteArrayToFile(mutilImage, bytes, true);
		}
	}

	private static File deleteChunk(String type) {
		File mutilImage=new File("src/etc/resources/"+type+".chunk");
		if(mutilImage.exists()) {
			mutilImage.delete();
		}
		return mutilImage;
	}
}
