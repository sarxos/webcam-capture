package com.github.sarxos.webcam.ds.raspistill;

import java.io.ByteArrayOutputStream;

/**
 * added getBytes. the purpose of this class is get buf directly by getBytes
 * method rather than toByteArray method causing Arrays.copy again.
 * 
 * @author maoanapex88@163.com (alexmao86)
 *
 */
public class AccessableByteArrayOutputStream extends ByteArrayOutputStream {

	public AccessableByteArrayOutputStream() {
		super();
	}

	public AccessableByteArrayOutputStream(int size) {
		super(size);
	}

	/**
	 * access buf array directly, I DO NOT want to copy memory again and again. this
	 * is image processing!
	 * 
	 * @return
	 */
	public byte[] getBytes() {
		return this.buf;
	}
}
