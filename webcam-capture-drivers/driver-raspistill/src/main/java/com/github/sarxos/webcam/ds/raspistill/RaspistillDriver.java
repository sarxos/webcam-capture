package com.github.sarxos.webcam.ds.raspistill;

import java.util.List;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;

/**
 * 
 * ClassName: RaspistillDriver <br/> 
 * Function: driver of raspistil, launch raspistill process then communicate with it. this driver is special for raspberrypi <br/> 
 * date: Jan 23, 2019 9:57:03 AM <br/> 
 * 
 * @author maoanapex88@163.com (alexmao86)
 */
public class RaspistillDriver implements WebcamDriver{
	
	@Override
	public List<WebcamDevice> getDevices() {
		List<String> stdout=CommanderUtil.execute("raspistill --help");
		if(stdout.isEmpty()||stdout.get(0).toLowerCase().contains("command not found")) {
			throw new UnsupportedOperationException("raspistill is not found, please run apt-get install raspistill. this driver supposed to run on raspberrypi");
		}
		
		return null;
	}
	
	@Override
	public boolean isThreadSafe() {
		return false;
	}
}
