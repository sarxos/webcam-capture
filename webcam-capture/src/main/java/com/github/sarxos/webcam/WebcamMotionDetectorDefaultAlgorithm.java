package com.github.sarxos.webcam;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sarxos.webcam.util.jh.JHBlurFilter;
import com.github.sarxos.webcam.util.jh.JHGrayFilter;


/**
 * Default motion detector algorithm.
 */
/**
 * @author kevin
 *
 */
public class WebcamMotionDetectorDefaultAlgorithm implements WebcamMotionDetectorAlgorithm {

	/**
	 * Default pixel difference intensity threshold (set to 25).
	 */
	public static final int DEFAULT_PIXEL_THREASHOLD = 25;

	/**
	 * Default percentage image area fraction threshold (set to 0.2%).
	 */
	public static final double DEFAULT_AREA_THREASHOLD = 0.2;

	/**
	 * Default max percentage image area fraction threshold (set to 100%).
	 */
	public static final double DEFAULT_AREA_THREASHOLD_MAX = 100;

	/**
	 * Pixel intensity threshold (0 - 255).
	 */
	private volatile int pixelThreshold = DEFAULT_PIXEL_THREASHOLD;

	/**
	 * Percentage image area fraction threshold (0 - 100).
	 */
	private volatile double areaThreshold = DEFAULT_AREA_THREASHOLD;

	/**
	 * Maximum pixel change percentage threshold (0 - 100).
	 */
	private volatile double areaThresholdMax = DEFAULT_AREA_THREASHOLD_MAX;

	/**
	 * Motion strength (0 = no motion, 100 = full image covered by motion).
	 */
	private double area = 0;

	/**
	 * Center of motion gravity.
	 */
	private Point cog = null;

	/**
	 * Blur filter instance.
	 */
	private final JHBlurFilter blur = new JHBlurFilter(6, 6, 1);

	/**
	 * Gray filter instance.
	 */
	private final JHGrayFilter gray = new JHGrayFilter();

	private List<Rectangle> doNotEnganeZones = Collections.emptyList();

	/**
	 * Creates default motion detector algorithm with default pixel and area thresholds.
	 * 
	 * @see #DEFAULT_PIXEL_THREASHOLD
	 * @see #DEFAULT_AREA_THREASHOLD
	 */
	public WebcamMotionDetectorDefaultAlgorithm() {
		this(DEFAULT_PIXEL_THREASHOLD, DEFAULT_AREA_THREASHOLD);
	}

	/**
	 * Creates default motion detector algorithm.
	 * 
	 * @param pixelThreshold intensity threshold (0 - 255)
	 * @param areaThreshold percentage threshold of image covered by motion
	 */
	public WebcamMotionDetectorDefaultAlgorithm(int pixelThreshold, double areaThreshold) {
		setPixelThreshold(pixelThreshold);
		setAreaThreshold(areaThreshold);
	}

	@Override
	public BufferedImage filter(BufferedImage original) {
		BufferedImage modified = blur.filter(original, null);
		modified = gray.filter(modified, null);
		return modified;
	}

	@Override
	public boolean detect(BufferedImage previousModified, BufferedImage currentModified) {

		points.clear();
		thresholds.clear();

		int p = 0;

		int cogX = 0;
		int cogY = 0;

		final int w = currentModified.getWidth();
		final int h = currentModified.getHeight();

		int j = 0;

		if (previousModified != null) {
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {

					// ignore point if it's in one of do-not-engage zones, simply skip motion
					// detection for corresponding pixel

					if (isInDoNotEngageZone(x, y)) {
						continue;
					}

					int cpx = currentModified.getRGB(x, y);
					int ppx = previousModified.getRGB(x, y);
					int pid = combinePixels(cpx, ppx) & 0x000000ff;

					if (pid >= pixelThreshold) {
						Point pp = new Point(x, y);
						boolean keep = j < maxPoints;

						if (keep) {
							for (Point g : points) {
								if (g.x != pp.x || g.y != pp.y) {
									if (pp.distance(g) <= range) {
										keep = false;
										break;
									}
								}
							}
						}

						if (keep) {
							points.add(new Point(x, y));
							j += 1;
						}

						cogX += x;
						cogY += y;
						p += 1;
						thresholds.add(pid);
					}
				}
			}
		}

		area = p * 100d / (w * h);

