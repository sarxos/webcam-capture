package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;


/**
 * Webcam device abstraction.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public interface WebcamDevice {

	/**
	 * This interface should be implemented by all webcam devices supporting
	 * possibility to access raw bytes or direct bytes buffer from native webcam
	 * device.
	 * 
	 * @author Bartosz Firyn (SarXos)
	 */
	public static interface BufferAccess {

		/**
		 * Get image in form of raw bytes. Do <b>not</b> use this buffer to set
		 * bytes value, it should be used only for read purpose!
		 * 
		 * @return Bytes buffer
		 */
		ByteBuffer getImageBytes();

	}

	public static interface FPSSource {

		/**
		 * Get current device FPS.
		 * 
		 * @return FPS
		 */
		double getFPS();

	}

	/**
	 * Get device name.
	 * 
	 * @return Device name
	 */
	String getName();

	/**
	 * Get the list of all possible image resolutions.
	 * 
	 * @return Possible resolutions
	 */
	Dimension[] getResolutions();

	/**
	 * Get currently set image size.
	 * 
	 * @return The size which is currently set
	 */
	Dimension getResolution();

	/**
	 * Set new expected image size.
	 * 
	 * @param size the size to be set
	 */
	void setResolution(Dimension size);

	/**
	 * Fetch image from underlying camera.
	 * 
	 * @return Image
	 */
	BufferedImage getImage();

	/**
	 * Open device, it can be closed any time.
	 */
	void open();

	/**
	 * Close device, however it can be open again.
	 */
	void close();

	/**
	 * Dispose device. After device is disposed it cannot be open again.
	 */
	void dispose();

	/**
	 * Is webcam device open?
	 * 
	 * @return True if webcam device is open, false otherwise
	 */
	boolean isOpen();
}
