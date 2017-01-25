package com.github.sarxos.webcam.ds.gst1.impl;

import java.io.File;


public class GsPlatform {

	private static final String OS_NAME = System.getProperty("os.name", "");

	public enum OS {
		WINDOWS,
		LINUX,
		MACOS,
	}

	private static OS os;
	static {
		if (GsPlatform.isLinux()) {
			os = OS.LINUX;
		}
		if (GsPlatform.isWindows()) {
			os = OS.WINDOWS;
		}
		if (GsPlatform.isMacOSX()) {
			os = OS.MACOS;
		}
	}

	public static boolean isUnix() {
		return File.separatorChar == '/';
	}

	public static boolean isWindows() {
		return File.separatorChar == '\\';
	}

	public static boolean isLinux() {
		return isUnix() && OS_NAME.toLowerCase().contains("linux");
	}

	public static boolean isMacOSX() {
		return isUnix() && (OS_NAME.startsWith("Mac") || OS_NAME.startsWith("Darwin"));
	}

	public static boolean isSolaris() {
		return isUnix() && (OS_NAME.startsWith("SunOS") || OS_NAME.startsWith("Solaris"));
	}

	public static OS getOs() {
		return os;
	}
}
