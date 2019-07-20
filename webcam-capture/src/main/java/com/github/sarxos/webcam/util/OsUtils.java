package com.github.sarxos.webcam.util;

/**
 * Just a simple enumeration with supported (not yet confirmed) operating
 * systems.
 * 
 * @author Bartosz Firyn (sarxos)
 */
public enum OsUtils {

	/**
	 * Microsoft Windows
	 */
	WIN,

	/**
	 * Linux or UNIX.
	 */
	NIX,

	/**
	 * Mac OS X
	 */
	OSX;

	private static OsUtils os = null;

	/**
	 * Get operating system.
	 * 
	 * @return OS
	 */
	public static OsUtils getOS() {
		if (os == null) {
			String osp = System.getProperty("os.name").toLowerCase();
			if (osp.contains("win")) {
				os = WIN;
			} else if (osp.contains("mac")) {
				os = OSX;
			} else if (osp.contains("nix") || osp.contains("nux")) {
				os = NIX;
			} else {
				throw new RuntimeException(osp + " is not supported");
			}
		}
		return os;
	}
}