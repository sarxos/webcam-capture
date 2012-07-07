## Java Webcam Capture POC

Use your PC webcam directly from Java. Now you can use your webcam to take 
pictures anytime or stream video anywhere you want.

[![Build Status](https://secure.travis-ci.org/sarxos/webcam-capture.png?branch=master)](http://travis-ci.org/sarxos/webcam-capture)

### I Want To Use It

Of course, you can! If you would like to use _webcam-capture_ library in your project, you can either:

1. Download complete ZIP from [here](http://www.sarxos.pl/repo/maven2/com/sarxos/webcam-capture/0.2/webcam-capture-0.2-dist.zip) and include JAR in your classpath. This ZIP file contains sources, examples, all required dependencies and compiled library JAR.  
2. Clone git repo and add source to your project
3. Clone git repo and build it on your local machine (required [Maven](http://maven.apache.org/)) and then use build artifacts
4. Add [Maven](http://maven.apache.org/) dependency to your project:

```xml
<dependencies>
	<dependency>
		<groupId>com.sarxos</groupId>
		<artifactId>webcam-capture</artifactId>
		<version>0.2</version>
	</dependency>
</dependencies>
```

For option 4 you have to also add ```<repository>``` to your pom (at least till it is unavailable from central):

```xml
<repositories>
    <repository>
        <id>sarxos-repo</id>
        <url>http://www.sarxos.pl/repo/maven2</url>
    </repository>
</repositories>
```

### Requirements

1. Java 6 (JRE or JDK) or higher installed
2. Java Media Framework (JMF) 2.1.1e or higher installed
3. Webcam connected, installed and configured

### How It Works

This program utilizes JMF capabilities to read video streams from the webcam, so 
if JMF is not able to find device, program will end with exception. To check if
JMF _recognise_ your webcam you have to:

1. Open Start / Programs / Java Media Framework 2.1.1e
2. Start JMF Registry
3. Switch to _Capture Devices_ tab
4. Your webcam should be there - available on this small list. I have no idea what 
   to do if it is not available on the list... In such situation you can try to press 
   _Detect Capture Devices_ button, but if this won't help, you will have to google 
   for some help.   

### How To Use It

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


