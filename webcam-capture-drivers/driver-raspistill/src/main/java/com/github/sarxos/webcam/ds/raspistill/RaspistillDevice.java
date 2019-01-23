package com.github.sarxos.webcam.ds.raspistill;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.WebcamDevice;

/**
 * 
 * ClassName: RaspistillDevice <br/> 
 * Function: device descriptor of raspistil <br/> 
 * date: Jan 23, 2019 9:57:03 AM <br/> 
 * 
 * @author maoanapex88@163.com (alexmao86)
 */
public class RaspistillDevice implements WebcamDevice{
	/** 
	 * TODO description the overrided function and changes. 
	 * @see com.github.sarxos.webcam.WebcamDevice#close() 
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	/** 
	 * TODO description the overrided function and changes. 
	 * @see com.github.sarxos.webcam.WebcamDevice#dispose() 
	 */  
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public BufferedImage getImage() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	/** 
	 * TODO description the overrided function and changes. 
	 * @see com.github.sarxos.webcam.WebcamDevice#getResolution() 
	 */  
	@Override
	public Dimension getResolution() {
		// TODO Auto-generated method stub
		return null;
	}

	/** 
	 * TODO description the overrided function and changes. 
	 * @see com.github.sarxos.webcam.WebcamDevice#getResolutions() 
	 */  
	@Override
	public Dimension[] getResolutions() {
		// TODO Auto-generated method stub
		return null;
	}

	/** 
	 * TODO description the overrided function and changes. 
	 * @see com.github.sarxos.webcam.WebcamDevice#isOpen() 
	 */  
	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	/** 
	 * TODO description the overrided function and changes. 
	 * @see com.github.sarxos.webcam.WebcamDevice#open() 
	 */  
	@Override
	public void open() {
		// TODO Auto-generated method stub
		
	}

	/** 
	 * TODO description the overrided function and changes. 
	 * @see com.github.sarxos.webcam.WebcamDevice#setResolution(java.awt.Dimension) 
	 */  
	@Override
	public void setResolution(Dimension dimension) {
		// TODO Auto-generated method stub
		
	}
	
}
