package us.sosia.video.stream.handler;

import java.awt.image.BufferedImage;

public interface StreamFrameListener {
	/**
	 * Callback when the image is received from the live stream.
	 * @param image The received and decoded image
	 * */
	void onFrameReceived(BufferedImage image);
}
