package com.github.sarxos.webcam.ds.raspberrypi;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/*
 * Copyright (c) 2008-2010, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * 2010-05-15: void256
 * Removed LWJGL name dependencies and instead added TextureFormat enum so that
 * it's now really independent.
 */
public class PNGDecoder {
	public enum TextureFormat {
		ALPHA, LUMINANCE, RGB, RGBA, ABGR
	}

	private static final byte[] SIGNATURE = { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };

	private static final int IHDR = 0x49484452;
	private static final int PLTE = 0x504C5445;
	private static final int tRNS = 0x74524E53;
	private static final int IDAT = 0x49444154;
	private static final int IEND = 0x49454E44;

	public static final byte COLOR_GREYSCALE = 0;
	public static final byte COLOR_TRUECOLOR = 2;
	public static final byte COLOR_INDEXED = 3;
	public static final byte COLOR_GREYALPHA = 4;
	public static final byte COLOR_TRUEALPHA = 6;
	
	//************************default decode image settings********************
	private static final int DATA_TYPE = DataBuffer.TYPE_BYTE;
	private static final ColorSpace COLOR_SPACE = ColorSpace.getInstance(ColorSpace.CS_sRGB);
	private static int[] OFFSET = new int[] { 0 };
	private static final int[] BITS = { 8, 8, 8 };
	private static int[] BAND_OFFSETS = new int[] { 0, 1, 2 };
	//************************************************************************

	private final InputStream input;
	//private final CRC32 crc;
	private final byte[] buffer;

	private int chunkLength;
	private int chunkType;
	private int chunkRemaining;

	private int width;
	private int height;
	private int bitdepth;
	private int colorType;
	private int bytesPerPixel;
	private byte[] palette;
	private byte[] paletteA;
	private byte[] transPixel;

	//decode buffer willed used
	private byte[] curLine;
	private byte[] prevLine;
	private ComponentColorModel cmodel;
	private ComponentSampleModel smodel;
	
	/**
	 * 
	 * Creates a new instance of PNGDecoder. 
	 * 
	 * @param input png stream
	 * @throws IOException
	 */
	public PNGDecoder(InputStream input) throws IOException {
		this.input = input;
		//this.crc = new CRC32();
		this.buffer = new byte[4096];

		readMetadata(input);
		
		curLine = new byte[width * bytesPerPixel + 1];
		prevLine = new byte[width * bytesPerPixel + 1];
		cmodel = new ComponentColorModel(COLOR_SPACE, BITS, false, false, Transparency.OPAQUE, DATA_TYPE);
		smodel = new ComponentSampleModel(DATA_TYPE, width, height, 3, width * 3, BAND_OFFSETS);
	}

