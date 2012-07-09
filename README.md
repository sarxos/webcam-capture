# Java Webcam Capture

Allows you to use your PC webcam directly from Java.

[![Build Status](https://secure.travis-ci.org/sarxos/webcam-capture.png?branch=master)](http://travis-ci.org/sarxos/webcam-capture)

## I Want To Use It

Download complete ZIP from [here](http://www.sarxos.pl/repo/maven2/com/sarxos/webcam-capture/0.2/webcam-capture-0.2-dist.zip) 
and include ```webcam-capture-[version].jar``` in your's project classpath. This ZIP file contains sources, examples, all 
required dependencies and compiled library JAR.  

If you are a [Maven](http://maven.apache.org/) user you can also add this dependency to your project:

```xml
<dependency>
	<groupId>com.sarxos</groupId>
	<artifactId>webcam-capture</artifactId>
	<version>0.2</version>
</dependency>
```

For option 4 you have to also add ```<repository>``` to your pom (at least till it is unavailable from central):

```xml
<repository>
	<id>sarxos-repo</id>
	<url>http://www.sarxos.pl/repo/maven2</url>
</repository>
```

## Requirements

1. Java 5 (JRE or JDK) or higher installed
2. Webcam connected, installed and configured

## How To Make It Working

This library utilizes several webcam/video frameworks allowing it to gain access 
to web cameras available in the system. As for now it's working correctly with 
those data sources:

1. LTI-CIVIL - [download](http://lti-civil.org/download.php)
2. JMF (it cannot be used together with FMJ) - [download](http://www.oracle.com/technetwork/java/javase/download-142937.html)
3. FMJ (it cannot be used together with JMF) - [download](http://fmj-sf.net/downloads.php)

JMF and FMJ are replacements - so for both the same ```JMFdataSource``` data source should be used. 
Before you choose one of them let me briefly describe the differences:

### LTI-CIVIL

This is a Java library for capturing images from a video devices source such as a USB camera. It provides a 
simple API and does not depend on or use JMF. It's easy to use, LGPL-licensed, which means it can be 
redistributed along with your binaries (remember to include original license in your software).

To use it as webcam data source you have to download LTI-CIVIL binaries and add those files
into your project:

```
civil.dll
lti-civil-no_s_w_t.jar
```

Please note that civil.dll has to be stored in your project root directory. If you want to put it 
somewhere else you will have to change ```java.library.path``` setting passed as the argument 
to JVM executable.

Add below's code line somewhere in your ```main()``` method. Remember that data source has to be
registered before you start using webcam.

```java
Webcam.setDataSource(new CivilDataSource());
```

### JMF (Java Media Framework)

Supported by Oracle (previously Sun), it is really old, first version is from 1998 and 2.0 
has been released somewhere between 1999 - 2000. It has not been updated for years. It's very 
fast and reliable, however it cannot be redistributed, so if you would like to create pack
where you would like to include all required binaries, you unfortunately cannot do that with JMF. 

[Download JMF](http://www.oracle.com/technetwork/java/javase/download-142937.html) and  install
it on your computer. Add JMF JARs to the classpath (if not yet there), and set new data source: 

```java
Webcam.setDataSource(new JMFDataSource());
```

### FMJ (aka Freedom for Media in Java)

FMJ is an open-source project with the goal of providing an alternative to Java Media Framework 
(JMF), while remaining API-compatible with JMF. It aims to produce a single API/Framework which 
can be used to capture, playback, process, and stream media across multiple platforms. It's compatible
with JMF, but to make it working you have to be really patient - there are many binary dependencies
which are not so trivial t resolve (e.g. avformat-51 from ffmpeg is required).

To run _webcam-capture_ with FMJ you will have to [download it](http://fmj-sf.net/downloads.php) and
configure (what is not so trivial).

As FMJ API is compatible with JMF you will have to use the same data source:

```java
Webcam.setDataSource(new JMFDataSource());
```

### QTJ (QuickTime for Java)

Not yet supported...

### TWAIN

Not yet supported...

### DSJ (DirectShow for Java)

Not yet supported...

### JavaCV (Java binding for Open Computer Vision aka OpenCV)

Not yet supported...

## How To Use It

To get image and save it to disk:

```java
Webcam webcam = Webcam.getWebcams().getDefault();
webcam.open();
ImageIO.write(webcam.getImage(), "JPG", new File("my-webcam-picture.jpg")); // it will be created in project directory
webcam.close();
```

If you have more then one webcam connected to your computer:

```java
Webcam laptop = Webcam.getWebcams().get(0); // first one will be usually build-in one
Webcam kitchen = Webcam.getWebcams().get(1);
```

To display images from webcam in JPanel:

```java
JPanel panel = new WebcamPanel(webcam); // use panel somehow (as content pane, as subcomponents, etc)
```

To detect motion with your webcam - loop solution:

```java
WebcamMotionDetector detector = new WebcamMotionDetector(Webcam.getDefault());
detector.setInterval(100); // one check per 100 ms
detector.start();

while (true) {
	if (detector.isMotion()) {
		System.out.println("Detected motion I, alarm turn on you have");
	}
	Thread.sleep(500);
}
```

To detect motion with webcam - listener solution:

```java
public class DetectMotionExample implements WebcamMotionListener {

	public DetectMotionExample() {
		WebcamMotionDetector detector = new WebcamMotionDetector(Webcam.getDefault());
		detector.setInterval(100); // one check per 100 ms
		detector.addMotionListener(this);
		detector.start();
	}

	@Override
	public void motionDetected(WebcamMotionEvent wme) {
		System.out.println("Detected motion I, alarm turn on you have");
	}

	public static void main(String[] args) throws IOException {
		new DetectMotionExample();
		System.in.read(); // keeps your program open
	}
}
```

Logging (Logback via SLF4J) is already there, so you can enable it simply by adding 
```logback.xml``` configuration file somewhere in your filesystem and calling:

```java
WebcamLogConfigurator.configure("path/to/logback.xml");
```

There are more examples available in ```src/example```, don't forget to check!

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


