## Java Webcam Capture POC

Use your PC webcam directly from Java. Now you can use your webcam to take 
pictures anytime or stream video anywhere you want.

[![Build Status](https://secure.travis-ci.org/sarxos/webcam-capture.png?branch=master)](http://travis-ci.org/sarxos/webcam-capture)

### Requirements

1. Java 7 (JRE or JDK) installed
2. Java Media Framework (JMF) 2.1.1 or higher installed
3. Webcam connected, installed and configured

### How It Works

This program utilizes JMF capabilities to read video streams from the webcam, so 
if JMF is not able to find device, program will end with exception. To check if
JMF _recognise_ your webcam you have to:

1. Open Start / Programs / Java Media Framework 2.1.1e
2. Start JMF Registry
3. Switch to _Capture Devices_ tab
4. Your webcam should be there - available on this small list. I have no idea what to do if it is not available on the list... In such situation you can try to press _Detect Capture Devices_ button, but if this won't help, you will have to google for some help.   

### How To Use It

To get image - the simplest way:

```java
// if you have only one webcam installed on your PC (e.g. laptop camera)
Webcam webcam = Webcam.getDefault();
webcam.open();

Image image = webcam.getImage();

webcam.close();
```

```java
// if you have many webcam installed on your PC (e.g. three web cameras connected to USB)
Webcam webcam1 = Webcam.getWebcams().get(0);
Webcam webcam2 = Webcam.getWebcams().get(1);
Webcam webcam3 = Webcam.getWebcams().get(2);
webcam1.open();
webcam2.open();
webcam3.open();

Image image1 = webcam1.getImage(); // share me on facebook
Image image2 = webcam2.getImage(); // send me to friend
Image image3 = webcam3.getImage(); // and this one... yeah, put me on the wall

webcam1.close();
webcam2.close();
webcam3.close();
```

To display view from webcam in JPanel:

```java
JPanel panel = new WebcamPanel(webcam);
// use panel somehow (as content pane, as subcomponents, etc)
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

There are more examples available in ```src/example```, don't forget to check!

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


