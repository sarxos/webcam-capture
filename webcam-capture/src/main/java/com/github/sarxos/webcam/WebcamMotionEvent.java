package com.github.sarxos.webcam;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EventObject;


/**
 * Webcam detected motion event.
 *
 * @author Bartosz Firyn (SarXos)
 */
public class WebcamMotionEvent extends EventObject {

	private static final long serialVersionUID = -7245768099221999443L;

	private final double strength;
	private final Point cog;
	private final BufferedImage previousImage;
	private final BufferedImage currentImage;

	/**
	 * Create detected motion event.
	 *
	 * @param detector
	 * @param strength
	 * @param cog center of motion gravity
	 */
	public WebcamMotionEvent(WebcamMotionDetector detector, double strength, Point cog) {
		this(detector, null, null, strength, cog);
	}

    /**
     * Create detected motion event.
     *
     * @param detector
     * @param strength
     * @param cog center of motion gravity
     * @param points list of all detected points
     */
    public WebcamMotionEvent(WebcamMotionDetector detector, double strength, Point cog, ArrayList<Point> points) {
        this(detector, null, null, strength, cog, points);
    }

	/**
	 * Create detected motion event.
	 *
	 * @param detector
	 * @param previousImage
	 * @param currentImage
	 * @param strength
	 * @param cog center of motion gravity
	 */
	public WebcamMotionEvent(WebcamMotionDetector detector, BufferedImage previousImage, BufferedImage currentImage, double strength, Point cog) {
		super(detector);
		this.previousImage = previousImage;
		this.currentImage = currentImage;
		this.strength = strength;
		this.cog = cog;
	}

    /**
     * Create detected motion event.
     *
     * @param detector
     * @param previousImage
     * @param currentImage
     * @param strength
     * @param cog center of motion gravity
     * @param points list of all detected points
     */
    public WebcamMotionEvent(WebcamMotionDetector detector, BufferedImage previousImage, BufferedImage currentImage, double strength, Point cog, ArrayList<Point> points) {
        this(detector, previousImage, currentImage, strength, cog);
        this.points = points;
    }

    private ArrayList<Point> points;
    public ArrayList<Point> getPoints(){
        return points;
    }
	
	/**
	 * Get percentage fraction of image covered by motion. 0 is no motion on
	 * image, and 100 is full image covered by motion.
	 *
	 * @return Motion area
	 */
	public double getArea() {
		return strength;
	}

	public Point getCog() {
		return cog;
	}

	public Webcam getWebcam() {
		return ((WebcamMotionDetector) getSource()).getWebcam();
	}

	/**
	 * Returns last image before the motion.
	 * Instance is shared among the listeners, so if you need to change the image, create a copy. 
	 */
	public BufferedImage getPreviousImage() {
		return previousImage;
	}

	/**
	 * Returns image with the motion detected.
	 * Instance is shared among the listeners, so if you need to change the image, create a copy. 
	 */
	public BufferedImage getCurrentImage() {
		return currentImage;
	}

	@Override
	public WebcamMotionDetector getSource() {
		return (WebcamMotionDetector) super.getSource();
	}

}
