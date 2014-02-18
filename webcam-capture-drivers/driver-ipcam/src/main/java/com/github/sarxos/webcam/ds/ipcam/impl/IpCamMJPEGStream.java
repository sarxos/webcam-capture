package com.github.sarxos.webcam.ds.ipcam.impl;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;


public class IpCamMJPEGStream extends DataInputStream {

	/**
	 * The first two bytes of every JPEG stream are the Start Of Image (SOI)
	 * marker values FFh D8h.
	 */
	private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };

	/**
	 * All JPEG data streams end with the End Of Image (EOI) marker values FFh
	 * D9h.
	 */
	private final byte[] EOI_MARKER = { (byte) 0xFF, (byte) 0xD9 };

	/**
	 * Name of content length header.
	 */
	private final String CONTENT_LENGTH = "Content-Length";

	/**
	 * Maximum header length.
	 */
	private final static int HEADER_MAX_LENGTH = 100;

	/**
	 * Max frame length (100kB).
	 */
	private final static int FRAME_MAX_LENGTH = 100000 + HEADER_MAX_LENGTH;

	private boolean open = true;

	public IpCamMJPEGStream(InputStream in) {
		super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
	}

	private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
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

	private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
		int end = getEndOfSeqeunce(in, sequence);
		return end < 0 ? -1 : end - sequence.length;
	}

	private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {

		ByteArrayInputStream bais = new ByteArrayInputStream(headerBytes);
		InputStreamReader isr = new InputStreamReader(bais);
		BufferedReader br = new BufferedReader(isr);

		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith(CONTENT_LENGTH)) {
				String[] parts = line.split(":");
				if (parts.length == 2) {
					return Integer.parseInt(parts[1].trim());
				}
			}
		}

		return 0;
	}

	public BufferedImage readFrame() throws IOException {

		if (!open) {
			return null;
		}

		byte[] header = null;
		byte[] frame = null;

		mark(FRAME_MAX_LENGTH);

		int n = getStartOfSequence(this, SOI_MARKER);

		reset();

		header = new byte[n];

		readFully(header);

		int length = -1;
		try {
			length = parseContentLength(header);
		} catch (NumberFormatException e) {
			length = getEndOfSeqeunce(this, EOI_MARKER);
		}

		reset();

		frame = new byte[length];

		skipBytes(n);
		readFully(frame);

		try {
			return ImageIO.read(new ByteArrayInputStream(frame));
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		open = false;
		super.close();
	}

	public boolean isClosed() {
		return !open;
	}
}