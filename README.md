# Java Webcam Capture

This library allows you to use your build-in or external webcam directly from Java.

[![Build Status](https://secure.travis-ci.org/sarxos/webcam-capture.png?branch=master)](http://travis-ci.org/sarxos/webcam-capture) [![Coverage Status](https://coveralls.io/repos/sarxos/webcam-capture/badge.png?branch=master)](https://coveralls.io/r/sarxos/webcam-capture?branch=master) [![Ohloh Stats](https://www.ohloh.net/p/java-webcam-capture/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/java-webcam-capture)

## Features

* Simple, thread-safe and non-blocking API,
* No additional software required,
* Supports multiple platforms (Windows, Linux, Mac OS, etc) and various architectures (32-bit, 64-bit, ARM),
* Stream images from build-in or USB-connected PC webcams, 
* Stream images from IP / network cameras (as MJPEG or JPEG),
* Detect motion,
* All required JARs Available in Maven Central,
* Can re-stream images as MJPEG,
* Available as standalone ZIP binaries with all dependencies included,
* Supports additional video grabbing drivers (such as OpenIMAJ, LTI-CIVIL, JMF, FMJ, OpenCV, VLC, IP Camera),
* Ready to use Swing component designed to display image from webcam / IP / network camera,

## Download

Below is the newest stable version ZIP containing main project
JAR with additional documents, examples and all required 3rd-party
dependencies:

* **Latest stable version** - [webcam-capture-0.3.10-RC7-dist.zip](https://github.com/sarxos/webcam-capture/releases/download/webcam-capture-parent-0.3.10-RC7/webcam-capture-0.3.10-RC7-dist.zip)

Other releases:

* Previous stable version - [webcam-capture-0.3.9-dist.zip](http://www.sarxos.pl/repo/maven2/com/github/sarxos/webcam-capture/0.3.9/webcam-capture-0.3.9-dist.zip)
* List of all releases ever made - [click](http://www.sarxos.pl/repo/maven2/index.php?dir=com%2Fgithub%2Fsarxos%2Fwebcam-capture%2F)

The latest development version JAR (aka SNAPSHOT) can be downloaded [here](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.github.sarxos&a=webcam-capture&v=0.3.10-SNAPSHOT).

## Maven

For those who use Maven and would like to add Webcam Capture dependency
in their own projects.

#### Official Release

```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture</artifactId>
	<version>0.3.9</version>
</dependency>
```

#### Release Candidate

```xml
<repository>
	<id>SarXos Repository</id>
	<url>http://www.sarxos.pl/repo/maven2</url>
</repository>
```
```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture</artifactId>
	<version>0.3.10-RC7</version>
</dependency>
```

#### Snapshot Version

```xml
<repository>
	<id>Sonatype OSS Snapshot Repository</id>
	<url>http://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```
```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture</artifactId>
	<version>0.3.10-SNAPSHOT</version>
</dependency>

```

If you would like to use the newest SNAPSHOT version, please be aware - 
sometimes it may be unstable. If you are OK with this, just add the above
repository and dependency into your ```pom.xml```.

## Contribution

If you have strong will, spare time, knownledge or even some small amount of
money you would like to spent for good purpose you can help developing this 
awesome Webcam Capture API and make it even better! Several kinds of 
contributions are very welcome:

##### Star Project

If you think this project is great, you would like
to help, but you don't know how - you can become project's stargazer.
By starring you're making project more popular. Visit [this](https://github.com/blog/1204-notifications-stars)
link if you would like to learn more about how notifications and stars 
works on Github.

##### Report Bug or Feature

If you've found a bug or you've came-up with some fantastic feature which 
can make Webcam Capture a better API to use, don't hesitate to 
[create new issue](https://github.com/sarxos/webcam-capture/issues/new) 
where you can describe in details what the problem is, or what would you
like to improve.

##### Perform Tests

Since Webcam Capture use some part of native code, it's very 
hard to cover all supported operating systems. I'm always testing it
on 64-bit Ubuntu Linux, Windows XP and Vista (both 32-bit), but I
have no possibility to test on Raspberry Pi, Mac OS and 32-bit Linux.
Please help and test on those systems if you have such possibility. 

##### Write Code

If you know Java or C++ you can help developing Webcam Capture by 
forking repository and sending pull requests. Please visit [this](http://stackoverflow.com/questions/4384776/how-do-i-contribute-to-others-code-in-github)
link if you don't know how to contribute to other's code at Github. 

##### Donate

People have expressed a wish to donate a little money. Donating won't 
get you anything special, other than a warm feeling inside, and possibly 
urge me to produce more freely available material for Webcam Capture 
project. You can donate via [PayPal](https://www.paypal.com), just click 
[donate](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=UYMENK76CSYZU)
button available below - it will redirect you to the secured 
PayPal page where you can provide donation amount (there is no minimal 
value).

[![Donate via PayPal](https://www.paypalobjects.com/en_US/GB/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=UYMENK76CSYZU)


## Hello World

Code below will capture image from your default webcam and save it in ```hello-world.png``` file:

```java
Webcam webcam = Webcam.getDefault();
webcam.open();
ImageIO.write(webcam.getImage(), "PNG", new File("hello-world.png"));
```

## More Examples!

Below is the list of pretty basic examples. All can be found in the 
project source code. Please note that some of those examples are using 
the newest API which possibly has not yet been released. In such a case
please make sure you are using the newest Webcam Capture SNAPSHOT version.

* [How to detect webcam](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/DetectWebcamExample.java)
* [How to take picture and save to file](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/TakePictureExample.java)
* [How to display image from webcam in Swing panel (basic)](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/WebcamPanelExample.java)
* [How to display image from webcam in Swing panel (more advanced)](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/WebcamViewerExample.java)
* [How to listen on camera connection / disconnection events](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/WebcamDiscoveryListenerExample.java)
* [How to configure capture resolution](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/TakePictureDifferentSizeExample.java)
* [How to configure non-standard capture resolutionj (e.g. HD720)](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/CustomResolutionExample.java)
* [How to save captured image in PNG / JPG / GIF / BMP etc](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/DifferentFileFormatsExample.java)
* [How to capture with many parallel threads](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/ConcurrentThreadsExample.java)
* [How to detect motion (text mode only)](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/DetectMotionExample.java)
* [How to display images from multiple IP cameras exposing pictures in JPG format](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-drivers/webcam-capture-driver-ipcam/src/examples/java/JpegDasdingStudioExample.java)
* [How to display image from IP camera exposing MJPEG stream](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-drivers/webcam-capture-driver-ipcam/src/examples/java/MjpegLignanoBeachExample.java)
* [How to use composite driver to display both, build-in and IP camera images](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-drivers/webcam-capture-driver-ipcam/src/examples/java/DualNativeAndMjpegWebcamExample.java)

And here are some more advanced examples, few with quite fancy GUI.

* [How to detect and mark human faces](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-detect-face)
* [How to detect motion and display effect in JFrame](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-examples/webcam-capture-motiondetector)
* [How to use webcam capture in Java Applet](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-applet)
* [How to paint custom effects in WebcamPanel displaying image from camera](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-painter)
* [How to read QR / DataMatrix and Bar codes](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-qrcode)
* [How to record video from webcam](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-examples/webcam-capture-video-recording/src/main/java/com/github/sarxos/webcam/Encoder.java)
* [How to transcode webcam images into live h264 stream](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-live-streaming)
* [How to use Webcam Capture API in JavaFX](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-javafx)
* [How to use Webcam Capture API in JavaFX and FXML](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-javafx-fxml)

## Capture Drivers

Imagine situation when you depend on some framework, but suddenly have to 
drop it and use different one (e.g. replace archaic JMF with newest GStreamer).
By doing this one have to rewrite significant piece of code because new framework is 
completely incompatible with previous one. Here Webcam Capture API comes to help 
you! This library has been created to remove the burden of situation when you
would like to write your application with intention to replace capturing framework
somewhere in the future.

Webcam Capture API defined ```WebcamDriver``` interface which has been already 
implemented in several _capturing drivers_ build on top of well-known frameworks
used to work with multimedia and cameras. Complete list can be found below.

By default, Webcam Capture library uses default driver which consists of small,
refined part of awesome [OpenIMAJ](http://sourceforge.net/p/openimaj/home/OpenIMAJ/) 
framework wrapped in thread-safe container which allows it to be used in 
multithreaded applications. 
However there are more ready-to-use drivers which can be used as a replacement 
or addition to the default one. By utilizing those drivers Webcam Capture can 
be extended with various new features (e.g. IP camera support).

List of additional capture drivers includes:

| Driver Name     | Stable | Central | Description                             |
|-----------------|--------|---------|-----------------------------------------|
| [ipcam][]       | yes    | yes     | IP / network camera driver              |
| [gstreamer][]  | yes    | no      | Driver for [GStreamer][] framework      |
| [openimaj][]   | yes    | no      | Driver for [OpenIMAJ][] framework       |
| [v4l4j][]      | yes    | no      | Driver for [V4L4j][] project            |
| [jmf][]        | yes    | yes     | Driver for [JMF][] / [FMJ][] frameworks |
| [lti-civil][]  | yes    | yes     | Driver for [LTI-CIVIL][] library        |
| [javacv][]     | no     | no      | Driver for [JavaCV][] library           |
| [vlcj][]       | no     | no      | Driver for [VLCj][] library             |
| [ffmpeg-cli][] | exp    | no      | Driver for [FFmpeg][] [CLI][] tool      |

* Central = Maven Central Repository
* _exp_ = experimental

[GStreamer]:  http://gstreamer.freedesktop.org/
[OpenIMAJ]:   http://www.openimaj.org/
[V4L4j]:      http://code.google.com/p/v4l4j/
[JMF]:        http://www.oracle.com/technetwork/java/javase/download-142937.html
[FMJ]:        http://fmj-sf.net/
[LTI-CIVIL]:  http://lti-civil.org/
[JavaCV]:     http://code.google.com/p/javacv/
[VLCj]:       http://code.google.com/p/vlcj/
[FFmpeg]:     http://www.ffmpeg.org/
[CLI]:        http://en.wikipedia.org/wiki/Command-line_interface

[ipcam]:      https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-ipcam
[gstreamer]:  https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-gstreamer
[openimaj]:   https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-openimaj
[v4l4j]:      https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-v4l4j
[jmf]:        https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-jmf
[lti-civil]:  https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-lti-civil
[javacv]:     https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-javacv
[vlcj]:       https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-vlcj
[ffmpeg-cli]: https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/driver-ffmpeg-cli

## History

I initially started working on Webcam Capture as a simple proof-of-concept after 
I read [Andrew Davison](http://fivedots.coe.psu.ac.th/~ad/)'s fantastic book entitled
[Killer Game Programming](http://www.amazon.com/Killer-Game-Programming-Andrew-Davison/dp/0596007302/ref=sr_1_1?s=books&ie=UTF8&qid=1360352393&sr=1-1&keywords=killer+game+programming)
(which is also available [online](http://fivedots.coe.psu.ac.th/~ad/jg/)). Thank you Andrew! 
Later I found that there is a complete mess in Java APIs allowing you to capture images
from webcams. Once you choose specific API you cannot change it without modifying 
large parts of the code. I decided to change this situation and write general purpose
wrapper for various different APIs (like JMF, OpenCV, OpenIMAJ, LTI-CIVIL, VLC).
In such a way, Webcam Capture as we know it today, was brought to life. Today you 
can change underlying frameworks just by replacing webcam driver (one line code 
change). If there is no driver for particular framework, it's very easy to write it
yourself.

## License

Copyright (C) 2012 - 2013 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

![SarXos](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture/src/etc/resources/sarxos.png "SarXos")
