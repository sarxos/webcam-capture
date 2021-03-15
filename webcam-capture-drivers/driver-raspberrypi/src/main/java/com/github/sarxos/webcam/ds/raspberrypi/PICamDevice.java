package com.github.sarxos.webcam.ds.raspberrypi;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Map;

import com.github.sarxos.webcam.WebcamDevice;

public class PICamDevice implements WebcamDevice, WebcamDevice.Configurable, WebcamDevice.BufferAccess {

	@Override
	public ByteBuffer getImageBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void getImageBytes(ByteBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParameters(Map<String, ?> params) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BufferedImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension getResolution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension[] getResolutions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setResolution(Dimension arg0) {
		// TODO Auto-generated method stub
		
	}

}
