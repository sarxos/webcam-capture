package us.sosia.video.stream.handler;

import java.awt.image.BufferedImage;

public class ImageUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	  /**
	   * Convert a {@link BufferedImage} of any type, to {@link BufferedImage} of a
	   * specified type. If the source image is the same type as the target type,
	   * then original image is returned, otherwise new image of the correct type is
	   * created and the content of the source image is copied into the new image.
	   * 
	   * @param sourceImage
	   *          the image to be converted
	   * @param targetType
	   *          the desired BufferedImage type
	   * 
	   * @return a BufferedImage of the specifed target type.
	   * 
	   * @see BufferedImage
	   */

	  public static BufferedImage convertToType(BufferedImage sourceImage,
	      int targetType)
	  {
	    BufferedImage image;

	    // if the source image is already the target type, return the source image

	    if (sourceImage.getType() == targetType)
	      image = sourceImage;

	    // otherwise create a new image of the target type and draw the new
	    // image

	    else
	    {
	      image = new BufferedImage(sourceImage.getWidth(),
	          sourceImage.getHeight(), targetType);
	      image.getGraphics().drawImage(sourceImage, 0, 0, null);
	    }

	    return image;
	  }
}
