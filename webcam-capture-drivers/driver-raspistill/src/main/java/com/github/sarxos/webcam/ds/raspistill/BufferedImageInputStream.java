package com.github.sarxos.webcam.ds.raspistill;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * this decorated inputstream in used to read images from raspistill process
 * outputstream.
 * 
 * @author maoanapex88@163.com (alexmao86)
 *
 */
class BufferedImageInputStream extends InputStream {
	// stdout text when start capture
	private static final String CAPTURE_START = "Starting cap";
	// stdout text when end capture
	private static final String CAPTURE_END = "Fini";

	private final InputStream in;
	private final StringMatcher startMatcher = new StringMatcher(CAPTURE_START);
	private final StringMatcher finishMatcher = new StringMatcher(CAPTURE_END);

	private final int bufferSize;
	private boolean notClosed = true;

	public BufferedImageInputStream(InputStream in, int bufferSize) {
		super();
		this.in = in;
		this.bufferSize = bufferSize;
	}

	@Override
	public int read() throws IOException {
		throw new IOException("open for read image");
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		super.close();
		in.close();
	}

	/**
	 * just read image without any validation, raspistill is running as debug mode.
	 * if not image return null;
	 * 
	 * @return
	 * @throws IOException
	 */
	public BufferedImage readBufferedImage() throws IOException {
		AccessableByteArrayOutputStream imageBytes = new AccessableByteArrayOutputStream(bufferSize);
		// read and match start flag
		int byt = -1;
		while (!startMatcher.isTarget()) {
			byt = in.read();
			startMatcher.push((char) byt);
		}

		// skip to new line char
		while ((byt = in.read()) != 10)
			;

		while (notClosed) {
			byt = in.read();
			finishMatcher.push((char) byt);
			imageBytes.write(byt);
			if (finishMatcher.isTarget()) {
				break;
			}
		}
		byte[] bytes = imageBytes.getBytes();
		imageBytes.close();

		// skip to new line char
		while ((byt = in.read()) != 10)
			;

		startMatcher.clear();
		finishMatcher.clear();

		return ImageIO.read(new ByteArrayInputStream(bytes, 0, bytes.length - CAPTURE_END.length()));
	}
}
