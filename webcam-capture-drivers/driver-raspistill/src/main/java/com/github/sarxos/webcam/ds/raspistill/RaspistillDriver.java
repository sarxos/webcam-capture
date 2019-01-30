package com.github.sarxos.webcam.ds.raspistill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;

/**
 * 
 * ClassName: RaspistillDriver <br/>
 * Function: driver of raspistill, launch raspistill process then communicate
 * with it. this driver is special for raspberrypi <br/>
 * according to this raspberrypi frum thead
 * <a href="https://www.raspberrypi.org/forums/viewtopic.php?t=67175">67175</a>,
 * option "output" must be last option when using signal mode.
 * 
 * https://www.raspberrypi.org/documentation/raspbian/applications/camera.md
 * 
 * @author maoanapex88@163.com (alexmao86)
 * 
 *         date: Jan 23, 2019 9:57:03 AM <br/>
 */
public class RaspistillDriver implements WebcamDriver, Constants {
	private final static Logger LOGGER = LoggerFactory.getLogger(RaspistillDriver.class);
	private final static String[] DEFAULT_ARGUMENTS = { "--width", "320", "--height", "240", "--quality", "36",
			"--encoding", "png", /* "--verbose", */"--nopreview", "--keypress", "--timeout", "0", "--output", "-" };
	private final Options options;
	private Map<String, String> arguments = new LinkedHashMap<>();
	private volatile boolean getDeviceCalled;

	/**
	 * 
	 * Creates a new instance of RaspistillDriver.
	 * <ul>
	 * <li>step 1: load default arguments</li>
	 * <li>step 3: search system properties</li>
	 * </ul>
	 */
	public RaspistillDriver() {
		options = OptionsBuilder.create();

		parse(options, DEFAULT_ARGUMENTS);

		Properties properties = System.getProperties();
		List<String> cmdLine = new ArrayList<>();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			if (!entry.getKey().toString().startsWith(SYSTEM_PROP_PREFIX)) {
				continue;
			}
			cmdLine.add(entry.getKey().toString());
			cmdLine.add(entry.getValue().toString());
		}

		if (!cmdLine.isEmpty()) {
			String[] cmdArray = new String[cmdLine.size()];
			cmdLine.toArray(cmdArray);
			parse(options, cmdArray);
		}

	}

	private void parse(final Options options, String[] cmdArray) {
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cmd = parser.parse(options, cmdArray);
			Option[] opts = cmd.getOptions();
			for (Option o : opts) {
				arguments.put(o.getLongOpt(), o.getArgs() == 0 ? "" : o.getValue());
			}
		} catch (ParseException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(MSG_WRONG_ARGUMENT);
			}
			e.printStackTrace();
		}
	}

	/**
	 * <a href=
	 * "https://www.petervis.com/Raspberry_PI/Raspberry_Pi_CSI/raspberry-pi-csi-interface-connector-pinout.html">Raspberrypi
	 * camera connector</a>
	 * 
	 * @see com.github.sarxos.webcam.WebcamDriver#getDevices()
	 */
	@Override
	public List<WebcamDevice> getDevices() {
		synchronized (this) {
			List<String> stdout = CommanderUtil.execute(COMMAND_CAPTURE);
			if (stdout.isEmpty() || stdout.get(0).toLowerCase().contains(MSG_COMMAND_NOT_FOUND)) {
				throw new UnsupportedOperationException(MSG_RASPISTILL_NOT_INSTALLED);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(MSG_COMPATIBLE_WARN);
			}

			stdout = CommanderUtil.execute(COMMAND_CAMERA_CHECK);
			if (stdout.size() != 1) {
				return Collections.emptyList();
			}
			String cameraCheckOutput = stdout.get(0).trim();
			int supported = Integer.parseInt(
					cameraCheckOutput.substring(cameraCheckOutput.indexOf("=") + 1, cameraCheckOutput.indexOf(" ")));
			if (supported == 0) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(MSG_HARDWARE_NOT_FOUND);
				}
				return Collections.emptyList();
			}
			int detected = Integer.parseInt(cameraCheckOutput.substring(cameraCheckOutput.lastIndexOf("=") + 1));
			List<WebcamDevice> devices = new ArrayList<>(detected);
			for (int i = 0; i < detected; i++) {
				WebcamDevice device = new RaspistillDevice(i, this.options, createSortedOptionMap(arguments));// copy
																												// map
																												// rather
																												// than
																												// pass
																												// reference
				devices.add(device);
			}
			getDeviceCalled = true;
			return devices;
		}
	}

	/**
	 * accroding to some frum thread, output option must be last one, so dump all
	 * user options to new sorted map see
	 * <a href="https://www.raspberrypi.org/forums/viewtopic.php?t=67175">thread
	 * 67175</a>
	 * 
	 * @param arguments2
	 * @return
	 */
	private Map<String, String> createSortedOptionMap(Map<String, String> arguments) {
		Map<String, String> sorted = new TreeMap<>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int s1 = o1.hashCode();
				int s2 = o2.hashCode();

				if (o1.equals(OPT_OUTPUT)) {
					s1 = Integer.MAX_VALUE;
				}

				if (o2.equals(OPT_OUTPUT)) {
					s2 = Integer.MAX_VALUE;
				}

				return s1 - s2;
			}
		});
		sorted.putAll(arguments);
		return sorted;
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	public RaspistillDriver width(int width) {
		if (getDeviceCalled) {
			throw new UnsupportedOperationException(MSG_CANNOT_CHANGE_PROP);
		}
		arguments.put(OPT_WIDTH, width + "");
		return this;
	}

	public RaspistillDriver height(int height) {
		if (getDeviceCalled) {
			throw new UnsupportedOperationException(MSG_CANNOT_CHANGE_PROP);
		}
		arguments.put(OPT_HEIGHT, height + "");
		return this;
	}

	public RaspistillDriver quality(int quality) {
		if (getDeviceCalled) {
			throw new UnsupportedOperationException(MSG_CANNOT_CHANGE_PROP);
		}
		arguments.put(OPT_QUALITY, quality + "");
		return this;
	}
	// .....more options
}
