/*
 * Copyright (c) 2017, Robert Bosch (Suzhou) All Rights Reserved.
 * This software is property of Robert Bosch (Suzhou). 
 * Unauthorized duplication and disclosure to third parties is prohibited.
 */
package com.github.sarxos.webcam.ds.raspberrypi;

/**
 * ClassName: Constants <br/>
 * all constants strings used by driver date: Jan 25, 2019 9:08:02 AM <br/>
 * 
 */
public interface Constants {
	/**
	 * the raspistill command name
	 */
	String COMMAND_RASPISTILL = "raspistill";
	/**
	 * native command to check camera support. it will return supported=1 detected=?
	 * for supported 0 for not support, 1 support ? is the number of camera
	 * supported.
	 */
	String COMMAND_VCGENCMD = "vcgencmd get_camera";
	/**
	 * system property prefix for raspistill configuation. driver options can be
	 * configurated globally by passing -Draspistill.${option}=${value} in your java
	 * launch arguments. or programmatically <code>System.setProperty("key",
	 * "value"); the key is raspistill long option name without "--"
	 */
	String SYSTEM_PROP_PREFIX = "raspi.";

	String DEVICE_NAME_PREFIX = "raspberrypi camera ";
	/**
	 * extended option, it can be processed by
	 * 
	 * @see com.github.sarxos.webcam.WebcamDevice.Configurable but not raspistill
	 *      argument. value type is Integer
	 */
	String EXTENDED_OPT_FPS = "FPS";

	// *******************raspistill options constants**************
	/**
	 * OPT_QUALITY raspistill --quality
	 */
	String OPT_QUALITY = "quality";
	/**
	 * OPT_HEIGHT raspistill --height
	 */
	String OPT_HEIGHT = "height";
	/**
	 * OPT_WIDTH raspistill --width
	 */
	String OPT_WIDTH = "width";
	/**
	 * OPT_TIMELAPSE --timelapse
	 */
	String OPT_TIMELAPSE = "timelapse";
	/**
	 * OPT_OUTPUT --output
	 */
	String OPT_OUTPUT = "output";
	/**
	 * OPT_CAMSELECT --camselect
	 */
	String OPT_CAMSELECT = "camselect";
	/**
	 * OPT_NOPREVIEW --nopreview
	 */
	String OPT_NOPREVIEW = "nopreview";
	/**
	 * OPT_SETTINGS -settings
	 */
	String OPT_SETTINGS = "settings";
	/**
	 * OPT_HELP --help
	 */
	String OPT_HELP = "help";
	/**
	 * OPT_OPACITY --opacity
	 */
	String OPT_OPACITY = "opacity";
	/**
	 * OPT_FULLSCREEN --fullscreen
	 */
	String OPT_FULLSCREEN = "fullscreen";
	/**
	 * OPT_PREVIEW --preview
	 */
	String OPT_PREVIEW = "preview";
	/**
	 * OPT_ENCODING --encoding
	 */
	String OPT_ENCODING = "encoding";
	// *******************message templates**************
	String MSG_CANNOT_CHANGE_PROP = "can not change property after device already discoveried";
	String MSG_RASPISTILL_NOT_INSTALLED = "raspistill is not found, please run apt-get install raspistill. this driver supposed to run on raspberrypi";
	String MSG_COMPATIBLE_WARN = "now raspberrypi only support one camera connector with dual camera, so just retrun camera 0";
	String MSG_WRONG_ARGUMENT = "wrong raspistill argument";
	String MSG_NOT_SUPPORTED_OS_WARN = "RaspistillDriver supposed to run on raspberrypi";
	String MSG_NOT_GRACEFUL_DOWN = "device is not shutdown perfactly, there maybe resource link?";
	String MSG_NOT_RUNNING_WARN = "device does not running";
	String MSG_HARDWARE_NOT_FOUND = "camera hardware not found";
	String MSG_COMMAND_NOT_FOUND = "command not found";
}
