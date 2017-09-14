package com.github.sarxos.webcam.util.jh;

import java.awt.image.BufferedImage;

import org.assertj.core.api.Assertions;
import org.junit.Test;


public class JHBlurFilterTest {

	@Test
	public void test_filterNonPremultiplied() {

		final JHBlurFilter filter = new JHBlurFilter();
		filter.setPremultiplyAlpha(false);

		final BufferedImage bi1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		final BufferedImage bi2 = filter.filter(bi1, null);

		Assertions
			.assertThat(bi1.getWidth())
			.isEqualTo(bi2.getWidth());
		Assertions
			.assertThat(bi1.getHeight())
			.isEqualTo(bi2.getHeight());
		Assertions
			.assertThat(bi1.getType())
			.isEqualTo(bi2.getType());
	}

	@Test
	public void test_filterPremultiplied() {

		final JHBlurFilter filter = new JHBlurFilter();
		filter.setPremultiplyAlpha(true);

		final BufferedImage bi1 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		final BufferedImage bi2 = filter.filter(bi1, null);

		Assertions
			.assertThat(bi1.getWidth())
			.isEqualTo(bi2.getWidth());
		Assertions
			.assertThat(bi1.getHeight())
			.isEqualTo(bi2.getHeight());
		Assertions
			.assertThat(bi1.getType())
			.isEqualTo(bi2.getType());
	}

	@Test
	public void test_setGetPremultiplyAlpha() {

		final JHBlurFilter filter = new JHBlurFilter();

		filter.setPremultiplyAlpha(true);
		Assertions
			.assertThat(filter.getPremultiplyAlpha())
			.isTrue();

		filter.setPremultiplyAlpha(false);
		Assertions
			.assertThat(filter.getPremultiplyAlpha())
			.isFalse();
	}
}
