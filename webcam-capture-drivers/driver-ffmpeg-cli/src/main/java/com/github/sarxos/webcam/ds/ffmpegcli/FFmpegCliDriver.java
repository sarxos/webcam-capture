package com.github.sarxos.webcam.ds.ffmpegcli;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.ffmpegcli.impl.VideoDeviceFilenameFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.bridj.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FFmpegCliDriver implements WebcamDriver, WebcamDiscoverySupport {

	private static final Logger LOG = LoggerFactory.getLogger(FFmpegCliDriver.class);

	private static final VideoDeviceFilenameFilter VFFILTER = new VideoDeviceFilenameFilter();

	private String path = "";

	@Override
	public List<WebcamDevice> getDevices() {
		List<WebcamDevice> devices;

		if (Platform.isWindows()) {
			devices = getWindowsDevices();
		} else {
			devices = getUnixDevices();
		}

		return devices;
	}

	private List<WebcamDevice> getUnixDevices() {
		File[] vfiles = VFFILTER.getVideoFiles();

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		String line = null;
		BufferedReader br = null;

		for (File vfile : vfiles) {

			String[] cmd = new String[] {
				getCommand(),
				"-f", FFmpegCliDriver.getCaptureDriver(),
				"-hide_banner", "",
				"-list_formats", "all",
				"-i", vfile.getAbsolutePath(),
			};

			Process process = startProcess(cmd);

			InputStream is = process.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));

			final String STARTER = "[" + FFmpegCliDriver.getCaptureDriver();
			final String MARKER = "] Raw";

			try {
				while ((line = br.readLine()) != null) {
					if (line.startsWith(STARTER) && line.contains(MARKER)) {
						LOG.debug("Command stdout line: {}", line);
						String resolutions = line.split(" : ")[3].trim();
						devices.add(new FFmpegCliDevice(path, vfile, resolutions));
						break;
					}
				}

			} catch (IOException e) {
				throw new WebcamException(e);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					throw new WebcamException(e);
				}
				process.destroy();
				try {
					process.waitFor();
				} catch (InterruptedException e) {
					throw new WebcamException(e);
				}
			}
		}
		return devices;
	}

	private List<WebcamDevice> getWindowsDevices() {

		List<String> devicesNames = getWindowsDevicesNames();

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		for (String deviceName : devicesNames) {
			devices.add(buildWindowsDevice(deviceName));
		}

		return devices;
	}

	private WebcamDevice buildWindowsDevice(String deviceName) {
		String deviceInput = "\"video=" + deviceName + "\"";

		String[] cmd = new String[] { getCommand(), "-list_options", "true", "-f", FFmpegCliDriver.getCaptureDriver(), "-hide_banner", "", "-i", deviceInput };

		Process listDevicesProcess = startProcess(cmd);

		final String STARTER = "[" + FFmpegCliDriver.getCaptureDriver();
		final String MARKER = "max s=";

		Set<String> resolutions = new LinkedHashSet<>();

		InputStream is = null;
		BufferedReader br;
		String line;
		try {
			is = listDevicesProcess.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));

			while ((line = br.readLine()) != null) {
				if (line.startsWith(STARTER) && line.contains(MARKER)) {
					int begin = line.indexOf(MARKER) + MARKER.length();
					String resolution = line.substring(begin, line.indexOf(" ", begin));
					resolutions.add(resolution);
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		StringBuilder vinfo = new StringBuilder();
		for (String resolution : resolutions) {
			vinfo.append(resolution).append(" ");
		}

		return new FFmpegCliDevice(path, deviceName, vinfo.toString().trim());
	}

	private List<String> getWindowsDevicesNames() {
		String[] cmd = new String[] { getCommand(), "-list_devices", "true", "-f", FFmpegCliDriver.getCaptureDriver(), "-hide_banner", "", "-i", "dummy" };
		Process listDevicesProcess = startProcess(cmd);

		List<String> devicesNames = new ArrayList<>();

		final String STARTER = "[" + FFmpegCliDriver.getCaptureDriver();
		final String NAME_MARKER = "]  \"";
		final String VIDEO_MARKER = "] DirectShow video";
		final String AUDIO_MARKER = "] DirectShow audio";

		boolean startDevices = false;

		InputStream is = null;
		BufferedReader br;
		String line;
		try {
			is = listDevicesProcess.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));

			while ((line = br.readLine()) != null) {
				if (line.startsWith(STARTER) && line.contains(VIDEO_MARKER)) {
					startDevices = true;
					continue;
				}
				if (startDevices) {
					if (line.startsWith(STARTER) && line.contains(NAME_MARKER)) {
						String deviceName = line.substring(line.indexOf(NAME_MARKER) + NAME_MARKER.length());
						// Remove final double quotes
						deviceName = deviceName.substring(0, deviceName.length() - 1);
						devicesNames.add(deviceName);
						continue;
					}
					if (line.startsWith(STARTER) && line.contains(AUDIO_MARKER)) {
						break;
					}
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return devicesNames;
	}

	private Process startProcess(String[] cmd) {
		Process process = null;

		OutputStream os;
		if (LOG.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (String c : cmd) {
				sb.append(c).append(' ');
			}
			LOG.debug("Executing command: {}", sb.toString());
		}

		try {
			ProcessBuilder builder = new ProcessBuilder(cmd);
			builder.redirectErrorStream(true);
			process = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		os = process.getOutputStream();

		try {
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return process;
	}

	public static String getCaptureDriver() {
		if (Platform.isLinux()) {
			return "video4linux2";
		} else if (Platform.isWindows()) {
			return "dshow";
		} else if (Platform.isMacOSX()) {
			return "avfoundation";
		}

		// Platform not supported
		return null;
	}

	public FFmpegCliDriver withPath(String path) {
		this.path = path;
		return this;
	}

	private String getCommand() {
		return getCommand(path);
	}

	public static String getCommand(String path) {
		return path + "ffmpeg";
	}

	@Override
	public boolean isThreadSafe() {
		return false;
	}

	@Override
	public long getScanInterval() {
		return 3000;
	}

	@Override
	public boolean isScanPossible() {
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}