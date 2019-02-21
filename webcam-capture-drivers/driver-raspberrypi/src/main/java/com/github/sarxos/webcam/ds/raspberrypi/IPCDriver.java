package com.github.sarxos.webcam.ds.raspberrypi;

import java.util.ArrayList;
import java.util.Collections;
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
 * ClassName: IPCDriver <br/>
 * interactive process communication abstraction. This class is designed to
 * reduce the number of methods that must be implemented by subclasses.
 * 
 * date: Jan 31, 2019 9:52:59 AM <br/>
 * 
 * @author maoanapex88@163.com (alexmao86)
 */
public abstract class IPCDriver implements WebcamDriver, Constants {
	private final static Logger LOGGER = LoggerFactory.getLogger(IPCDriver.class);
	private Map<String, String> arguments = new LinkedHashMap<>();
	private volatile boolean deviceCalled;
	private final Options options;
	private final String command;

	public IPCDriver(String command) {
		super();
		this.command = command;

		options = OptionsBuilder.create(command);

		parseArguments(options, getDefaultOptions());

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
			parseArguments(options, cmdArray);
		}
	}

	protected boolean isDeviceCalled() {
		return deviceCalled;
	}

	protected String getCommand() {
		return command;
	}

	/**
	 * get default options
	 * 
	 * @param options
	 */
	protected abstract String[] getDefaultOptions();

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	/**
	 * parse arguments and add to arguments
	 * 
	 * @param options
	 * @param cmdArray
	 */
	protected void parseArguments(final Options options, String[] cmdArray) {
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine cmd = parser.parse(options, cmdArray);
			Option[] opts = cmd.getOptions();
			for (Option o : opts) {
				arguments.put(o.getLongOpt(), o.getValue() == null ? "" : o.getValue());
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
			List<String> stdout = CommanderUtil.execute(this.command);
			if (stdout.isEmpty() || stdout.get(0).toLowerCase().contains(MSG_COMMAND_NOT_FOUND)) {
				throw new UnsupportedOperationException(MSG_RASPI_NOT_INSTALLED);
			}

			stdout = CommanderUtil.execute(COMMAND_VCGENCMD);
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
				WebcamDevice device = createIPCDevice(i, new LinkedHashMap<String, String>(arguments));
				devices.add(device);
			}
			deviceCalled = true;
			return devices;
		}
	}

	protected Options getOptions() {
		return options;
	}

	protected abstract WebcamDevice createIPCDevice(int camSelect, Map<String, String> parameters);
}
