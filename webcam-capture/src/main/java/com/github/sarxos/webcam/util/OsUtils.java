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
	public static final OsUtils getOS() {
		if (os == null) {
			String osp = System.getProperty("os.name").toLowerCase();
			if (osp.indexOf("win") >= 0) {
				os = WIN;
			} else if (osp.indexOf("mac") >= 0) {
				os = OSX;
			} else if (osp.indexOf("nix") >= 0 || osp.indexOf("nux") >= 0) {
				os = NIX;
			} else {
				throw new RuntimeException(osp + " is not supported");
			}
		}
		return os;
	}
}