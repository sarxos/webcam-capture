package com.github.sarxos.webcam;

import java.awt.Dimension;
import java.awt.image.BufferedImage;


public interface WebcamDevice {

	public String getName();

	public Dimension[] getSizes();

	public Dimension getSize();

	public void setSize(Dimension size);

	public BufferedImage getImage();

	public void open();

	public void close();

}
