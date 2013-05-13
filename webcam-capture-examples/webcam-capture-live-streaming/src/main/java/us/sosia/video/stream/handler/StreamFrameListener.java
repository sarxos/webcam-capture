package us.sosia.video.stream.handler;

import java.awt.image.BufferedImage;

import com.xuggle.xuggler.IAudioSamples;

public interface StreamFrameListener {
	/**
	 * Callback when the image is received from the live stream.
	 * @param image The received and decoded image
	 * */
	public void onFrameReceived(BufferedImage image);
	
	public void onAudioRecieved(IAudioSamples samples);
}
