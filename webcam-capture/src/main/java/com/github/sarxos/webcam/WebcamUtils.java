package com.github.sarxos.webcam;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.util.ImageUtils;


public class WebcamUtils {

	public static final void capture(Webcam webcam, File file) {
		if (!webcam.isOpen()) {
			webcam.open();
		}
		try {
			ImageIO.write(webcam.getImage(), ImageUtils.FORMAT_JPG, file);
		} catch (IOException e) {
			throw new WebcamException(e);
		}
	}

	public static final void capture(Webcam webcam, File file, String format) {
		if (!webcam.isOpen()) {
			webcam.open();
		}
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

	/**
	 * Capture image as BYteBuffer.
	 *
	 * @param webcam the webcam from which image should be obtained
	 * @param format the file format
	 * @return Byte buffer
	 */
	public static final ByteBuffer getImageByteBuffer(Webcam webcam, String format) {
		return ByteBuffer.wrap(getImageBytes(webcam, format));
	}

	/**
	 * Get resource bundle for specific class.
	 *
	 * @param clazz the class for which resource bundle should be found
	 * @param locale the {@link Locale} object
	 * @return Resource bundle
	 */
	public static final ResourceBundle loadRB(Class<?> clazz, Locale locale) {
		String pkg = WebcamUtils.class.getPackage().getName().replaceAll("\\.", "/");
		return PropertyResourceBundle.getBundle(String.format("%s/i18n/%s", pkg, clazz.getSimpleName()));
	}
}
