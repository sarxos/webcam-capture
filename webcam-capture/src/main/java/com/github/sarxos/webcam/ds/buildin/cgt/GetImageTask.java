package com.github.sarxos.webcam.ds.buildin.cgt;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicReference;

import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;


public class GetImageTask extends WebcamGrabberTask {

	private AtomicReference<Dimension> dimension = new AtomicReference<Dimension>();
	private AtomicReference<byte[]> bytes = new AtomicReference<byte[]>();

	public byte[] getImage(Dimension size) {
		dimension.set(size);
		process();
		return bytes.get();
	}

	@Override
	protected void handle(OpenIMAJGrabber grabber) {
		Pointer<Byte> image = grabber.getImage();
		Dimension size = dimension.get();
		bytes.set(image == null ? null : image.getBytes(size.width * size.height * 3));
	}
}
