package com.github.sarxos.webcam.ds.raspberrypi;

public class ColorUtil {
	private static int R = 0;
	private static int G = 1;
	private static int B = 2;
	/**
	 * Change raw YUV420 color to RGB color. Colours could be 0-255.
	 *
	 * @param yuv
	 *            The array of YUV components to convert
	 * @param rgb
	 *            An array to return the colour values with
	 */
	public static byte[] convertYUVtoRGB(byte[] src, int width, int height) {
		int numOfPixel = width * height;
		int positionOfV = numOfPixel;
		int positionOfU = numOfPixel/4 + numOfPixel;
		byte[] rgb = new byte[numOfPixel*3];

		for(int i=0; i<height; i++){
			int startY = i*width;
			int step = (i/2)*(width/2);
			int startV = positionOfV + step;
			int startU = positionOfU + step;
			for(int j = 0; j < width; j++){
				int Y = startY + j;
				int V = startV + j/2;
				int U = startU + j/2;
				int index = Y*3;		
				rgb[index+B] = (byte)((src[Y]&0xff) + 1.4075 * ((src[V]&0xff)-128));
				rgb[index+G] = (byte)((src[Y]&0xff) - 0.3455 * ((src[U]&0xff)-128) - 0.7169*((src[V]&0xff)-128));
				rgb[index+R] = (byte)((src[Y]&0xff) + 1.779 * ((src[U]&0xff)-128));
			}
		}
		
		return rgb;
	}
	
	
}