	private void readMetadata(InputStream input) throws IOException {
		/*int read = */input.read(buffer, 0, SIGNATURE.length);//just skip sign
		/* no check
		if (read != SIGNATURE.length || !checkSignatur(buffer)) {
			throw new IOException("Not a valid PNG file");
		}
		 */
		openChunk(IHDR);
		readIHDR();
		closeChunk();

		searchIDAT: for (;;) {
			openChunk();
			switch (chunkType) {
			case IDAT:
				break searchIDAT;
			case PLTE:
				readPLTE();
				break;
			case tRNS:
				readtRNS();
				break;
			}
			closeChunk();
		}

		if (colorType == COLOR_INDEXED && palette == null) {
			throw new IOException("Missing PLTE chunk");
		}
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public boolean hasAlpha() {
		return colorType == COLOR_TRUEALPHA || paletteA != null || transPixel != null;
	}

	public boolean isRGB() {
		return colorType == COLOR_TRUEALPHA || colorType == COLOR_TRUECOLOR || colorType == COLOR_INDEXED;
	}
	public static void main(String args[]) throws Exception {
		File f = new File("src/etc/resources/1.png");
		InputStream in = new FileInputStream(f);
		PNGDecoder decoder = new PNGDecoder(in);
		decoder.decode();
	}
	/**
	 * read just one png file
	 * @return
	 * @throws IOException 
	 */
	public final BufferedImage decode() throws IOException {
		byte[] bytes = new byte[width * height * 3];//must new each time!
		byte[][] data = new byte[][] { bytes };
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		decode(buffer, TextureFormat.RGB);
		DataBufferByte dbuf = new DataBufferByte(data, bytes.length, OFFSET);
		WritableRaster raster = Raster.createWritableRaster(smodel, dbuf, null);
		
		BufferedImage bi = new BufferedImage(cmodel, raster, false, null);
		bi.flush();
		return bi;
	}
	/**
	 * read png for png stream
	 * @return
	 * @throws IOException 
	 */
	public BufferedImage nextFrame() throws IOException {
		BufferedImage next=decode();
		input.skip(16);// each time of decode, there will be 16 bytes should be skipped
		readMetadata(input);
		return next;
	}
	public Iterator<BufferedImage> iterator(){
		return new Iterator<BufferedImage>() {
			@Override
			public BufferedImage next() {
				BufferedImage next=null;
				try {
					next = decode();
					input.skip(16);
					readMetadata(input);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return next;
			}
			
			@Override
			public boolean hasNext() {
				try {
					return input.available()>0;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		};
	}
	/**
	 * decode image data to buffer, mapping to awt type byte.
	 * if rgb, r,g,b,r,g,b...,r,g,b,...
	 * if abgr, a,b,g,r....
	 * if rgba, r,g,b,a....
	 * 
	 * @param buffer
	 * @param stride
	 * @param fmt
	 * @throws IOException 
	 */
	private final void decode(ByteBuffer buffer, TextureFormat fmt) throws IOException {
		final Inflater inflater = new Inflater();
		try {
			for (int y = 0; y < height; y++) {
				readChunkUnzip(inflater, curLine, 0, curLine.length);
				unfilter(curLine, prevLine);
				
				switch (colorType) {
				case COLOR_TRUECOLOR:
					switch (fmt) {
					case ABGR:
						copyRGBtoABGR(buffer, curLine);
						break;
					case RGBA:
						copyRGBtoRGBA(buffer, curLine);
						break;
					case RGB:
						copy(buffer, curLine);
						break;
					default:
						throw new UnsupportedOperationException("Unsupported format for this image");
					}
					break;
				case COLOR_TRUEALPHA:
					switch (fmt) {
					case ABGR:
						copyRGBAtoABGR(buffer, curLine);
						break;
					case RGBA:
						copy(buffer, curLine);
						break;
					case RGB:
						copyRGBAtoRGB(buffer, curLine);
						break;
					default:
						throw new UnsupportedOperationException("Unsupported format for this image");
					}
					break;
				case COLOR_GREYSCALE:
					switch (fmt) {
					case LUMINANCE:
					case ALPHA:
						copy(buffer, curLine);
						break;
					default:
						throw new UnsupportedOperationException("Unsupported format for this image");
					}
					break;
				case COLOR_INDEXED:
					switch (fmt) {
					case ABGR:
						copyPALtoABGR(buffer, curLine);
						break;
					case RGBA:
						copyPALtoRGBA(buffer, curLine);
						break;
					default:
						throw new UnsupportedOperationException("Unsupported format for this image");
					}
					break;
				default:
					throw new UnsupportedOperationException("Not yet implemented");
				}

				byte[] tmp = curLine;
				curLine = prevLine;
				prevLine = tmp;
			}
		} finally {
			inflater.end();
		}
	}

	private void copy(ByteBuffer buffer, byte[] curLine) {
		buffer.put(curLine, 1, curLine.length - 1);
	}

	private void copyRGBtoABGR(ByteBuffer buffer, byte[] curLine) {
		if (transPixel != null) {
			byte tr = transPixel[1];
			byte tg = transPixel[3];
			byte tb = transPixel[5];
			for (int i = 1, n = curLine.length; i < n; i += 3) {
				byte r = curLine[i];
				byte g = curLine[i + 1];
				byte b = curLine[i + 2];
				byte a = (byte) 0xFF;
				if (r == tr && g == tg && b == tb) {
					a = 0;
				}
				buffer.put(a).put(b).put(g).put(r);
			}
		} else {
			for (int i = 1, n = curLine.length; i < n; i += 3) {
				buffer.put((byte) 0xFF).put(curLine[i + 2]).put(curLine[i + 1]).put(curLine[i]);
			}
		}
	}

	private void copyRGBtoRGBA(ByteBuffer buffer, byte[] curLine) {
		if (transPixel != null) {
			byte tr = transPixel[1];
			byte tg = transPixel[3];
			byte tb = transPixel[5];
			for (int i = 1, n = curLine.length; i < n; i += 3) {
				byte r = curLine[i];
				byte g = curLine[i + 1];
				byte b = curLine[i + 2];
				byte a = (byte) 0xFF;
				if (r == tr && g == tg && b == tb) {
					a = 0;
				}
				buffer.put(r).put(g).put(b).put(a);
			}
		} else {
			for (int i = 1, n = curLine.length; i < n; i += 3) {
				buffer.put(curLine[i]).put(curLine[i + 1]).put(curLine[i + 2]).put((byte) 0xFF);
			}
		}
	}

	private void copyRGBAtoABGR(ByteBuffer buffer, byte[] curLine) {
		for (int i = 1, n = curLine.length; i < n; i += 4) {
			buffer.put(curLine[i + 3]).put(curLine[i + 2]).put(curLine[i + 1]).put(curLine[i]);
		}
	}

	private void copyRGBAtoRGB(ByteBuffer buffer, byte[] curLine) {
		for (int i = 1, n = curLine.length; i < n; i += 4) {
			buffer.put(curLine[i]).put(curLine[i + 1]).put(curLine[i + 2]);
		}
	}

	private void copyPALtoABGR(ByteBuffer buffer, byte[] curLine) {
		if (paletteA != null) {
			for (int i = 1, n = curLine.length; i < n; i += 1) {
				int idx = curLine[i] & 255;
				byte r = palette[idx * 3 + 0];
				byte g = palette[idx * 3 + 1];
				byte b = palette[idx * 3 + 2];
				byte a = paletteA[idx];
				buffer.put(a).put(b).put(g).put(r);
			}
		} else {
			for (int i = 1, n = curLine.length; i < n; i += 1) {
				int idx = curLine[i] & 255;
				byte r = palette[idx * 3 + 0];
				byte g = palette[idx * 3 + 1];
				byte b = palette[idx * 3 + 2];
				byte a = (byte) 0xFF;
				buffer.put(a).put(b).put(g).put(r);
			}
		}
	}

	private void copyPALtoRGBA(ByteBuffer buffer, byte[] curLine) {
		if (paletteA != null) {
			for (int i = 1, n = curLine.length; i < n; i += 1) {
				int idx = curLine[i] & 255;
				byte r = palette[idx * 3 + 0];
				byte g = palette[idx * 3 + 1];
				byte b = palette[idx * 3 + 2];
				byte a = paletteA[idx];
				buffer.put(r).put(g).put(b).put(a);
			}
		} else {
			for (int i = 1, n = curLine.length; i < n; i += 1) {
				int idx = curLine[i] & 255;
				byte r = palette[idx * 3 + 0];
				byte g = palette[idx * 3 + 1];
				byte b = palette[idx * 3 + 2];
				byte a = (byte) 0xFF;
				buffer.put(r).put(g).put(b).put(a);
			}
		}
	}

	private void unfilter(byte[] curLine, byte[] prevLine) throws IOException {
		switch (curLine[0]) {
		case 0: // none
			break;
		case 1:
			unfilterSub(curLine);
			break;
		case 2:
			unfilterUp(curLine, prevLine);
			break;
		case 3:
			unfilterAverage(curLine, prevLine);
			break;
		case 4:
			unfilterPaeth(curLine, prevLine);
			break;
		default:
			throw new IOException("invalide filter type in scanline: " + curLine[0]);
		}
	}

	private void unfilterSub(byte[] curLine) {
		final int bpp = this.bytesPerPixel;
		final int lineSize = width * bpp;

		for (int i = bpp + 1; i <= lineSize; ++i) {
			curLine[i] += curLine[i - bpp];
		}
	}

	private void unfilterUp(byte[] curLine, byte[] prevLine) {
		final int bpp = this.bytesPerPixel;
		final int lineSize = width * bpp;

		for (int i = 1; i <= lineSize; ++i) {
			curLine[i] += prevLine[i];
		}
	}

	private void unfilterAverage(byte[] curLine, byte[] prevLine) {
		final int bpp = this.bytesPerPixel;
		final int lineSize = width * bpp;

		int i;
		for (i = 1; i <= bpp; ++i) {
			curLine[i] += (byte) ((prevLine[i] & 0xFF) >>> 1);
		}
		for (; i <= lineSize; ++i) {
			curLine[i] += (byte) (((prevLine[i] & 0xFF) + (curLine[i - bpp] & 0xFF)) >>> 1);
		}
	}

	private void unfilterPaeth(byte[] curLine, byte[] prevLine) {
		final int bpp = this.bytesPerPixel;
		final int lineSize = width * bpp;

		int i;
		for (i = 1; i <= bpp; ++i) {
			curLine[i] += prevLine[i];
		}
		for (; i <= lineSize; ++i) {
			int a = curLine[i - bpp] & 255;
			int b = prevLine[i] & 255;
			int c = prevLine[i - bpp] & 255;
			int p = a + b - c;
			int pa = p - a;
			if (pa < 0)
				pa = -pa;
			int pb = p - b;
			if (pb < 0)
				pb = -pb;
			int pc = p - c;
			if (pc < 0)
				pc = -pc;
			if (pa <= pb && pa <= pc)
				c = a;
			else if (pb <= pc)
				c = b;
			curLine[i] += (byte) c;
		}
	}

	private void readIHDR() throws IOException {
		checkChunkLength(13);
		readChunk(buffer, 0, 13);
		width = readInt(buffer, 0);
		height = readInt(buffer, 4);
		bitdepth = buffer[8] & 255;
		colorType = buffer[9] & 255;

		if (bitdepth != 8) {
			throw new IOException("Unsupported bit depth: " + bitdepth);
		}

		switch (colorType) {
		case COLOR_GREYSCALE:
			bytesPerPixel = 1;
			break;
		case COLOR_TRUECOLOR:
			bytesPerPixel = 3;
			break;
		case COLOR_TRUEALPHA:
			bytesPerPixel = 4;
			break;
		case COLOR_INDEXED:
			bytesPerPixel = 1;
			break;
		default:
			throw new IOException("unsupported color format");
		}

		if (buffer[10] != 0) {
			throw new IOException("unsupported compression method");
		}
		if (buffer[11] != 0) {
			throw new IOException("unsupported filtering method");
		}
		if (buffer[12] != 0) {
			throw new IOException("unsupported interlace method");
		}
	}

	private void readPLTE() throws IOException {
		int paletteEntries = chunkLength / 3;
		if (paletteEntries < 1 || paletteEntries > 256 || (chunkLength % 3) != 0) {
			throw new IOException("PLTE chunk has wrong length");
		}
		palette = new byte[paletteEntries * 3];
		readChunk(palette, 0, palette.length);
	}

	private void readtRNS() throws IOException {
		switch (colorType) {
		case COLOR_GREYSCALE:
			checkChunkLength(2);
			transPixel = new byte[2];
			readChunk(transPixel, 0, 2);
			break;
		case COLOR_TRUECOLOR:
			checkChunkLength(6);
			transPixel = new byte[6];
			readChunk(transPixel, 0, 6);
			break;
		case COLOR_INDEXED:
			if (palette == null) {
				throw new IOException("tRNS chunk without PLTE chunk");
			}
			paletteA = new byte[palette.length / 3];
			Arrays.fill(paletteA, (byte) 0xFF);
			readChunk(paletteA, 0, paletteA.length);
			break;
		default:
			// just ignore it
		}
	}

	private void closeChunk() throws IOException {
		if (chunkRemaining > 0) {
			// just skip the rest and the CRC
			skip(chunkRemaining + 4);
		} else {
			readFully(buffer, 0, 4);//read crc
			/*int expectedCrc = */readInt(buffer, 0);
			/*
			int computedCrc = (int) crc.getValue();
			if (computedCrc != expectedCrc) {
				throw new IOException("Invalid CRC");
			}
			*/
		}
		chunkRemaining = 0;
		chunkLength = 0;
		chunkType = 0;
	}

	private void openChunk() throws IOException {
		readFully(buffer, 0, 8);
		chunkLength = readInt(buffer, 0);
		chunkType = readInt(buffer, 4);
		chunkRemaining = chunkLength;
		//crc.reset();
		//crc.update(buffer, 4, 4); // only chunkType
	}

	private void openChunk(int expected) throws IOException {
		openChunk();
		if (chunkType != expected) {
			throw new IOException("Expected chunk: " + Integer.toHexString(expected));
		}
	}

	private void checkChunkLength(int expected) throws IOException {
		if (chunkLength != expected) {
			throw new IOException("Chunk has wrong size");
		}
	}

	private int readChunk(byte[] buffer, int offset, int length) throws IOException {
		if (length > chunkRemaining) {
			length = chunkRemaining;
		}
		readFully(buffer, offset, length);
		//crc.update(buffer, offset, length);
		chunkRemaining -= length;
		return length;
	}

	private void refillInflater(Inflater inflater) throws IOException {
		while (chunkRemaining == 0) {
			closeChunk();
			openChunk(IDAT);
		}
		int read = readChunk(buffer, 0, buffer.length);
		inflater.setInput(buffer, 0, read);
	}

	private void readChunkUnzip(Inflater inflater, byte[] buffer, int offset, int length) throws IOException {
		assert (buffer != this.buffer);
		try {
			do {
				int read = inflater.inflate(buffer, offset, length);
				if (read <= 0) {
					if (inflater.finished()) {
						throw new EOFException();
					}
					if (inflater.needsInput()) {
						refillInflater(inflater);
					} else {
						throw new IOException("Can't inflate " + length + " bytes");
					}
				} else {
					offset += read;
					length -= read;
				}
			} while (length > 0);
		} catch (DataFormatException ex) {
			throw (IOException) (new IOException("inflate error").initCause(ex));
		}
	}

	private void readFully(byte[] buffer, int offset, int length) throws IOException {
		do {
			int read = input.read(buffer, offset, length);
			if (read < 0) {
				throw new EOFException();
			}
			offset += read;
			length -= read;
		} while (length > 0);
	}

	private int readInt(byte[] buffer, int offset) {
		return ((buffer[offset]) << 24) | ((buffer[offset + 1] & 255) << 16) | ((buffer[offset + 2] & 255) << 8)
				| ((buffer[offset + 3] & 255));
	}

	private void skip(long amount) throws IOException {
		while (amount > 0) {
			long skipped = input.skip(amount);
			if (skipped < 0) {
				throw new EOFException();
			}
			amount -= skipped;
		}
	}
	/*
	private static boolean checkSignatur(byte[] buffer) {
		for (int i = 0; i < SIGNATURE.length; i++) {
			if (buffer[i] != SIGNATURE[i]) {
				return false;
			}
		}
		return true;
	}
	*/
}
