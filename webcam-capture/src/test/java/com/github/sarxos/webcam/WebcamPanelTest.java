package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.ds.test.DummyDriver;


public class WebcamPanelTest {

	private final int width = 256;
	private final int height = 345;

	@Test
	public void test_size() throws InterruptedException {

		Webcam.setDriver(new DummyDriver());

		final Webcam w = Webcam.getDefault();
		final WebcamPanel p = new WebcamPanel(w);

		w.open();
		p.repaint();

		final BufferedImage bi = w.getImage();
		final Dimension d = p.getPreferredSize();

		Assertions
			.assertThat(d.getWidth())
			.isEqualTo(bi.getWidth());
		Assertions
			.assertThat(d.getHeight())
			.isEqualTo(bi.getHeight());

		p.stop();
		w.close();
	}

	@Test
	public void test_sizeSpecified() throws InterruptedException {

		Webcam.setDriver(new DummyDriver());

		final Webcam w = Webcam.getDefault();
		final WebcamPanel p = new WebcamPanel(w, new Dimension(width, height), false);

		w.open();
		p.repaint();

		final Dimension d = p.getPreferredSize();

		Assertions
			.assertThat(d.getWidth())
			.isEqualTo(width);
		Assertions
			.assertThat(d.getHeight())
			.isEqualTo(height);

		p.stop();
		w.close();
	}

	@Test
	public void test_modeFill() throws InterruptedException {

		Webcam.setDriver(new DummyDriver());

		final Webcam w = Webcam.getDefault();
		w.open();

		final WebcamPanel p = new WebcamPanel(w, new Dimension(width, height), false);
		p.setDrawMode(DrawMode.FILL);
		p.start();

		Assertions
			.assertThat(p.getDrawMode())
			.isEqualTo(DrawMode.FILL);

		ScreenImage.createImage(p);

		Thread.sleep(100);

		p.stop();
		w.close();
	}

	@Test
	public void test_modeFit() throws InterruptedException {

		Webcam.setDriver(new DummyDriver());

		final Webcam w = Webcam.getDefault();
		w.open();

		final WebcamPanel p = new WebcamPanel(w, new Dimension(width, height), false);
		p.setDrawMode(DrawMode.FIT);
		p.start();

		Assertions
			.assertThat(p.getDrawMode())
			.isEqualTo(DrawMode.FIT);

		ScreenImage.createImage(p);

		Thread.sleep(100);

		p.stop();
		w.close();
	}

	@Test
	public void test_modeNone() throws InterruptedException {

		Webcam.setDriver(new DummyDriver());

		final Webcam w = Webcam.getDefault();
		w.open();

		final WebcamPanel p = new WebcamPanel(w, new Dimension(width, height), false);
		p.setDrawMode(DrawMode.NONE);
		p.start();

		Assertions
			.assertThat(p.getDrawMode())
			.isEqualTo(DrawMode.NONE);

		ScreenImage.createImage(p);

		Thread.sleep(100);

		p.stop();
		w.close();
	}
}
