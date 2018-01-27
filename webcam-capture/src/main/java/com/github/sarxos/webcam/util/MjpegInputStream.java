package com.github.sarxos.webcam.util;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is {@link InputStream} with ability to read MJPEG frames as {@link BufferedImage}.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class MjpegInputStream extends DataInputStream {

	private static final Logger LOG = LoggerFactory.getLogger(MjpegInputStream.class);

	/**
	 * The first two bytes of every JPEG frame are the Start Of Image (SOI) marker values FFh D8h.
	 */
	private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };

	/**
	 * All JPEG data streams end with the End Of Image (EOI) marker values FFh D9h.
	 */
	private final byte[] EOI_MARKER = { (byte) 0xFF, (byte) 0xD9 };

	/**
	 * Name of content length header.
	 */
	private final String CONTENT_LENGTH = "Content-Length".toLowerCase();

	/**
	 * Maximum header length.
	 */
	private final static int HEADER_MAX_LENGTH = 100;

	/**
	 * Max frame length (100kB).
	 */
	private final static int FRAME_MAX_LENGTH = 100000 + HEADER_MAX_LENGTH;

	/**
	 * Is stream open?
	 */
	private boolean open = true;

	public MjpegInputStream(final InputStream in) {
		super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
	}

	private int getEndOfSeqeunce(final DataInputStream in, final byte[] sequence) throws IOException {
		int s = 0;
		byte c;
		for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
			c = (byte) in.readUnsignedByte();
			if (c == sequence[s]) {
				s++;
				if (s == sequence.length) {
					return i + 1;
				}
			} else {
				s = 0;
			}
		}
		return -1;
	}

	private int getStartOfSequence(final DataInputStream in, final byte[] sequence) throws IOException {
		int end = getEndOfSeqeunce(in, sequence);
		return end < 0 ? -1 : end - sequence.length;
	}

	private int parseContentLength(final byte[] headerBytes) throws IOException, NumberFormatException {

		try (
			final ByteArrayInputStream bais = new ByteArrayInputStream(headerBytes);
			final InputStreamReader isr = new InputStreamReader(bais);
			final BufferedReader br = new BufferedReader(isr)) {

			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.toLowerCase().startsWith(CONTENT_LENGTH)) {
					final String[] parts = line.split(":");
					if (parts.length == 2) {
						return Integer.parseInt(parts[1].trim());
					}
				}
			}
		}

		return 0;
	}

	/**
	 * Read single MJPEG frame (JPEG image) from stram.
	 *
	 * @return JPEG image as {@link BufferedImage} or null
	 * @throws IOException when there is a problem in reading from stream
	 */
	public BufferedImage readFrame() throws IOException {

		if (!open) {
			return null;
		}

		mark(FRAME_MAX_LENGTH);

		int n = getStartOfSequence(this, SOI_MARKER);

		reset();

		final byte[] header = new byte[n];

		readFully(header);

		int length = -1;
		try {
			length = parseContentLength(header);
		} catch (NumberFormatException e) {
			length = getEndOfSeqeunce(this, EOI_MARKER);
		}

		if (length == 0) {
			LOG.error("Invalid MJPEG stream, EOI (0xFF,0xD9) not found!");
		}

		reset();

		final byte[] frame = new byte[length];

		skipBytes(n);

		readFully(frame);

		try (final ByteArrayInputStream bais = new ByteArrayInputStream(frame)) {
			return ImageIO.read(bais);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			this.open = false;
		}
	}

	public boolean isClosed() {
		return !open;
	}
}