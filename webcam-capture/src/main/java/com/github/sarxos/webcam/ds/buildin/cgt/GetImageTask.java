package com.github.sarxos.webcam.ds.buildin.cgt;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicReference;

import org.bridj.Pointer;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberProcessor;
import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;


public class GetImageTask extends WebcamGrabberTask {

	private AtomicReference<Dimension> dimension = new AtomicReference<Dimension>();
	private AtomicReference<byte[]> bytes = new AtomicReference<byte[]>();

	private WebcamGrabberProcessor processor = null;

	public GetImageTask(WebcamGrabberProcessor processor) {
		this.processor = processor;
	}

	public byte[] getImage(Dimension size) {
		dimension.set(size);
		process(processor);
		return bytes.get();
	}

	@Override
	protected void handle() {
		Pointer<Byte> image = grabber.getImage();
		Dimension size = dimension.get();
		bytes.set(image == null ? null : image.getBytes(size.width * size.height * 3));
	}
}
