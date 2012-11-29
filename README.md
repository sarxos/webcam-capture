# Java Webcam Capture

This library allows you to use your build-in or external webcam directly from Java.

Complete documentation, API, examples, tutorials and many more can be found here:

[http://webcam-capture.sarxos.pl/](http://webcam-capture.sarxos.pl/)


[![Build Status](https://secure.travis-ci.org/sarxos/webcam-capture.png?branch=master)](http://travis-ci.org/sarxos/webcam-capture)

## Features

* Simple and thread-safe API,
* No additional software required,
* Supports multiple platforms (Windows, Linux, Mac OS, etc) and various architectures (32-bit, 64-bit, ARM),
* Stream images from build-in or USB-connected PC webcams, 
* Stream images from IP / network cameras,
* Detect motion,
* Available in Maven Central,
* Can re-stream images,
* Also available as standalone ZIP binaries with all dependencies included,
* Supports additional video grabbing drivers (such as OpenIMAJ, LTI-CIVIL, JMF, FMJ, OpenCV, VLC, IP Camera),
* Contains Swing component to display image from webcam / IP / network camera,

## Maven

```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture</artifactId>
	<version>0.3.6</version>
</dependency>
```

If you are not using Maven, then **[here](http://www.sarxos.pl/repo/maven2/com/github/sarxos/webcam-capture/0.3.6/webcam-capture-0.3.6-dist.zip)**
you can download ZIP containing all required 3rd-party JARs.

## Examples

Some pretty basic examples.

### Save Webcam Image In File

Code below will capture image from your PC webcam and save it in ```test.png``` file:

```java
Webcam webcam = Webcam.getDefault();
BufferedImage image = webcam.getImage();
ImageIO.write(image, "PNG", new File("test.png"));
```

### Display Webcam Image In JFrame

This one will display image from webcam in ```JFrame``` window:

```java
JFrame window = new JFrame("Test webcam panel");
window.add(new WebcamPanel(Webcam.getDefault()));
window.pack();
window.setVisible(true);
window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
```

### Take Picture From IP / Network Camera

This is simple example of how to use Webcam Capture with **IP / network camera**:

```java
String address = "http://88.37.116.138/mjpg/video.mjpg ";
IpCamDevice livecam = new IpCamDevice("Lignano Beach", new URL(address), IpCamMode.PUSH);
IpCamDriver driver = new IpCamDriver();
driver.register(livecam);
Webcam.setDriver(driver);
Image image = Webcam.getDefault().getImage(); // live picture from Lignano beach (Italia)
```

For more detailed / complex examples of how to use Webcam Capture with IP / network cameras please follow to **[this
subproject](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-driver-ipcam)**.

### Detect Motion

This code will print appropriate message whenever motion si detected.

```java
WebcamMotionDetector detector = new WebcamMotionDetector(Webcam.getDefault(), 25, 1000);
detector.setInterval(100);
detector.start();
while (true) {
    if (detector.isMotion()) {
    	System.out.printl("Motion detected!");
    }
    Thread.sleep(200);
}
```

More detailed motion detector example with some fancy GUI can be found  in
**[webcam-capture-motiondetector](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-motiondetector)**
subproject.

### Use Webcam In Applet

Example of how to enable Webcam Capture capabilities in Java Applet can be found in 
**[webcam-capture-applet](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-applet)**
subproject. 

### Custom Painter for WebcamPanel

You can very easily create your own image painter which can be used by `WebcamPanel`. Such custom 
painter can do fantastic things with image from camera, add some effects, filters, etc. Can also 
perform image analysis and display output in real-time. 

To explore example of how to create simple custom painter, please follow to the 
**[webcam-capture-painter](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-painter)**
subproject. I'm sure you can find some fancy stuff there.

### Read QR Codes

Example presenting how to read QR codes with _Webcam Capture_ and 
[ZXing](https://github.com/zxing/zxing) project is available in 
**[webcam-capture-qrcode](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-qrcode)**
subproject.

```java
BufferedImage image = webcam.getImage();
LuminanceSource source = new BufferedImageLuminanceSource(image);
BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

try {
	Result result = new MultiFormatReader().decode(bitmap);
	if (result != null) {
		System.out.println("QR code text: " + result.getText());
	}
} catch (NotFoundException e) {
	System.out.println("No QR code in camera view");
}
```

## Drivers

Webcam Capture can utilize additional drivers to extend its own functionality. Currently below
drivers are stable and available in Maven Central:

* **[IP Camera Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-driver-ipcam)** (adds IP / network cameras support)  
* **[JMF Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-driver-jmf)** (JMF replacement for build-in webcam driver)

Stable but not available in Maven Central:

* **[LTI-CIVIL Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-driver-civil)** (LTI-CIVIL replacement for build-in webcam driver)
* **[OpenIMAJ Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-driver-civil)** (OpenIMAJ replacement for build-in webcam driver)

Unstable drivers (experimental stuff, can be dropped in future):

* **[OpenCV Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-driver-javacv)** (JavaCV / OpenCV replacement for build-in webcam driver)  
* **[VLC Driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-driver-vlcj)** (VLC replacement for build-in webcam driver)

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

