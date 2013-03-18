# Java Webcam Capture

This library allows you to use your build-in or external webcam directly from Java.

Complete documentation, API, examples, tutorials and many more can be found here:

[http://webcam-capture.sarxos.pl/](http://webcam-capture.sarxos.pl/)


[![Build Status](https://secure.travis-ci.org/sarxos/webcam-capture.png?branch=master)](http://travis-ci.org/sarxos/webcam-capture)

## Features

* Simple, thread-safe and non-blocking API,
* No additional software required,
* Supports multiple platforms (Windows, Linux, Mac OS, etc) and various architectures (32-bit, 64-bit, ARM),
* Stream images from build-in or USB-connected PC webcams, 
* Stream images from IP / network cameras,
* Detect motion,
* All required JARs Available in Maven Central,
* Can re-stream images as MJPEG,
* Also available as standalone ZIP binaries with all dependencies included,
* Supports additional video grabbing drivers (such as OpenIMAJ, LTI-CIVIL, JMF, FMJ, OpenCV, VLC, IP Camera),
* Ready to use Swing component designed to display image from webcam / IP / network camera,

## Download

Below you can find links to the ZIP files containing Webcam Capture 
JAR with all required 3rd-party dependencies.

* Latest stable version: [webcam-capture-0.3.9-dist.zip](http://www.sarxos.pl/repo/maven2/com/github/sarxos/webcam-capture/0.3.9/webcam-capture-0.3.9-dist.zip)
* Latest SNAPSHOT version: [webcam-capture-0.3.10-SNAPSHOT-dist.zip](http://www.sarxos.pl/repo/maven2/com/github/sarxos/webcam-capture/0.3.10-SNAPSHOT/webcam-capture-0.3.10-SNAPSHOT-dist.zip)
* Other releases: [list of all releases ever made](http://www.sarxos.pl/repo/maven2/index.php?dir=com%2Fgithub%2Fsarxos%2Fwebcam-capture%2F)

## Maven

```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture</artifactId>
	<version>0.3.9</version>
</dependency>
```

You can also use the newest SNAPSHOT version, but please be aware - 
sometimes it can be unstable. Add the following repository into 
your POM, and replace used dependency if you decide to take that risk.

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

## Contribution

If you have spare time, knownledge or some small amount of money you 
can help developing 
awesome Webcam Capture API and make it even better! Several kinds of 
contributions are very welcome:

* **Star Project** - star the project and become a stargazer. This will make project
more popular.
* **Report bug or feature** - If you've found a bug or you've came-up with some fantastic feature which 
can make Webcam Capture a better API to use, don't hesitate to 
[create new issue](https://github.com/sarxos/webcam-capture/issues/new) 
where you can describe in details what the problem is, or what would you
like to improve.
* **Perform tests** - Since Webcam Capture use some part of native code, it's very 
hard to cover all supported operating systems. I'm always testing it
on 64-bit Ubuntu Linux, Windows XP and Vista (both 32-bit), but I
have no possibility to test on Raspberry Pi, Mac OS and 32-bit Linux.
Please help and test on those systems if you have such possibility. 
* **Write code** - If you know Java or C++ you can help developing Webcam Capture by 
forking repository and sending pull requests.
* **Donate** - People have expressed a wish to donate a little money. Donating won't 
get you anything special, other than a warm feeling inside, and possibly 
urge me to produce more freely available material for Webcam Capture 
project. You can donate via PayPal, find _donate_ button available 
[here](http://webcam-capture.sarxos.pl/#contribute) on the project page.


## Hello World

Code below will capture image from your default webcam and save it in ```hello-world.png``` file:

```java
Webcam webcam = Webcam.getDefault();
webcam.open();
ImageIO.write(webcam.getImage(), "PNG", new File("hello-world.png"));
```

## More Examples

Below is the list of pretty basic examples. All can be found in the 
project source code. Please note that some of those examples are using 
the newest API which possibly has not yet been released. In such a case
please make sure you are using the newest Webcam Capture SNAPSHOT version.

* [How to detect webcam](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/DetectWebcamExample.java)
* [How to take picture and save to file](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/TakePictureExample.java)
* [How to take pictures from two cameras and save to files](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/TakePictureFromTwoCamsExample.java)
* [How to display image from webcam in Swing panel (basic)](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/WebcamPanelExample.java)
* [How to display image from webcam in Swing panel (more advanced)](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/WebcamViewerExample.java)
* [How to listen on camera connection / disconnection events](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/WebcamDiscoveryListenerExample.java)
* [How to configure capture resolution](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/TakePictureDifferentSizeExample.java)
* [How to configure non-standard capture resolutionj (e.g. HD720)](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/CustomResolutionExample.java)
* [How to save captured image in PNG / JPG / GIF / BMP etc](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/DifferentFileFormatsExample.java)
* [How to capture with many parrallel threads](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/ConcurrentThreadsExample.java)
* [How to detect motion (text mode only)](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture/src/example/java/com/github/sarxos/webcam/example/DetectMotionExample.java)

And here are some more advanced examples, few with quite fancy GUI.

* [How to detect motion and display effect in JFrame](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-examples/webcam-capture-motiondetector)
* [How to use webcam capture in Java Applet](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-applet)
* [How to paint custom effects in WebcamPanel displaying image from camera](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-painter)
* [How to read QR / DataMatrix and Bar codes](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-qrcode)
* [How to record video from webcam](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-record-video)
* [How to transcode webcam images into live h264 stream](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-live-streaming)


## Capture Drivers

By default Webcam Capture use default driver which consists of small, refined
part of awesome [OpenIMAJ](http://sourceforge.net/p/openimaj/home/OpenIMAJ/) 
framework wrapped in thread-safe container which allows it to be used in 
multithreaded applications. 
However there are more ready-to-use drivers which can be used as a replacement 
or addition to the default one. By utilizing those drivers Webcam Capture can 
be extended with various new features (e.g. IP camera support).

List of additional drivers includes (stable):

* [IP Camera Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/webcam-capture-driver-ipcam) - available in Maven Central, adds IP / network cameras support
* [JMF Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/webcam-capture-driver-jmf) - available in Maven Central, JMF replacement for default driver
* [LTI-CIVIL Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/webcam-capture-driver-lti-civil) - available in Maven Central, Windows, Linux, Mac OS, LTI-CIVIL replacement for default driver
* [OpenIMAJ Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/webcam-capture-driver-openimaj) - **not** available in Maven Central, OpenIMAJ replacement for default driver

And (unstable, experimental):

* [OpenCV Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/webcam-capture-driver-javacv) - **unstable**, OpenCV replacement for default driver
* [VLC Driver](https://github.com/sarxos/webcam-capture/tree/master/wwebcam-capture-drivers/ebcam-capture-driver-vlcj) - **unstable**, Linux only, VLC replacement for default driver
* [FFmpeg Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/webcam-capture-driver-ffmpeg-cli) - **experimental**, Linux only, FFmpeg replacement for default driver

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

![SarXos](https://raw.github.com/sarxos/webcam-capture/master/sarxos.png "SarXos")