		if (area >= areaThreshold && area <= areaThresholdMax) {
			cog = new Point(cogX / p, cogY / p);
			return true;
		} else {
			cog = new Point(w / 2, h / 2);
			return false;
		}
	}

	/**
	 * Return true if point identified by x and y coordinates is in one of the do-not-engage zones.
	 * Return false otherwise.
	 *
	 * @param x the x coordinate of a point
	 * @param y the y coordinate of a point
	 * @return True if point is in one of do-not-engage zones, false otherwise
	 */
	private boolean isInDoNotEngageZone(final int x, final int y) {
		for (final Rectangle zone : doNotEnganeZones) {
			if (zone.contains(x, y)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Point getCog() {
		return this.cog;
	}

	@Override
	public double getArea() {
		return this.area;
	}

	/**
	 * Set pixel intensity difference threshold above which pixel is classified as "moved". Minimum
	 * value is 0 and maximum is 255. Default value is 10. This value is equal for all RGB
	 * components difference.
	 *
	 * @param threshold the pixel intensity difference threshold
	 * @see #DEFAULT_PIXEL_THREASHOLD
	 */
	public void setPixelThreshold(int threshold) {
		if (threshold < 0) {
			throw new IllegalArgumentException("Pixel intensity threshold cannot be negative!");
		}
		if (threshold > 255) {
			throw new IllegalArgumentException("Pixel intensity threshold cannot be higher than 255!");
		}
		this.pixelThreshold = threshold;
	}

	/**
	 * Set percentage fraction of detected motion area threshold above which it is classified as
	 * "moved". Minimum value for this is 0 and maximum is 100, which corresponds to full image
	 * covered by spontaneous motion.
	 *
	 * @param threshold the percentage fraction of image area
	 * @see #DEFAULT_AREA_THREASHOLD
	 */
	public void setAreaThreshold(double threshold) {
		if (threshold < 0) {
			throw new IllegalArgumentException("Area fraction threshold cannot be negative!");
		}
		if (threshold > 100) {
			throw new IllegalArgumentException("Area fraction threshold cannot be higher than 100!");
		}
		this.areaThreshold = threshold;
	}

	/**
	 * Set max percentage fraction of detected motion area threshold, below which it is classified
	 * as "moved". The max is optionally used to help filter false positives caused by sudden
	 * exposure changes. Minimum value for this is 0 and maximum is 100, which corresponds to full
	 * image covered by spontaneous motion.
	 *
	 * @param threshold the percentage fraction of image area
	 * @see #DEFAULT_AREA_THREASHOLD_MAX
	 */
	public void setMaxAreaThreshold(double threshold) {
		if (threshold < 0) {
			throw new IllegalArgumentException("Area fraction threshold cannot be negative!");
		}
		if (threshold > 100) {
			throw new IllegalArgumentException("Area fraction threshold cannot be higher than 100!");
		}
		this.areaThresholdMax = threshold;
	}

	private static int combinePixels(int rgb1, int rgb2) {

		// first ARGB

		int a1 = (rgb1 >> 24) & 0xff;
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = rgb1 & 0xff;

		// second ARGB

		int a2 = (rgb2 >> 24) & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = rgb2 & 0xff;

		r1 = clamp(Math.abs(r1 - r2));
		g1 = clamp(Math.abs(g1 - g2));
		b1 = clamp(Math.abs(b1 - b2));

		// in case if alpha is enabled (translucent image)

		if (a1 != 0xff) {
			a1 = a1 * 0xff / 255;
			int a3 = (255 - a1) * a2 / 255;
			r1 = clamp((r1 * a1 + r2 * a3) / 255);
			g1 = clamp((g1 * a1 + g2 * a3) / 255);
			b1 = clamp((b1 * a1 + b2 * a3) / 255);
			a1 = clamp(a1 + a3);
		}

		return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
	}

	/**
	 * Clamp a value to the range 0..255
	 */
	private static int clamp(int c) {
		if (c < 0) {
			return 0;
		}
		if (c > 255) {
			return 255;
		}
		return c;
	}

	/**
	 * ArrayList to store the points for a detected motion
	 */
	ArrayList<Point> points = new ArrayList<Point>();

	/**
	 * Array list to store thresholds for debugging
	 */
	ArrayList<Integer> thresholds = new ArrayList<Integer>();

	public ArrayList<Integer> getThresholds() {
		return this.thresholds;
	}

	/**
	 * The default minimum range between each point where motion has been detected
	 */
	public static final int DEFAULT_RANGE = 50;

	/**
	 * The default for the max amount of points that can be detected at one time
	 */
	public static final int DEFAULT_MAX_POINTS = 100;

	/**
	 * The current minimum range between points
	 */
	private int range = DEFAULT_RANGE;

	/**
	 * The current max amount of points
	 */
	private int maxPoints = DEFAULT_MAX_POINTS;

	/**
	 * Set the minimum range between each point detected
	 * 
	 * @param i the range to set
	 */
	@Override
	public void setPointRange(int i) {
		range = i;
	}

	/**
	 * Get the current minimum range between each point
	 * 
	 * @return The current range
	 */
	@Override
	public int getPointRange() {
		return range;
	}

	/**
	 * Set the max amount of points that can be detected at one time
	 * 
	 * @param i The amount of points that can be detected
	 */
	@Override
	public void setMaxPoints(int i) {
		maxPoints = i;
	}

	/**
	 * Get the current max amount of points that can be detected at one time
	 * 
	 * @return
	 */
	@Override
	public int getMaxPoints() {
		return maxPoints;
	}

	/**
	 * Returns the currently stored points that have been detected
	 * 
	 * @return The current points
	 */
	@Override
	public ArrayList<Point> getPoints() {
		return points;
	}

	@Override
	public void setDoNotEngageZones(List<Rectangle> doNotEngageZones) {
		this.doNotEnganeZones = doNotEngageZones;
	}
}
