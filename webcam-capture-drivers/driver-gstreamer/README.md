# webcam-capture-driver-gstreamer

This is capture driver which gives Webcam Capture API possibility to use
[GStreamer](http://gstreamer.freedesktop.org/documentation/gstreamer010.html)
as a middleware accessing webcam devices (build-in or USB enabled).

It has been designed to work with Windows and Linux **only**. To make use of it user have to 
[download](http://code.google.com/p/ossbuild/) GStreamer application installer 
on Windows or use _apt-get_, _yum_ or other packages manager to install it on Linux.

Currently supported GStreamer version is 0.10.x, so make sure you are installing
the correct one! It is **not** compatible with GStreamer 1.0 and above!

## Supported Platforms

* Windows
* Linux

## Download

The latest **development** version JAR (aka SNAPSHOT) can be downloaded [here](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.github.sarxos&a=webcam-capture-driver-gstreamer&v=0.3.10-SNAPSHOT).

The latest **stable** version ZIP bundle can be downloaded [here](http://repo.sarxos.pl/maven2/com/github/sarxos/webcam-capture-driver-gstreamer/0.3.10-RC7/webcam-capture-driver-gstreamer-0.3.10-RC7-dist.zip).

## Maven

Stable:

```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture-driver-gstreamer</artifactId>
	<version>0.3.10</version>
</dependency>
```

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
    <artifactId>webcam-capture-driver-gstreamer</artifactId>
    <version>0.3.11-SNAPSHOT</version>
</dependency>
```

## Example

```java
static {
	Webcam.setDriver(new GStreamerDriver());
}

public static void main(String[] args) {
	JFrame frame = new JFrame("GStreamer Webcam Capture Driver Demo");
	frame.add(new WebcamPanel(Webcam.getDefault()));
	frame.pack();
	frame.setVisible(true);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
```

## License

Copyright (C) 2012 - 2014 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

