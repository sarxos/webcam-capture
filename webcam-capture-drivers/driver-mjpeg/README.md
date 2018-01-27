# webcam-capture-driver-mjpeg

This is capture driver which is able to capture video feed from MJPEG stream. But how to create MJPEG stream? This can be done with GStreamer, FFMpeg or other tool. Streaming can be done by TCP, UDP, or even named pipe (FIFO).

### Streaming With GStreamer

Here is example of how GStreamer 1.x can be used to stream video feed as MJPEG on Raspberry Pi
or any other Linux system with gStreamer 1.x installed. This command should be executed on the
host computer (i.e. the server with connected webcam and from where we want to stream video feed).

```plain
$ gst-launch-1.0 -v v4l2src device=/dev/video0 \
  ! capsfilter caps="video/x-raw, width=640, height=480" \
  ! decodebin name=dec ! videoconvert ! jpegenc \
  ! multipartmux ! tcpserversink host=0.0.0.0 port=5000
``` 

The above will `/dev/video0` device (default webcam) to get video from and convert
individual frames to JPEG and then perform MIME multipart encoding to make it finally available
over TCP on port `5000`.

Expected command output is something like this:

```plain
$ gst-launch-1.0 -v v4l2src device=/dev/video0 ! decodebin name=dec ! videoconvert ! jpegenc ! multipartmux ! tcpserversink host=0.0.0.0 port=5000
Setting pipeline to PAUSED ...
/GstPipeline:pipeline0/GstTCPServerSink:tcpserversink0: current-port = 5000
Pipeline is live and does not need PREROLL ...
Setting pipeline to PLAYING ...
New clock: GstSystemClock
/GstPipeline:pipeline0/GstV4l2Src:v4l2src0.GstPad:src: caps = video/x-raw, format=(string)YUY2, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, interlace-mode=(string)progressive, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstDecodeBin:dec.GstGhostPad:sink.GstProxyPad:proxypad0: caps = video/x-raw, format=(string)YUY2, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, interlace-mode=(string)progressive, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstDecodeBin:dec/GstTypeFindElement:typefind.GstPad:src: caps = video/x-raw, format=(string)YUY2, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, interlace-mode=(string)progressive, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstDecodeBin:dec/GstTypeFindElement:typefind.GstPad:sink: caps = video/x-raw, format=(string)YUY2, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, interlace-mode=(string)progressive, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstDecodeBin:dec.GstGhostPad:sink: caps = video/x-raw, format=(string)YUY2, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, interlace-mode=(string)progressive, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstVideoConvert:videoconvert0.GstPad:src: caps = video/x-raw, format=(string)YUY2, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, interlace-mode=(string)progressive, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstJpegEnc:jpegenc0.GstPad:sink: caps = video/x-raw, format=(string)YUY2, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, interlace-mode=(string)progressive, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstVideoConvert:videoconvert0.GstPad:sink: caps = video/x-raw, format=(string)YUY2, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, interlace-mode=(string)progressive, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstDecodeBin:dec.GstDecodePad:src_0.GstProxyPad:proxypad1: caps = video/x-raw, format=(string)YUY2, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, interlace-mode=(string)progressive, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstJpegEnc:jpegenc0.GstPad:src: caps = image/jpeg, sof-marker=(int)0, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstMultipartMux:multipartmux0.GstPad:sink_0: caps = image/jpeg, sof-marker=(int)0, width=(int)1280, height=(int)720, pixel-aspect-ratio=(fraction)1/1, framerate=(fraction)10/1
/GstPipeline:pipeline0/GstMultipartMux:multipartmux0.GstPad:src: caps = multipart/x-mixed-replace, boundary=(string)ThisRandomString
/GstPipeline:pipeline0/GstTCPServerSink:tcpserversink0.GstPad:sink: caps = multipart/x-mixed-replace, boundary=(string)ThisRandomString
```

You can break streaming by pressing `Ctrl + C` or by closing your terminal. The best way to leave streaming open after terminal is closed is to use `nohup`, for example:

```plain
$ nohup gst-launch-1.0 -v v4l2src device=/dev/video0 ! \
  decodebin name=dec ! videoconvert ! \
  jpegenc ! multipartmux ! \
  tcpserversink host=0.0.0.0 port=5000 &
``` 

## Maven

<!--
Stable:

```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture-driver-mjpeg</artifactId>
	<version>0.3.12</version>
</dependency>
```
-->

Snapshot:

```xml
<repository>
    <id>Sonatype OSS Snapshot Repository</id>
    <url>http://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.sarxos</groupId>
    <artifactId>webcam-capture-driver-mjpeg</artifactId>
    <version>0.3.13-SNAPSHOT</version>
</dependency>
```

## How To Use

Set capture driver before you start using Webcam class:

```java
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.mjpeg.MjpegCaptureDriver;


public class WebcamPanelExample {

	static {
		Webcam.setDriver(new MjpegCaptureDriver()
			.withUri("tcp://127.0.0.1:5000") // this is your local host
			.withUri("tcp://192.168.1.12:5000")); // this is some remote host
	}

	public static void main(String[] args) throws InterruptedException {

		// run this from your terminal (as a single line, and remove dollar sign):

		// $ gst-launch-1.0 -v v4l2src device=/dev/video0 ! capsfilter caps="video/x-raw, width=320,
		// height=240" ! decodebin name=dec ! videoconvert ! jpegenc ! multipartmux ! tcpserversink
		// host=0.0.0.0 port=5000

		final Webcam webcam = Webcam.getDefault();

		final WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(true);
		panel.setImageSizeDisplayed(true);
		panel.setMirrored(true);

		final JFrame window = new JFrame("MJPEG Streaming From TCP Socket");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
}
```

## Capture Driver License

Copyright (C) 2012 - 2018 Bartosz Firyn <https://github.com/sarxos> and contributors

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

