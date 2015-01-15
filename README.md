# Webcam Capture API for Java

This library allows you to use your build-in or external webcam directly from Java. It's designed to abstract commonly used camera features and support multiple capturing farmeworks.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.sarxos/webcam-capture/badge.svg)](http://search.maven.org/#artifactdetails|com.github.sarxos|webcam-capture|0.3.10|bundle)
[![Build Status](https://img.shields.io/travis/sarxos/webcam-capture.svg?branch=master)](http://travis-ci.org/sarxos/webcam-capture)
[![Coverage Status](https://img.shields.io/coveralls/sarxos/webcam-capture.svg?branch=master)](https://coveralls.io/r/sarxos/webcam-capture?branch=master)
[![Ohloh Stats](https://img.shields.io/badge/ohloh-%24%20243k%2019.7%20kNCSL-blue.svg)](https://www.ohloh.net/p/java-webcam-capture)

## Features

1. Simple, thread-safe and non-blocking API,
2. No additional software required,
3. Supports multiple platforms (Windows, Linux, Mac OS, etc) and various architectures (32-bit, 64-bit, ARM),
4. Get images from build-in or USB-connected PC webcams, 
5. Get images from IP / network cameras (as MJPEG or JPEG),
6. Offers ready to use motion detector,
7. All required JARs Available in Maven Central,
8. Offers possibility to expose images as MJPEG stream,
9. It is available as Maven dependency or standalone ZIP binary (with all dependencies included),
10. Swing component to display video feed from camera,
11. Swing component to choose camera (drop down),
12. Multiple capturing frameworks are supported:
  * [OpenIMAJ](http://www.openimaj.org/),
  * [LTI CIVIL](http://sourceforge.net/projects/lti-civil/),
  * [Java Media Framework (JMF)](http://www.oracle.com/technetwork/java/javase/tech/index-jsp-140239.html),
  * [Freedom for Media in Java (FMJ)](http://fmj-sf.net/),
  * [OpenCV](http://opencv.org/) via [JavaCV](https://github.com/bytedeco/javacv),
  * [VLC](http://www.videolan.org/vlc/) via [vlcj](http://www.capricasoftware.co.uk/projects/vlcj/index.html),
  * [GStreamer](http://gstreamer.freedesktop.org/) via [gstreamer-java](https://code.google.com/p/gstreamer-java/)
  * MJPEG IP Cameras,

## Raspberry PI

_(and other ARM devices)_

The lates version (0.3.10) does not work on ARM just out of the box. To make it working you need to replace version 0.6.2 of BridJ JAR by the [0.6.3-SNAPHOST](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.nativelibs4java&a=bridj&v=0.6.3-SNAPSHOT) or newer [bridj-0.7-20140918](http://maven.ecs.soton.ac.uk/content/groups/maven.openimaj.org/com/nativelibs4java/bridj/0.7-20140918/bridj-0.7-20140918.jar). Moreover, lately Jonathon Hare from OpenIMAJ team, found a problem described in [bridj #525](https://github.com/ochafik/nativelibs4java/issues/525) which causes problems on armhf architecture.

## Maven

The latest stable version is [available](http://search.maven.org/#artifactdetails|com.github.sarxos|webcam-capture|0.3.10|bundle) in Maven Central:

```xml
<dependency>
  <groupId>com.github.sarxos</groupId>
  <artifactId>webcam-capture</artifactId>
  <version>0.3.10</version>
</dependency>
```

Snapshot version:

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
	<version>0.3.11-SNAPSHOT</version>
</dependency>
```

## Download

The newest stable version can be downloaded as separated ZIP binary. This ZIP file contains Webcam Capture API itself and all required dependencies (in ```libs``` directory). Click on the below link to download it:

 [webcam-capture-0.3.10-dist.zip](https://github.com/sarxos/webcam-capture/releases/download/webcam-capture-parent-0.3.10/webcam-capture-0.3.10-dist.zip)

The latest development version JAR (aka SNAPSHOT) can be downloaded [here](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.github.sarxos&a=webcam-capture&v=0.3.11-SNAPSHOT).

## Contribution

If you have strong will, spare time, knownledge or even some small amount of money you would like to spent for good purpose you can help developing this awesome Webcam Capture API and make it even better! Several kinds of contributions are very welcome:

##### Star Project

If you think this project is great, you would like to help, but you don't know how - you can become project's stargazer. By starring you're making project more popular. Visit [this](https://github.com/blog/1204-notifications-stars) link if you would like to learn more about how notifications and stars works on Github.

##### Report Bug or Feature

If you've found a bug or you've came-up with some fantastic feature which can make Webcam Capture a better API to use, don't hesitate to [create new issue](https://github.com/sarxos/webcam-capture/issues/new) where you can describe in details what the problem is, or what would you like to improve.

##### Perform Tests

Since Webcam Capture use some part of native code, it's very hard to cover all supported operating systems. I'm always testing it on 64-bit Ubuntu Linux, Windows XP and Vista (both 32-bit), but I have no possibility to test on Raspberry Pi, Mac OS and 32-bit Linux. Please help and test on those systems if you have such possibility. 

##### Write Code

If you know Java or C++ you can help developing Webcam Capture by forking repository and sending pull requests. Please visit [this](http://stackoverflow.com/questions/4384776/how-do-i-contribute-to-others-code-in-github) link if you don't know how to contribute to other's code at Github. 

##### Donate

People have expressed a wish to donate a little money. Donating won't get you anything special, other than a warm feeling inside, and possibly urge me to produce more freely available material for Webcam Capture project. You can donate via [PayPal](https://www.paypal.com), just click [donate](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=UYMENK76CSYZU) button available below - it will redirect you to the secured PayPal page where you can provide donation amount (there is no minimal value).

[![Donate via PayPal](https://www.paypalobjects.com/en_US/GB/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=UYMENK76CSYZU)

## Hello World

Code below will capture image from your default webcam and save it in ```hello-world.png``` file:

```java
Webcam webcam = Webcam.getDefault();
webcam.open();
ImageIO.write(webcam.getImage(), "PNG", new File("hello-world.png"));
```

## More Examples!

Below are the very pretty basic examples demonstrating of how Webcam Capture API can be used in the Java code. All can be found in the project source code. Please note that some of those examples may use the newest API which has not yet been released to maven Central. In such a case please make sure you are using the newest Webcam Capture API SNAPSHOT.

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

Imagine situation when you depend on some framework, but suddenly have to drop it and use different one (e.g. replace archaic JMF with newest GStreamer). By doing this one have to rewrite significant piece of code because new framework is completely incompatible with previous one. Here Webcam Capture API comes to help you! This library has been created to remove the burden of situation when you would like to write your application with intention to replace capturing framework somewhere in the future.

Webcam Capture API defined ```WebcamDriver``` interface which has been already implemented in several _capturing drivers_ build on top of well-known frameworks used to work with multimedia and cameras. Complete list can be found below. 

By default, Webcam Capture library uses default driver which consists of small, refined part of awesome [OpenIMAJ](http://sourceforge.net/p/openimaj/home/OpenIMAJ/) framework wrapped in thread-safe container which allows it to be used in multithreaded applications. However there are more ready-to-use drivers which can be used as a replacement or addition to the default one. By utilizing those drivers Webcam Capture can be extended with various new features (e.g. IP camera support). 

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

I initially started working on Webcam Capture as a simple proof-of-concept after I read [Andrew Davison](http://fivedots.coe.psu.ac.th/~ad/)'s fantastic book entitled [Killer Game Programming](http://www.amazon.com/Killer-Game-Programming-Andrew-Davison/dp/0596007302/ref=sr_1_1?s=books&ie=UTF8&qid=1360352393&sr=1-1&keywords=killer+game+programming) (which is also available [online](http://fivedots.coe.psu.ac.th/~ad/jg/)). Thank you Andrew! Later I found that there is a complete mess in Java APIs allowing you to capture images from webcams. Once you choose specific API you cannot change it without modifying large parts of the code. I decided to change this situation and write general purpose wrapper for various different APIs (like JMF, OpenCV, OpenIMAJ, LTI-CIVIL, VLC). In such a way, Webcam Capture as we know it today, was brought to life. Today you can change underlying frameworks just by replacing webcam driver (one line code change). If there is no driver for particular framework, it's very easy to write it yourself.

## License

Copyright (C) 2012 - 2015 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

![SarXos](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture/src/etc/resources/sarxos.png "SarXos")
