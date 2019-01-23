package com.github.sarxos.webcam.ds.raspistill;

import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OptionsBuilder {
	private final static Logger LOGGER=LoggerFactory.getLogger(OptionsBuilder.class);
	private OptionsBuilder(){
		
	}
	/**
	 * load supported options from command output. then modify some options manually
	 */
	public static Options create() {
		Options options=new Options();
		List<String> lines=CommanderUtil.execute("raspistill --help");
		for(String line : lines) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(line);
			}
			if(!line.startsWith("-")) {
				continue;
			}
			int indexOfDesc=line.indexOf(":");
			if(indexOfDesc==-1) {
				LOGGER.debug(line+" is omitted, raspistill changed help text format?");
				continue;
			}
			String opts[]=line.substring(0, indexOfDesc).trim().split(",");
			String desc=line.substring(indexOfDesc+1).trim();
			options.addOption(new Option(opts[0].trim().substring(1), opts[1].trim().substring(2), true/*will change later*/, desc));
		}
		//no argument parameters
		options.getOption("r").setArgs(0);
		options.getOption("v").setArgs(0);
		options.getOption("d").setArgs(0);
		options.getOption("fp").setArgs(0);
		options.getOption("k").setArgs(0);
		options.getOption("s").setArgs(0);
		options.getOption("g").setArgs(0);
		options.getOption("gc").setArgs(0);
		options.getOption("set").setArgs(0);
		options.getOption("bm").setArgs(0);
		options.getOption("dt").setArgs(0);
		options.getOption("ts").setArgs(0);
		options.getOption("f").setArgs(0);
		options.getOption("n").setArgs(0);
		options.getOption("ISO").setArgs(0);
		options.getOption("vs").setArgs(0);
		options.getOption("hf").setArgs(0);
		options.getOption("vf").setArgs(0);
		options.getOption("st").setArgs(0);
		options.getOption("3d").setArgs(0);
		options.getOption("dec").setArgs(0);
		options.getOption("3dswap").setArgs(0);
		options.getOption("ag").setArgs(0);
		options.getOption("dg").setArgs(0);
		return options;
	}
}
