package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ClassName: OptionsBuilder <br/>
 * this class is used to parse raspistill help text to apache cli options,
 * because raspistill is keep updating supported command options, so avoid
 * invalid options setted by user, we parse raspi??? options in realtime as
 * the baseline of options.
 * 
 * date: Jan 24, 2019 9:13:26 AM <br/>
 * 
 * @see org.apache.commons.cli.Options
 * @see org.apache.commons.cli.Option
 * @version
 * @since JDK 1.8
 */
class OptionsBuilder {
	private final static Logger LOGGER = LoggerFactory.getLogger(OptionsBuilder.class);
	private final static Set<String> NO_VALUE_OPTIONS = new HashSet<>();
	private static Map<String, Options> SINGLETONS=new ConcurrentHashMap<>();
	static {
		// using short option
		NO_VALUE_OPTIONS.add("raw");
		NO_VALUE_OPTIONS.add("verbose");
		NO_VALUE_OPTIONS.add("demo");
		NO_VALUE_OPTIONS.add("fullpreview");
		NO_VALUE_OPTIONS.add("keypress");
		NO_VALUE_OPTIONS.add("signal");
		NO_VALUE_OPTIONS.add("gl");
		NO_VALUE_OPTIONS.add("glcapture");
		NO_VALUE_OPTIONS.add("settings");
		NO_VALUE_OPTIONS.add("burst");
		NO_VALUE_OPTIONS.add("datetime");
		NO_VALUE_OPTIONS.add("timestamp");
		NO_VALUE_OPTIONS.add("framestart");
		NO_VALUE_OPTIONS.add("nnpreview");
		NO_VALUE_OPTIONS.add("ISO");
		NO_VALUE_OPTIONS.add("vstab");
		NO_VALUE_OPTIONS.add("hflip");
		NO_VALUE_OPTIONS.add("vflip");
		NO_VALUE_OPTIONS.add("stats");
		NO_VALUE_OPTIONS.add("stereo");
		NO_VALUE_OPTIONS.add("dec");
		NO_VALUE_OPTIONS.add("3dswap");
		NO_VALUE_OPTIONS.add("analoggain");
		NO_VALUE_OPTIONS.add("digitalgain");
	}

	private OptionsBuilder() {

	}

	/**
	 * load supported options from command output. then modify some options
	 * manually. step 1: launch raspistill command to load supported options step 2:
	 * configurate zero long options
	 * @param name raspistill raspivid ... raspberrypi camera command line name
	 */
	public static Options create(String name) {
		synchronized (SINGLETONS) {
			if(SINGLETONS.containsKey(name)) {
				return SINGLETONS.get(name);
			}
			
			Options options = new Options();
			List<String> lines = CommanderUtil.execute(Constants.COMMAND_RASPISTILL);
			for (String line : lines) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(line);
				}
				if (!line.startsWith("-")) {
					continue;
				}
				int indexOfDesc = line.indexOf(":");
				String opts[] = line.substring(0, indexOfDesc).trim().split(",");
				String desc = line.substring(indexOfDesc + 1).trim();
				options.addOption(new Option(opts[0].trim().substring(1), opts[1].trim().substring(2), true, desc));
			}
			// no argument parameters
			for (String optionName : NO_VALUE_OPTIONS) {
				options.getOption(optionName).setArgs(0);
			}
			SINGLETONS.put(name, options);
			
			return options;
		}
	}
}
