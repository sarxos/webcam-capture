package com.github.sarxos.webcam.ds.ffmpegcli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.ds.ffmpegcli.impl.VideoDeviceFilenameFilter;


public class FFmpegCliDriver implements WebcamDriver, WebcamDiscoverySupport {

	private static final Logger LOG = LoggerFactory.getLogger(FFmpegCliDriver.class);

	private static final VideoDeviceFilenameFilter VFFILTER = new VideoDeviceFilenameFilter();
	private static final Runtime RT = Runtime.getRuntime();
	private static final String MARKER = "mjpeg";
	private static final String STARTER = "[video4linux";

	@Override
	public List<WebcamDevice> getDevices() {

		File[] vfiles = VFFILTER.getVideoFiles();

		List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

		OutputStream os = null;
		InputStream is1 = null;
		InputStream is2 = null;
		Process process = null;

		String line = null;
		BufferedReader br1 = null;
		BufferedReader br2 = null;

		for (File vfile : vfiles) {

			String[] cmd = new String[] { "ffmpeg", "-f", "video4linux2", "-list_formats", "all", "-i", vfile.getAbsolutePath() };

			if (LOG.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				for (String c : cmd) {
					sb.append(c).append(' ');
				}
				LOG.debug("Executing command: {}", sb.toString());
			}

			try {
				process = RT.exec(cmd);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			os = process.getOutputStream();
			is1 = process.getInputStream();
			is2 = process.getErrorStream();

			try {
				os.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			br1 = new BufferedReader(new InputStreamReader(is1));
			br2 = new BufferedReader(new InputStreamReader(is2));

			boolean read = false;

			try {
				while ((line = br2.readLine()) != null) {
					if (line.startsWith(STARTER) && line.indexOf(MARKER) != -1) {
						LOG.debug("Command stderr line: {}", line);
						devices.add(new FFmpegCliDevice(vfile, line));
						read = true;
						break;
					}
				}
				if (!read) {
					while ((line = br1.readLine()) != null) {
						if (line.startsWith(STARTER) && line.indexOf(MARKER) != -1) {
							LOG.debug("Command stdout line: {}", line);
							devices.add(new FFmpegCliDevice(vfile, line));
							break;
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					is1.close();
					is2.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return devices;
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
