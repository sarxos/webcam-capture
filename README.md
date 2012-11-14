# Java Webcam Capture

This library allows you to use your build-in or external webcam directly from Java.

Complete documentation, API, examples, tutorials and many more can be found here:

[http://webcam-capture.sarxos.pl/](http://webcam-capture.sarxos.pl/)


[![Build Status](https://secure.travis-ci.org/sarxos/webcam-capture.png?branch=master)](http://travis-ci.org/sarxos/webcam-capture)

## How To Use

Basic example of how _Webcam Capture_ can be used to get image from PC webcam.

### Basic Example

Code below will capture image from your PC webcam and save it in ```test.png``` file:

```java
Webcam webcam = Webcam.getDefault();
BufferedImage image = webcam.getImage();
ImageIO.write(image, "PNG", new File("test.png"));
```

This one will display image from webcam in ```JFrame``` window:

```java
JFrame window = new JFrame("Test webcam panel");
window.add(new WebcamPanel(Webcam.getDefault()));
window.pack();
window.setVisible(true);
window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
```

For examples of how to use Webcam Capture with **IP cameras** please follow the **[appropriate
subproject](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-driver-ipcam)**.

### Maven

_Webcam Capture_ is available in Maven Central, so you can either add Maven (or Ivy or Grandle)
dependency to your project or access page above and download complete ZIP package, and then add 
all JARs into your project's classpath.

```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture</artifactId>
	<version>0.3.5</version>
</dependency>
```

### Required JARs

If you are not using Maven nor any other dependency manager, you can simply download ZIP file
containing all required JARs. This ZIP is available for download 
**[here](http://repo.sarxos.pl/maven2/com/github/sarxos/webcam-capture/0.3.5/webcam-capture-0.3.5-dist.zip)**.

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


