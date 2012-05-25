## Java Webcam Capture POC

Proof of concept of how to use your PC webcam from java.

### Requirements

1. Java 7 (JRE or JDK) installed
2. Java Media Framework (JMF) 2.1.1 or higher installed
3. Webcam connected, installed and configured

### How It Works

This program utilizes JMF capabilities to read video streams from the webcam, so 
if JMF is not able to find device, program will end with exception. To check if
JMF _see_ your webcam you have to:

1. Open Start / Programs / Java Media Framework 2.1.1e
2. Start JMF Registry
3. Switch to _Capture Devices_ tab
4. Your webcam should be there. I have no idea what to do if it is not available on the list!  

If you have more then one webcam then first one will be used (however you can easily 
change this in the source code).

### How To Use It

To get image:

```java
Webcam webcam = Webcam.getWebcams().get(0);
webcam.open();

Image image = webcam.getImage();

webcam.close();
```

To display view from webcam in JPanel:

```java
JPanel panel = new WebcamPanel(webcam);
// use panel somehow (as content pane, as subcomponents, etc)
```

Examples available in ```src/example```

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


