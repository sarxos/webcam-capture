package com.github.sarxos.webcam.ds.raspberrypi;

import static uk.co.caprica.picam.PicamNativeLibrary.installTempLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDriver;
import com.github.sarxos.webcam.util.CommanderUtil;

import uk.co.caprica.picam.NativeLibraryException;

public class PICamDriver implements WebcamDriver {
	private final static Logger LOGGER = LoggerFactory.getLogger(PICamDriver.class);
	private static final AtomicBoolean nativeInstalled=new AtomicBoolean(false);
	private Map<String, String> arguments = new LinkedHashMap<>();
	
	static {
		try {
			installTempLibrary();
			nativeInstalled.set(true);
		} catch (NativeLibraryException e) {
			e.printStackTrace();
		}
	}
	/**
	 * modify arguments, please check raspistill command help document.
	 * <pre>
	 * raspistill Camera App v1.3.11
		
		Runs camera for specific time, and take JPG capture at end if requested
		
		usage: raspistill [options]
		
		Image parameter commands
		
		-?, --help	: This help information
		-w, --width	: Set image width <size>
		-h, --height	: Set image height <size>
		-q, --quality	: Set jpeg quality <0 to 100>
		-r, --raw	: Add raw bayer data to jpeg metadata
		-o, --output	: Output filename <filename> (to write to stdout, use '-o -'). If not specified, no file is saved
		-l, --latest	: Link latest complete image to filename <filename>
		-v, --verbose	: Output verbose information during run
		-t, --timeout	: Time (in ms) before takes picture and shuts down (if not specified, set to 5s)
		-th, --thumb	: Set thumbnail parameters (x:y:quality) or none
		-d, --demo	: Run a demo mode (cycle through range of camera options, no capture)
		-e, --encoding	: Encoding to use for output file (jpg, bmp, gif, png)
		-x, --exif	: EXIF tag to apply to captures (format as 'key=value') or none
		-tl, --timelapse	: Timelapse mode. Takes a picture every <t>ms. %d == frame number (Try: -o img_%04d.jpg)
		-fp, --fullpreview	: Run the preview using the still capture resolution (may reduce preview fps)
		-k, --keypress	: Wait between captures for a ENTER, X then ENTER to exit
		-s, --signal	: Wait between captures for a SIGUSR1 or SIGUSR2 from another process
		-g, --gl	: Draw preview to texture instead of using video render component
		-gc, --glcapture	: Capture the GL frame-buffer instead of the camera image
		-set, --settings	: Retrieve camera settings and write to stdout
		-cs, --camselect	: Select camera <number>. Default 0
		-bm, --burst	: Enable 'burst capture mode'
		-md, --mode	: Force sensor mode. 0=auto. See docs for other modes available
		-dt, --datetime	: Replace output pattern (%d) with DateTime (MonthDayHourMinSec)
		-ts, --timestamp	: Replace output pattern (%d) with unix timestamp (seconds since 1970)
		-fs, --framestart	: Starting frame number in output pattern(%d)
		-rs, --restart	: JPEG Restart interval (default of 0 for none)
		
		Preview parameter commands
		
		-p, --preview	: Preview window settings <'x,y,w,h'>
		-f, --fullscreen	: Fullscreen preview mode
		-op, --opacity	: Preview window opacity (0-255)
		-n, --nopreview	: Do not display a preview window
		
		Image parameter commands
		
		-sh, --sharpness	: Set image sharpness (-100 to 100)
		-co, --contrast	: Set image contrast (-100 to 100)
		-br, --brightness	: Set image brightness (0 to 100)
		-sa, --saturation	: Set image saturation (-100 to 100)
		-ISO, --ISO	: Set capture ISO
		-vs, --vstab	: Turn on video stabilisation
		-ev, --ev	: Set EV compensation - steps of 1/6 stop
		-ex, --exposure	: Set exposure mode (see Notes)
		-fli, --flicker	: Set flicker avoid mode (see Notes)
		-awb, --awb	: Set AWB mode (see Notes)
		-ifx, --imxfx	: Set image effect (see Notes)
		-cfx, --colfx	: Set colour effect (U:V)
		-mm, --metering	: Set metering mode (see Notes)
		-rot, --rotation	: Set image rotation (0-359)
		-hf, --hflip	: Set horizontal flip
		-vf, --vflip	: Set vertical flip
		-roi, --roi	: Set region of interest (x,y,w,d as normalised coordinates [0.0-1.0])
		-ss, --shutter	: Set shutter speed in microseconds
		-awbg, --awbgains	: Set AWB gains - AWB mode must be off
		-drc, --drc	: Set DRC Level (see Notes)
		-st, --stats	: Force recomputation of statistics on stills capture pass
		-a, --annotate	: Enable/Set annotate flags or text
		-3d, --stereo	: Select stereoscopic mode
		-dec, --decimate	: Half width/height of stereo image
		-3dswap, --3dswap	: Swap camera order for stereoscopic
		-ae, --annotateex	: Set extra annotation parameters (text size, text colour(hex YUV), bg colour(hex YUV))
		-ag, --analoggain	: Set the analog gain (floating point)
		-dg, --digitalgain	: Set the digital gain (floating point)
		
		
		Notes
		
		Exposure mode options :
		off,auto,night,nightpreview,backlight,spotlight,sports,snow,beach,verylong,fixedfps,antishake,fireworks
		
		Flicker avoid mode options :
		off,auto,50hz,60hz
		
		AWB mode options :
		off,auto,sun,cloud,shade,tungsten,fluorescent,incandescent,flash,horizon
		
		Image Effect mode options :
		none,negative,solarise,sketch,denoise,emboss,oilpaint,hatch,gpen,pastel,watercolour,film,blur,saturation,colourswap,washedout,posterise,colourpoint,colourbalance,cartoon
		
		Metering Mode options :
		average,spot,backlit,matrix
		
		Dynamic Range Compression (DRC) options :
		off,low,med,high
		
		Preview parameter commands
		
		-gs, --glscene	: GL scene square,teapot,mirror,yuv,sobel,vcsm_square
		-gw, --glwin	: GL window settings <'x,y,w,h'>
	 * </pre>
	 * @param params
	 */
	public void setCameraConfiguration(Map<String, String> params){
		arguments.putAll(params);
	}
	@Override
	public List<WebcamDevice> getDevices() {
		if(!nativeInstalled.get()){
			throw new java.lang.UnsupportedOperationException("picam native binary is not installed correctly");
		}
		List<String> stdout = CommanderUtil.execute(Constants.COMMAND_VCGENCMD, 5000);
		if (stdout.size() != 1) {
			return Collections.emptyList();
		}
		String cameraCheckOutput = stdout.get(0).trim();
		int supported = Integer.parseInt(
				cameraCheckOutput.substring(cameraCheckOutput.indexOf("=") + 1, cameraCheckOutput.indexOf(" ")));
		if (supported == 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(Constants.MSG_HARDWARE_NOT_FOUND);
			}
			return Collections.emptyList();
		}
		int detected = Integer.parseInt(cameraCheckOutput.substring(cameraCheckOutput.lastIndexOf("=") + 1));
		List<WebcamDevice> devices = new ArrayList<>(detected);
		for (int i = 0; i < detected; i++) {
			WebcamDevice device = createPICamDevice(i, new LinkedHashMap<String, String>(arguments));
			devices.add(device);
		}
		return devices;
	}

	private WebcamDevice createPICamDevice(int i, LinkedHashMap<String, String> map) {
		PICamDevice device=new PICamDevice(i);
		device.setParameters(map);
		return device;
	}
	@Override
	public boolean isThreadSafe() {
		return false;
	}

}
