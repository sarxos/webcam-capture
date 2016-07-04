package com.github.sarxos.webcam;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of this interface is responsible for decision whether the difference between two
 * images represents movement or not. Instance may specified as parameter of WebcamMotionDetector
 * constructor, otherwise {@link WebcamMotionDetectorDefaultAlgorithm} is used.
 */
public interface WebcamMotionDetectorAlgorithm {

	/**
	 * WebcamMotionDetector calls this method for each image used as parameter of the method
	 * {@link #detect(BufferedImage, BufferedImage)}. Implementation may transform the original
	 * image and prepare it for comparison of two images. May return the same instance if no there
	 * is no need to transform.
	 * 
	 * @param original image
	 * @return modified image
	 */
	BufferedImage filter(BufferedImage original);

	/**
	 * Detects motion by comparison of the two specified images content.
	 * {@link #filter(BufferedImage)} method was called for both specified images.
	 * 
	 * @param previous
	 * @param current
	 * @return If the motion was detected returns true, otherwise returns false
	 */
	boolean detect(BufferedImage previous, BufferedImage current);

	/**
	 * Get motion center of gravity. When no motion is detected this value points to the image
	 * center. May return null before the first movement check.
	 *
	 * @return Center of gravity point
	 */
	Point getCog();

	/**
	 * Get percentage fraction of image covered by motion. 0 means no motion on image and 100 means
	 * full image covered by spontaneous motion.
	 *
	 * @return Return percentage image fraction covered by motion
	 */
	double getArea();

	/**
	 * Set the minimum range between each point detected
	 * 
	 * @param i the range to set
	 */
	void setPointRange(int i);

	/**
	 * Set the max amount of points that can be detected at one time
	 * 
	 * @param i The amount of points that can be detected
	 */
	void setMaxPoints(int i);

	/**
	 * Get the current minimum range between each point
	 * 
	 * @return The current range
	 */
	int getPointRange();

	/**
	 * Get the current max amount of points that can be detected at one time
	 * 
	 * @return
	 */
	int getMaxPoints();

	/**
	 * Returns the currently stored points that have been detected
	 * 
	 * @return The current points
	 */
	ArrayList<Point> getPoints();

	/**
	 * Set list of the rectangular zones where motion detection should be ignored.
	 *
	 * @param bounds the list of rectangles to ignore
	 */
	void setDoNotEngageZones(List<Rectangle> bounds);
}
