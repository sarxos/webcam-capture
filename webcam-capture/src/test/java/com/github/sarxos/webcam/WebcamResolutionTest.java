package com.github.sarxos.webcam;

import org.assertj.core.api.Assertions;
import org.junit.Test;


public class WebcamResolutionTest {

	@Test
	public void test_getSize() {
		Assertions
			.assertThat(WebcamResolution.VGA.getSize())
			.isNotNull();
		Assertions
			.assertThat(WebcamResolution.VGA.getSize().getWidth())
			.isEqualTo(640);
		Assertions
			.assertThat(WebcamResolution.VGA.getSize().getHeight())
			.isEqualTo(480);
	}

	@Test
	public void test_getPixelCount() {
		Assertions
			.assertThat(WebcamResolution.VGA.getPixelsCount())
			.isEqualTo(640 * 480);
	}

	@Test
	public void test_getAspectRatio() {
		Assertions
			.assertThat(WebcamResolution.VGA.getAspectRatio())
			.isNotNull();
		Assertions
			.assertThat(WebcamResolution.VGA.getAspectRatio().getWidth())
			.isEqualTo(4);
		Assertions
			.assertThat(WebcamResolution.VGA.getAspectRatio().getHeight())
			.isEqualTo(3);
	}

	@Test
	public void test_getWidth() {
		Assertions
			.assertThat(WebcamResolution.VGA.getWidth())
			.isEqualTo(640);
	}

	@Test
	public void test_getHeight() {
		Assertions
			.assertThat(WebcamResolution.VGA.getHeight())
			.isEqualTo(480);
	}

	@Test
	public void test_toString() {
		Assertions
			.assertThat(WebcamResolution.VGA.toString())
			.isEqualTo("VGA 640x480 (4:3)");
	}
}
