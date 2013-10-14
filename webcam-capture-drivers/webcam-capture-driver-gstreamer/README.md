# webcam-capture-driver-gstreamer

This is GStreamer driver for Webcam Capture project. It allows Webcam Capture to
handle pictures from build-in or USB-connected webcams. It has been designed to 
work with Windows and Linux **only**. To make use of it user have to 
[download](http://code.google.com/p/ossbuild/) and install GStreamer application 
on Windows or use _apt-get_, _yum_ or other packages manager to install it on Linux.

Currently supported GStreamer version is 0.10.x, so make sure you are installing
the correct one! It is **not** compatible with GStreamer 1.0 and above!


## Download

Below is the newest stable version ZIP containing main project
JAR with additional documents, examples and all required 3rd-party
dependencies:

* **Latest stable version** - [webcam-capture-driver-gstreamer-0.3.10-RC6-dist.zip](http://www.sarxos.pl/repo/maven2/com/github/sarxos/webcam-capture-driver-gstreamer/0.3.10-RC6/webcam-capture-driver-gstreamer-0.3.10-RC6-dist.zip)

## Example

```java
static {
	Webcam.setDriver(new GStreamerDriver());
}

public static void main(String[] args) {

	WebcamPanel panel = new WebcamPanel(Webcam.getWebcams().get(0));
	panel.setFPSDisplayed(true);

	JFrame frame = new JFrame("GStreamer Webcam Capture Driver Demo");
	frame.add(panel);
	frame.pack();
	frame.setVisible(true);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
```

## License

Copyright (C) 2012 - 2013 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

