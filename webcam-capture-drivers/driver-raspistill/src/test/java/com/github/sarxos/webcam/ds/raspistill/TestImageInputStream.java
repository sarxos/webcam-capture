package com.github.sarxos.webcam.ds.raspistill;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

public class TestImageInputStream extends TestCase {

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
		testType("png");
	}
	
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
