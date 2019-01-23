# webcam-capture-driver-raspistill

This capture driver is special for raspberrypi. __raspistill__ is the command line tool on raspberrypi for capturing still photographs with the camera module. blash...., Ok, The most important is that command line tool has feature grabbing image repeatly(mock FPS) from camera and print to file or console. They are:

1.  -o, --output : Output filename <filename> (to **write to stdout, use '-o -**'). If not specified, no file is saved
2.  -tl, --timelapse : Timelapse mode. Takes a picture every <t>ms
3.  -n, --nopreview : Do not display a preview window

so it is possible use java Runtime to launch raspistill process and intercept its console output, makes it as
webcam driver. this is one simple and straightforward approach without native JNI or JNA call, no file system exchange. You will not struggle with performance issue and native code compiling. Enjoy it! 


## Maven

```xml
<!--If you want to access snapshot repo, please add this to your pom -->
<repository>
    <id>Sonatype OSS Snapshot Repository</id>
    <url>http://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
<dependency>
    <groupId>com.github.sarxos</groupId>
    <artifactId>webcam-capture-driver-raspistill</artifactId>
    <version>${your version}</version>
</dependency>
```

## How To Use

Set capture driver before you start using Webcam class.

```java
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.gstreamer.ScreenCaptureDriver;


public class WebcamPanelExample {

	static {
		Webcam.setDriver(new RaspistillDriver());
	}

	public static void main(String[] args) throws InterruptedException {

		final JFrame window = new JFrame("Screen Capture Example");
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new FlowLayout());

		final Dimension resolution = WebcamResolution.QVGA.getSize();

		for (final Webcam webcam : Webcam.getWebcams()) {

			webcam.setCustomViewSizes(resolution);
			webcam.setViewSize(resolution);
			webcam.open();

			final WebcamPanel panel = new WebcamPanel(webcam);
			panel.setFPSDisplayed(true);
			panel.setDrawMode(DrawMode.FIT);
			panel.setImageSizeDisplayed(true);
			panel.setPreferredSize(resolution);

			window.getContentPane().add(panel);
		}

		window.pack();
		window.setVisible(true);
	}
}
```
The picture below is an effect of the above code running. 

![](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-drivers/driver-raspistill/src/etc/resources/screen-1.jpg?raw=true)

## Known Problems

##Raspistill Arguments
```Raspberry Pi Camera Board - RaspiStill Command List
raspistill Camera App v1.3.11

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
```
## Driver Parameters
this driver makes above arguments configurable, when driver instance created, it will load arguments step by step as follows,
1. load built-in parameters in defaults.properties packed in jar. one line one argument
2. search raspistill.properties in classpath root, if exists, will merge to default.properties.
3. search system properties which starts with raspistill. then merge to properties
4. RaspistillDriver class is builder design pattern, you can change configurations when create new driver instance, code as *new RaspistillDriver().width(600).height(400)....*

## Capture Driver License

Copyright (C) 2012 - 2019 Bartosz Firyn <https://github.com/sarxos> and contributors

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

