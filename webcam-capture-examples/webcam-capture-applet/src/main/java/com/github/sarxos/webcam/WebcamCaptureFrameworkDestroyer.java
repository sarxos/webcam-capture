package com.github.sarxos.webcam;

/**
 * IMPORTANT! This approach should be used only in case of Applets (due to its non standard
 * lifecycle).
 *
 * @author Bartosz Firyn (sarxos)
 */
public class WebcamCaptureFrameworkDestroyer {

	public static final void destroy() {
		Webcam.shutdown();
	}
}
