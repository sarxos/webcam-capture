package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;


/**
 * Webcam device abstraction.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public interface WebcamDevice {

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
