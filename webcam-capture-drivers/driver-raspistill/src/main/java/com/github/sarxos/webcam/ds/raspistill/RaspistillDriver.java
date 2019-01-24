package com.github.sarxos.webcam.ds.raspistill;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;

/**
 * 
 * ClassName: RaspistillDriver <br/> 
 * Function: driver of raspistill, launch raspistill process then communicate with it. this driver is special for raspberrypi <br/> 
 * date: Jan 23, 2019 9:57:03 AM <br/> 
 * 
 * @author maoanapex88@163.com (alexmao86)
 */
public class RaspistillDriver implements WebcamDriver{
	private final static Logger LOGGER=LoggerFactory.getLogger(RaspistillDriver.class);
	private final Options options;
	private Map<String, String> arguments=new LinkedHashMap<>();
	private volatile boolean getDeviceCalled;
	/**
	 * 
	 * Creates a new instance of RaspistillDriver. 
	 * step 1: load defaults.properties arguments
	 * step 2: try to search current class loader's class path if raspistill.properties exits
	 * step 3: search system properties
	 */
	public RaspistillDriver() {
		options=OptionsBuilder.create();
		
		InputStream input=RaspistillDriver.class.getResourceAsStream("defaults.properties");
		mergeArguments(input);
		
		input=Thread.currentThread().getContextClassLoader().getResourceAsStream("/raspistill.properties");
		if(input!=null) {
			mergeArguments(input);
		}
		
		Properties properties=System.getProperties();
		List<String> cmdLine=new ArrayList<>();
		for(Entry<Object, Object> entry:properties.entrySet()) {
			if(!entry.getKey().toString().startsWith("raspistill.")) {
				continue;
			}
			cmdLine.add(entry.getKey().toString());
			cmdLine.add(entry.getValue().toString());
		}
		if(!cmdLine.isEmpty()) {
			String[] cmdArray=new String[cmdLine.size()];
			cmdLine.toArray(cmdArray);
			CommandLineParser parser = new PosixParser();
			CommandLine cmd;
			try {
				cmd = parser.parse( options, cmdArray);
				Option[] opts=cmd.getOptions();
				for(Option o:opts) {
					arguments.put(o.getLongOpt(), o.getArgs()==0?"":o.getValue());
				}
			} catch (ParseException e) {
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("bad raspistill arguments in system properties");
				}
				e.printStackTrace();
			}
		}
	}
	
	private void mergeArguments(InputStream input) {
		try {
			List<String> lines=IOUtils.readLines(input);
			String[] arguArray=new String[lines.size()];
			lines.toArray(arguArray);
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse( options, arguArray);
			Option[] opts=cmd.getOptions();
			for(Option o:opts) {
				arguments.put(o.getLongOpt(), o.getArgs()==0?"":o.getValue());
			}
			input.close();
		} catch (IOException | ParseException e) {
			LOGGER.debug("internal error, please report bug to github issue page");
			e.printStackTrace();
		}
	}
	/**
	 * <a href="https://www.petervis.com/Raspberry_PI/Raspberry_Pi_CSI/raspberry-pi-csi-interface-connector-pinout.html">Raspberrypi camera connector</a>
	 * @see com.github.sarxos.webcam.WebcamDriver#getDevices()
	 */
	@Override
	public List<WebcamDevice> getDevices() {
		synchronized(this) {
			List<String> stdout=CommanderUtil.execute("uname -a");
			if(stdout.isEmpty()||!stdout.get(0).contains("Linux Raspberrypi")) {
				LOGGER.warn("RaspistillDriver supposed to run on raspberrypi");
			}
			
			stdout=CommanderUtil.execute("raspistill --help");
			if(stdout.isEmpty()||stdout.get(0).toLowerCase().contains("command not found")) {
				throw new UnsupportedOperationException("raspistill is not found, please run apt-get install raspistill. this driver supposed to run on raspberrypi");
			}
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("now raspberrypi only support one camera connector with dual camera, so just retrun camera 0");
			}
			
			List<WebcamDevice> devices=new ArrayList<>(1);
			//TODO check hardware if hardware is one dual camera module. if dual camera return two devices
			WebcamDevice device=new RaspistillDevice(0, new LinkedHashMap<>(arguments));
			devices.add(device);
			getDeviceCalled=true;
			return devices;
		}
	}
	
	@Override
	public boolean isThreadSafe() {
		return false;
	}
	
	public RaspistillDriver width(int width) {
		if(getDeviceCalled) {
			throw new UnsupportedOperationException("can not change proerty after device already discoveried");
		}
		arguments.put("width", width+"");
		return this;
	}
	public RaspistillDriver height(int height) {
		if(getDeviceCalled) {
			throw new UnsupportedOperationException("can not change proerty after device already discoveried");
		}
		arguments.put("height", height+"");
		return this;
	}
	public RaspistillDriver quality(int quality) {
		if(getDeviceCalled) {
			throw new UnsupportedOperationException("can not change proerty after device already discoveried");
		}
		arguments.put("quality", quality+"");
		return this;
	}
	//.....more options
}
