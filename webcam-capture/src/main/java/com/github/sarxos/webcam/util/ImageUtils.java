package com.github.sarxos.webcam.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


public class ImageUtils {

	public static BufferedImage premultiple(BufferedImage src) {
		BufferedImage pre = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = pre.createGraphics();
		g2.drawImage(src, 0, 0, null);
		g2.dispose();
		pre.flush();
		return pre;
	}

	public static BufferedImage unpremultiple(BufferedImage pre) {
		BufferedImage src = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = pre.createGraphics();
		g2.drawImage(src, 0, 0, null);
		g2.dispose();
		src.flush();
		return src;
	}

}
