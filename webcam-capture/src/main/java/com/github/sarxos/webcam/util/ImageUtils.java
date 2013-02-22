package com.github.sarxos.webcam.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.WebcamException;


public class ImageUtils {

	/**
	 * Graphics Interchange Format.
	 */
	public static final String FORMAT_GIF = "GIF";

	/**
	 * Portable Network Graphic format.
	 */
	public static final String FORMAT_PNG = "PNG";

	/**
	 * Joint Photographic Experts Group format.
	 */
	public static final String FORMAT_JPG = "JPG";

	/**
	 * Bitmap image format.
	 */
	public static final String FORMAT_BMP = "BMP";

	/**
	 * Wireless Application Protocol Bitmap image format.
	 */
	public static final String FORMAT_WBMP = "WBMP";

	public static BufferedImage premultiple(BufferedImage src) {
		BufferedImage pre = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = pre.createGraphics();
		g2.drawImage(src, 0, 0, null);
		g2.dispose();
		pre.flush();
		return pre;
	}

	public static BufferedImage unpremultiple(BufferedImage pre) {
		BufferedImage src = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = pre.createGraphics();
		g2.drawImage(src, 0, 0, null);
		g2.dispose();
		src.flush();
		return src;
	}

	/**
	 * Convert {@link BufferedImage} to byte array.
	 * 
	 * @param image the image to be converted
	 * @param format the output image format
	 * @return New array of bytes
	 */
	public static byte[] toByteArray(BufferedImage image, String format) {

		byte[] bytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, format, baos);
			bytes = baos.toByteArray();
		} catch (IOException e) {
			throw new WebcamException(e);
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				throw new WebcamException(e);
			}
		}

		return bytes;
	}

	public static byte[] toRawByteArray(BufferedImage image) {

		DataBuffer dbuf = image.getRaster().getDataBuffer();

		if (dbuf instanceof DataBufferByte) {

			return ((DataBufferByte) dbuf).getData();

		} else {

			int w = image.getWidth();
			int h = image.getHeight();
			int n = w * h;

			byte[] bytes = new byte[n * 3];

			int i, x, y, rgb;

			for (i = 0; i < n; i++) {

				x = i % w;
				y = i / h;

				rgb = image.getRGB(x, y);

				bytes[i * 3 + 0] = (byte) ((rgb >> 16) & 0xff);
				bytes[i * 3 + 1] = (byte) ((rgb >> 8) & 0xff);
				bytes[i * 3 + 2] = (byte) (rgb & 0xff);
			}

			return bytes;
		}
	}
}
