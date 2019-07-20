package com.github.sarxos.webcam.util.jh;

import static com.github.sarxos.webcam.util.jh.JHFlipFilter.FLIP_180;
import static com.github.sarxos.webcam.util.jh.JHFlipFilter.FLIP_90CCW;
import static com.github.sarxos.webcam.util.jh.JHFlipFilter.FLIP_90CW;

import java.awt.image.BufferedImage;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.github.sarxos.webcam.util.ImageUtils;


public class JHFlipFilterTest {

	final BufferedImage original = ImageUtils.readFromResource("tv-test-pattern.png");

	@Test
	public void test_FLIP_90CW() {
		final JHFlipFilter filter = new JHFlipFilter(FLIP_90CW);
		final BufferedImage rotated = filter.filter(original, null);
		Assertions
			.assertThat(original.getWidth())
			.isEqualTo(rotated.getHeight());
		Assertions
			.assertThat(original.getHeight())
			.isEqualTo(rotated.getWidth());
		Assertions
			.assertThat(filter.toString())
			.isEqualTo("Rotate 90");
	}

	@Test
	public void test_FLIP_90CCW() {
		final JHFlipFilter filter = new JHFlipFilter(FLIP_90CCW);
		final BufferedImage rotated = filter.filter(original, null);
		Assertions
			.assertThat(original.getWidth())
			.isEqualTo(rotated.getHeight());
		Assertions
			.assertThat(original.getHeight())
			.isEqualTo(rotated.getWidth());
		Assertions
			.assertThat(filter.toString())
			.isEqualTo("Rotate -90");
	}

	@Test
	public void test_FLIP_180() {
		final JHFlipFilter filter = new JHFlipFilter(FLIP_180);
		final BufferedImage rotated = filter.filter(original, null);
		Assertions
			.assertThat(original.getWidth())
			.isEqualTo(rotated.getWidth());
		Assertions
			.assertThat(original.getHeight())
			.isEqualTo(rotated.getHeight());
		Assertions
			.assertThat(filter.toString())
			.isEqualTo("Rotate 180");
	}

	@Test
	public void test_default() {
		final JHFlipFilter filter = new JHFlipFilter();
		Assertions
			.assertThat(filter.getOperation())
			.isEqualTo(FLIP_90CW);
	}

	@Test
	public void test_setOperation() {
		final JHFlipFilter filter = new JHFlipFilter();
		filter.setOperation(888);
		Assertions
			.assertThat(filter.getOperation())
			.isEqualTo(888);
		Assertions
			.assertThat(filter.toString())
			.isEqualTo("Flip");
	}
}
