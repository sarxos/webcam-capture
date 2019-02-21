package com.github.sarxos.webcam.ds.raspberrypi;

public class ColorUtil {
	/**
	 * Change raw YUV420 color to RGB color. Colours could be 0-255.
	 *
	 * @param yuv
	 *            The array of YUV components to convert
	 * @param rgb
	 *            An array to return the colour values with
	 
	public static byte[] convertYUVtoRGB(byte[] yuv, int width, int height) {
		int size=width*height;
		int uOffset=size;
		int vOffset=uOffset+(uOffset>>2);
		
		byte rgb[]=new byte[size<<2];
		int index=0;
		
		for(int y=0;y<height;y++) {
			for(int x=0;x<width;x++) {
				int pos=y*width+x;
				
				float Y=yuv[pos];
				float U = yuv[(y/2) * (width/2) + (x/2) + uOffset];
				float V = yuv[(y/2) * (width/2) + (x/2) + vOffset];
				
				rgb[index++]= (byte)(Y + 1.140f * V);
				rgb[index++]= (byte)(Y - 0.394f * U - 0.581f * V);
				rgb[index++]= (byte)(Y + 2.028f * U);
			}
		}
		return rgb;
	}
	*/
	
}
