package com.github.sarxos.webcam;

import java.awt.image.BufferedImage;


public interface WebcamImageTransformer {

	BufferedImage transform(BufferedImage image);

}
