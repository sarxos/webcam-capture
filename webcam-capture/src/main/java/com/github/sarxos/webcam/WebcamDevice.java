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
		 * Read the underlying image memory buffer. This method will return new
		 * reference to pre-allocated off-heap memory where image bytes are
		 * stored. The size of this buffer is image width * height * 3 bytes.<br>
		 * <br>
		 * 
		 * <b>NOTE!</b> Do <b>not</b> use this buffer to set bytes value. It
		 * should be used only for read purpose!
		 * 
		 * @return Bytes buffer
		 */
		ByteBuffer getImageBytes();

		/**
		 * Copy the underlying image memory into the target buffer passed as the
		 * argument.The remaining capacity of the target buffer needs to be at
		 * least image width * height * 3 bytes.
		 * 
		 * @param target the buffer to which image data should be copied
		 */
		void getImageBytes(ByteBuffer target);

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
