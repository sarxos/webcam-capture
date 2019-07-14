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
	 * the raspiyuv command name
	 */
	String COMMAND_RASPIYUV = "raspiyuv";
	/**
	 * the raspivid command name
	 */
	String COMMAND_RASPIVID = "raspivid";
	/**
	 * the raspividyuv command name
	 */
	String COMMAND_RASPIVIDYUV = "raspividyuv";
	/**
	 * native command to check camera support. it will return supported=1 detected=?
	 * for supported 0 for not support, 1 support ? is the number of camera
	 * supported.
	 */
	String COMMAND_VCGENCMD = "vcgencmd get_camera";
	/**
	 * system property prefix for raspi(still|vid|...) configuation. driver options
	 * can be configurated globally by passing -Draspi.${option}=${value} in your
	 * java launch arguments. or programmatically <code>System.setProperty("key",
	 * "value"); the key is raspi??? long option name without "--"
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
	/** AWBGAINS --awbgains, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_AWBGAINS = "awbgains";

	/** FLICKER --flicker, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_FLICKER = "flicker";

	/** KEYPRESS --keypress, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_KEYPRESS = "keypress";

	/** GLWIN --glwin, raspistill */
	String OPT_GLWIN = "glwin";

	/** ANNOTATE --annotate, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_ANNOTATE = "annotate";

	/** BITRATE --bitrate, raspivid */
	String OPT_BITRATE = "bitrate";

	/** VSTAB --vstab, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_VSTAB = "vstab";

	/** LISTEN --listen, raspivid, raspividyuv */
	String OPT_LISTEN = "listen";

	/** OUTPUT --output, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_OUTPUT = "output";

	/** MODE --mode, raspistill, raspivid, raspividyuv */
	String OPT_MODE = "mode";

	/** SATURATION --saturation, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_SATURATION = "saturation";

	/** DATETIME --datetime, raspistill */
	String OPT_DATETIME = "datetime";

	/** SPLIT --split, raspivid */
	String OPT_SPLIT = "split";

	/** DRC --drc, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_DRC = "drc";

	/** STATS --stats, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_STATS = "stats";

	/** SEGMENT --segment, raspivid */
	String OPT_SEGMENT = "segment";

	/** DECIMATE --decimate, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_DECIMATE = "decimate";

	/** 3DSWAP --3dswap, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_3DSWAP = "3dswap";

	/** RAW_FORMAT --raw-format, raspivid */
	String OPT_RAW_FORMAT = "raw-format";

	/** SHARPNESS --sharpness, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_SHARPNESS = "sharpness";

	/** ANALOGGAIN --analoggain, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_ANALOGGAIN = "analoggain";

	/** SIGNAL --signal, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_SIGNAL = "signal";

	/** HEIGHT --height, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_HEIGHT = "height";

	/** QP --qp, raspivid */
	String OPT_QP = "qp";

	/** SETTINGS --settings, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_SETTINGS = "settings";

	/** TIMED --timed, raspivid, raspividyuv */
	String OPT_TIMED = "timed";

	/** INTRA --intra, raspivid */
	String OPT_INTRA = "intra";

	/** LEVEL --level, raspivid */
	String OPT_LEVEL = "level";

	/** PROFILE --profile, raspivid */
	String OPT_PROFILE = "profile";

	/** RAW --raw, raspistill, raspivid */
	String OPT_RAW = "raw";

	/** CIRCULAR --circular, raspivid */
	String OPT_CIRCULAR = "circular";

	/** ENCODING --encoding, raspistill */
	String OPT_ENCODING = "encoding";

	/** QUALITY --quality, raspistill */
	String OPT_QUALITY = "quality";

	/** ANNOTATEEX --annotateex, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_ANNOTATEEX = "annotateex";

	/** EV --ev, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_EV = "ev";

	/** CODEC --codec, raspivid */
	String OPT_CODEC = "codec";

	/** VECTORS --vectors, raspivid */
	String OPT_VECTORS = "vectors";

	/** FULLSCREEN --fullscreen, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_FULLSCREEN = "fullscreen";

	/** EXPOSURE --exposure, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_EXPOSURE = "exposure";

	/** GLCAPTURE --glcapture, raspistill */
	String OPT_GLCAPTURE = "glcapture";

	/** CONTRAST --contrast, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_CONTRAST = "contrast";

	/** STEREO --stereo, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_STEREO = "stereo";

	/** GLSCENE --glscene, raspistill */
	String OPT_GLSCENE = "glscene";

	/** BURST --burst, raspistill, raspiyuv */
	String OPT_BURST = "burst";

	/** HFLIP --hflip, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_HFLIP = "hflip";

	/** EXIF --exif, raspistill */
	String OPT_EXIF = "exif";

	/** PREVIEW --preview, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_PREVIEW = "preview";

	/** DIGITALGAIN --digitalgain, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_DIGITALGAIN = "digitalgain";

	/** CAMSELECT --camselect, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_CAMSELECT = "camselect";

	/** THUMB --thumb, raspistill */
	String OPT_THUMB = "thumb";

	/** FRAMERATE --framerate, raspivid, raspividyuv */
	String OPT_FRAMERATE = "framerate";

	/** RGB --rgb, raspividyuv, raspiyuv */
	String OPT_RGB = "rgb";

	/** DEMO --demo, raspistill, raspivid, raspividyuv */
	String OPT_DEMO = "demo";

	/** VFLIP --vflip, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_VFLIP = "vflip";

	/** ROI --roi, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_ROI = "roi";

	/** TIMEOUT --timeout, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_TIMEOUT = "timeout";

	/** FULLPREVIEW --fullpreview, raspistill, raspiyuv */
	String OPT_FULLPREVIEW = "fullpreview";

	/** FLUSH --flush, raspivid */
	String OPT_FLUSH = "flush";

	/** METERING --metering, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_METERING = "metering";

	/** IMXFX --imxfx, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_IMXFX = "imxfx";

	/** TIMELAPSE --timelapse, raspistill, raspiyuv */
	String OPT_TIMELAPSE = "timelapse";

	/** PENC --penc, raspivid */
	String OPT_PENC = "penc";

	/** LATEST --latest, raspistill, raspiyuv */
	String OPT_LATEST = "latest";

	/** TIMESTAMP --timestamp, raspistill */
	String OPT_TIMESTAMP = "timestamp";

	/** IREFRESH --irefresh, raspivid */
	String OPT_IREFRESH = "irefresh";

	/** ISO --ISO, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_ISO = "ISO";

	/** GL --gl, raspistill */
	String OPT_GL = "gl";

	/** RESTART --restart, raspistill */
	String OPT_RESTART = "restart";

	/** INITIAL --initial, raspivid, raspividyuv */
	String OPT_INITIAL = "initial";

	/** SHUTTER --shutter, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_SHUTTER = "shutter";

	/** ROTATION --rotation, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_ROTATION = "rotation";

	/** START --start, raspivid */
	String OPT_START = "start";

	/** FRAMESTART --framestart, raspistill */
	String OPT_FRAMESTART = "framestart";

	/** AWB --awb, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_AWB = "awb";

	/** VERBOSE --verbose, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_VERBOSE = "verbose";

	/** LUMA --luma, raspividyuv, raspiyuv */
	String OPT_LUMA = "luma";

	/** HELP --help, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_HELP = "help";

	/** BRIGHTNESS --brightness, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_BRIGHTNESS = "brightness";

	/** NOPREVIEW --nopreview, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_NOPREVIEW = "nopreview";

	/** INLINE --inline, raspivid */
	String OPT_INLINE = "inline";

	/** COLFX --colfx, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_COLFX = "colfx";

	/** WIDTH --width, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_WIDTH = "width";

	/** SAVE_PTS --save-pts, raspivid, raspividyuv */
	String OPT_SAVE_PTS = "save-pts";

	/** OPACITY --opacity, raspistill, raspivid, raspividyuv, raspiyuv */
	String OPT_OPACITY = "opacity";

	/** WRAP --wrap, raspivid */
	String OPT_WRAP = "wrap";

	// *******************message templates**************
	String MSG_CANNOT_CHANGE_PROP = "can not change property after device already discoveried";
	String MSG_RASPI_NOT_INSTALLED = "{} is not found, please run apt-get install {}. this driver supposed to run on raspberrypi";

	String MSG_WRONG_ARGUMENT = "wrong raspistill argument";
	String MSG_NOT_SUPPORTED_OS_WARN = "Driver supposed to run on raspberrypi";
	String MSG_NOT_GRACEFUL_DOWN = "device is not shutdown perfactly, there maybe resource link?";
	String MSG_NOT_RUNNING_WARN = "device does not running";
	String MSG_HARDWARE_NOT_FOUND = "camera hardware not found";
	String MSG_COMMAND_NOT_FOUND = "command not found";
}
