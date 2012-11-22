package com.github.sarxos.webcam.ds.buildin.cgt;

import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicReference;

import com.github.sarxos.webcam.ds.buildin.WebcamGrabberProcessor;
import com.github.sarxos.webcam.ds.buildin.WebcamGrabberTask;


public class GetSizeTask extends WebcamGrabberTask {

	private AtomicReference<Dimension> dimension = new AtomicReference<Dimension>();
	private WebcamGrabberProcessor processor = null;

	public GetSizeTask(WebcamGrabberProcessor processor) {
		this.processor = processor;
	}

	public Dimension getSize() {
		process(processor);
		return dimension.get();
	}

	@Override
	protected void handle() {
		dimension.set(new Dimension(grabber.getWidth(), grabber.getHeight()));
	}
}
