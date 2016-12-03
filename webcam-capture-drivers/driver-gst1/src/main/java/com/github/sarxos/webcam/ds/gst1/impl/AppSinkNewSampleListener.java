package com.github.sarxos.webcam.ds.gst1.impl;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicReference;

import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.FlowReturn;
import org.freedesktop.gstreamer.Sample;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.elements.AppSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.ds.gst1.Gst1Device;


/**
 * This class is a sample {@link AppSink} listener which is invoked when a new buffer is ready.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class AppSinkNewSampleListener implements AppSink.NEW_SAMPLE {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AppSinkNewSampleListener.class);

	private final Gst1Device device;

	public AppSinkNewSampleListener(Gst1Device device) {
		this.device = device;
	}

	public void rgbFrame(boolean isPrerollFrame, int width, int height, IntBuffer rgb) {

		LOG.debug("RGB frame ({}x{}), preroll is {}", width, height, isPrerollFrame);

		final AtomicReference<BufferedImage> ref = device.getRef();
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		rgb.get(pixels, 0, width * height);

		ref.set(image);
	}

	@Override
	public FlowReturn newSample(AppSink elem) {

		LOG.debug("New sample ready in {}", elem);

		final Sample sample = elem.pullSample();
		final Structure capsStruct = sample.getCaps().getStructure(0);
		final int w = capsStruct.getInteger("width");
		final int h = capsStruct.getInteger("height");
		final Buffer buffer = sample.getBuffer();
		final ByteBuffer bb = buffer.map(false);

		if (bb != null) {
			rgbFrame(false, w, h, bb.asIntBuffer());
			buffer.unmap();
		}

		sample.dispose();

		return FlowReturn.OK;
	}
}