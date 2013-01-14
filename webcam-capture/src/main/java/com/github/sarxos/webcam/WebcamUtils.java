package com.github.sarxos.webcam;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.util.ImageUtils;


public class WebcamUtils {

	public static final void capture(Webcam webcam, File file) {
		try {
			ImageIO.write(webcam.getImage(), ImageUtils.FORMAT_JPG, file);
		} catch (IOException e) {
			throw new WebcamException(e);
		}
	}

	public static final void capture(Webcam webcam, File file, String format) {
		try {
			ImageIO.write(webcam.getImage(), format, file);
		} catch (IOException e) {
			throw new WebcamException(e);
		}
	}

	public static final void capture(Webcam webcam, String filename) {
		if (filename.endsWith(".jpg")) {
			filename = filename + ".jpg";
		}
		capture(webcam, new File(filename));
	}

	public static final void capture(Webcam webcam, String filename, String format) {
		String ext = "." + format.toLowerCase();
		if (!filename.startsWith(ext)) {
			filename = filename + ext;
		}
		capture(webcam, new File(filename), format);
	}

	public static final byte[] getImageBytes(Webcam webcam, String format) {
		return ImageUtils.toByteArray(webcam.getImage(), format);
	}

	public static final ByteBuffer getImageByteBuffer(Webcam webcam, String format) {
		return ByteBuffer.wrap(getImageBytes(webcam, format));
	}

}
