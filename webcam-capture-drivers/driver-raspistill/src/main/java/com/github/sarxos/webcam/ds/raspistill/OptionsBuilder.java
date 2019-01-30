package com.github.sarxos.webcam.ds.raspistill;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ClassName: OptionsBuilder <br/>
 * this class is used to parse raspistill help text to apache cli options,
 * because raspistill is keep updating supported command options, so avoid
 * invalid options setted by user, we parse raspistill options in realtime as
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
	private final static Set<String> ZERO_LONG_OPTIONS = new HashSet<>();
	private static Options SINGLETON;
	static {
		// using short option
		ZERO_LONG_OPTIONS.add("r");
		ZERO_LONG_OPTIONS.add("v");
		ZERO_LONG_OPTIONS.add("d");
		ZERO_LONG_OPTIONS.add("fp");
		ZERO_LONG_OPTIONS.add("k");
		ZERO_LONG_OPTIONS.add("s");
		ZERO_LONG_OPTIONS.add("g");
		ZERO_LONG_OPTIONS.add("gc");
		ZERO_LONG_OPTIONS.add("set");
		ZERO_LONG_OPTIONS.add("bm");
		ZERO_LONG_OPTIONS.add("dt");
		ZERO_LONG_OPTIONS.add("ts");
		ZERO_LONG_OPTIONS.add("f");
		ZERO_LONG_OPTIONS.add("n");
		ZERO_LONG_OPTIONS.add("ISO");
		ZERO_LONG_OPTIONS.add("vs");
		ZERO_LONG_OPTIONS.add("hf");
		ZERO_LONG_OPTIONS.add("vf");
		ZERO_LONG_OPTIONS.add("st");
		ZERO_LONG_OPTIONS.add("3d");
		ZERO_LONG_OPTIONS.add("dec");
		ZERO_LONG_OPTIONS.add("3dswap");
		ZERO_LONG_OPTIONS.add("ag");
		ZERO_LONG_OPTIONS.add("dg");
	}

	private OptionsBuilder() {

	}

	/**
	 * load supported options from command output. then modify some options
	 * manually. step 1: launch raspistill command to load supported options step 2:
	 * configurate zero long options
	 */
	public static Options create() {
		synchronized (ZERO_LONG_OPTIONS) {
			if (SINGLETON == null) {
				Options options = new Options();
				List<String> lines = CommanderUtil.execute(Constants.COMMAND_CAPTURE);
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
				for (String optionName : ZERO_LONG_OPTIONS) {
					options.getOption(optionName).setArgs(0);
				}
				SINGLETON = options;
			}
			return SINGLETON;
		}
	}
}
