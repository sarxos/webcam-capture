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
 * invalid options setted by user, we parse raspi??? options in realtime as the
 * baseline of options.
 * 
 * date: Jan 24, 2019 9:13:26 AM <br/>
 * 
 * @see org.apache.commons.cli.Options
 * @see org.apache.commons.cli.Option
 */
class OptionsBuilder implements Constants {
	private final static Logger LOGGER = LoggerFactory.getLogger(OptionsBuilder.class);
	// raspi??? to options
	private static Map<String, Options> SINGLETONS = new ConcurrentHashMap<>();

	private static Set<String> NO_VALUE_OPTIONS = new HashSet<>();
	static {
		NO_VALUE_OPTIONS.add(OPT_RAW);
		NO_VALUE_OPTIONS.add(OPT_LATEST);
		NO_VALUE_OPTIONS.add(OPT_VERBOSE);
		NO_VALUE_OPTIONS.add(OPT_DEMO);
		NO_VALUE_OPTIONS.add(OPT_FULLPREVIEW);
		NO_VALUE_OPTIONS.add(OPT_KEYPRESS);
		NO_VALUE_OPTIONS.add(OPT_SIGNAL);
		NO_VALUE_OPTIONS.add(OPT_GL);
		NO_VALUE_OPTIONS.add(OPT_GLCAPTURE);
		NO_VALUE_OPTIONS.add(OPT_SETTINGS);
		NO_VALUE_OPTIONS.add(OPT_DATETIME);
		NO_VALUE_OPTIONS.add(OPT_FRAMESTART);

		NO_VALUE_OPTIONS.add(OPT_PREVIEW);
		NO_VALUE_OPTIONS.add(OPT_FULLSCREEN);
		NO_VALUE_OPTIONS.add(OPT_NOPREVIEW);

		NO_VALUE_OPTIONS.add(OPT_ISO);
		NO_VALUE_OPTIONS.add(OPT_VSTAB);
		NO_VALUE_OPTIONS.add(OPT_STATS);
		NO_VALUE_OPTIONS.add(OPT_STEREO);
		NO_VALUE_OPTIONS.add(OPT_DECIMATE);
		NO_VALUE_OPTIONS.add(OPT_HFLIP);
		NO_VALUE_OPTIONS.add(OPT_VFLIP);
		NO_VALUE_OPTIONS.add(OPT_3DSWAP);

		NO_VALUE_OPTIONS.add(OPT_PENC);
		NO_VALUE_OPTIONS.add(OPT_CIRCULAR);
		NO_VALUE_OPTIONS.add(OPT_FLUSH);
		NO_VALUE_OPTIONS.add(OPT_SAVE_PTS);
		NO_VALUE_OPTIONS.add(OPT_RGB);
		NO_VALUE_OPTIONS.add(OPT_FLUSH);
	}

	/**
	 * load supported options from command output. then modify some options
	 * manually. step 1: launch raspistill command to load supported options step 2:
	 * configurate zero long options
	 * 
	 * @param name
	 *            raspistill raspivid ... raspberrypi camera command line name
	 */
	public static Options create(String name) {
		synchronized (SINGLETONS) {
			if (SINGLETONS.containsKey(name)) {
				return SINGLETONS.get(name);
			}

			Options options = new Options();
			List<String> lines = CommanderUtil.execute(name);
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
			SINGLETONS.put(name, options);
			for (String opt : NO_VALUE_OPTIONS) {
				Option option = options.getOption(opt);
				if (option != null) {
					option.setArgs(0);
				}
			}
			return options;
		}
	}
}
