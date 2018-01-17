package com.github.sarxos.webcam.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;


/**
 * This class will save {@link BufferedImage} into a byte array and try to compress it a given size.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class AdaptiveSizeWriter {

	private static final float INITIAL_QUALITY = 1f;

	private volatile int size;
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private float quality = 1f; // 1f = 100% quality, at the beginning

	public AdaptiveSizeWriter(int size) {
		this.size = size;
	}

	public byte[] write(final BufferedImage bi) {

		// loop and try to compress until compressed image bytes array is not longer than a given
		// maximum value, reduce quality by 25% in every step

		int m = size;
		int s = 0;
		int i = 0;
		do {
			if ((s = compress(bi, quality)) > m) {
				quality *= 0.75;
				if (i++ >= 20) {
					break;
				}
			}
		} while (s > m);

		return baos.toByteArray();
	}

	/**
	 * Compress {@link BufferedImage} with a given quality into byte array.
	 *
	 * @param bi the {@link BufferedImage} to compres into byte array
	 * @param quality the compressed image quality (1 = 100%, 0.5 = 50%, 0.1 = 10%, etc)
	 * @return The size of compressed data (number of bytes)
	 */
	private int compress(BufferedImage bi, float quality) {

		baos.reset();

		final JPEGImageWriteParam params = new JPEGImageWriteParam(null);
		params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		params.setCompressionQuality(quality);

		try (MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(baos)) {
			final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
			writer.setOutput(mcios);
			writer.write(null, new IIOImage(bi, null, null), params);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		return baos.size();
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		if (this.size != size) {
			this.size = size;
			this.quality = INITIAL_QUALITY;
		}
	}
}