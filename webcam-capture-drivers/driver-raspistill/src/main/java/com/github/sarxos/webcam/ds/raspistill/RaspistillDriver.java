package com.github.sarxos.webcam.ds.raspistill;

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
 * option "output" must be last option when using signal mode
 * 
 * @author maoanapex88@163.com (alexmao86)
 * 
 *         date: Jan 23, 2019 9:57:03 AM <br/>
 */
public class RaspistillDriver implements WebcamDriver {

	private final static Logger LOGGER = LoggerFactory.getLogger(RaspistillDriver.class);
	private final static String[] DEFAULT_ARGUMENTS = { "--width", "800", "--height", "600", "--quality", "85",
			"--encoding", "png", "--nopreview", "--signal", "--output", "-" };
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
			if (!entry.getKey().toString().startsWith(Constants.SYSTEM_PROP_PREFIX)) {
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
				LOGGER.debug(Constants.MSG_WRONG_ARGUMENT);
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
			List<String> stdout = CommanderUtil.execute("uname -a");
			if (stdout.isEmpty() || !stdout.get(0).contains("Linux Raspberrypi")) {
				LOGGER.warn(Constants.MSG_NOT_SUPPORTED_OS_WARN);
			}

			stdout = CommanderUtil.execute(Constants.COMMAND_NAME);
			if (stdout.isEmpty() || stdout.get(0).toLowerCase().contains("command not found")) {
				throw new UnsupportedOperationException(Constants.MSG_RASPISTILL_NOT_INSTALLED);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(Constants.MSG_COMPATIBLE_WARN);
			}

			List<WebcamDevice> devices = new ArrayList<>(1);
			// TODO check hardware if hardware is one dual camera module. if dual camera
			// return two devices
			WebcamDevice device = new RaspistillDevice(0, new LinkedHashMap<>(arguments));
			devices.add(device);
			getDeviceCalled = true;
			return devices;
		}
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	public RaspistillDriver width(int width) {
		if (getDeviceCalled) {
			throw new UnsupportedOperationException(Constants.MSG_CANNOT_CHANGE_PROP);
		}
		arguments.put(Constants.OPT_WIDTH, width + "");
		return this;
	}

	public RaspistillDriver height(int height) {
		if (getDeviceCalled) {
			throw new UnsupportedOperationException(Constants.MSG_CANNOT_CHANGE_PROP);
		}
		arguments.put(Constants.OPT_HEIGHT, height + "");
		return this;
	}

	public RaspistillDriver quality(int quality) {
		if (getDeviceCalled) {
			throw new UnsupportedOperationException(Constants.MSG_CANNOT_CHANGE_PROP);
		}
		arguments.put(Constants.OPT_QUALITY, quality + "");
		return this;
	}
	// .....more options
}
