# webcam-capture-driver-lti-civil

This is webcam capture driver designed to leverage capabilities of
[LTI-CIVIL](http://sourceforge.net/projects/lti-civil/) project by Larson Technologies Inc. 
and use it to access wide range of UVC devices which can be build-in or connected
with the USB cable.

Although, the LTI-CIVIL works with Mac OS as well as with Linux or Windows, this driver 
has been designed for Linux and Windows only (32- and 64-bit). This is because I
do not have Mac OS machine to perform tests.

## NOTES

* LTI-CIVIL does not work on Ubuntu 12.04 on x86_64!

## Download

TBD

## Example

```java
static {
	Webcam.setDriver(new LtiCivilDriver());
}

public static void main(String[] args) {

	WebcamPanel panel = new WebcamPanel(Webcam.getWebcams().get(0));
	panel.setFPSDisplayed(true);

	JFrame frame = new JFrame("LTI-CIVIL Webcam Capture Driver Demo");
	frame.add(panel);
	frame.pack();
	frame.setVisible(true);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
```

## Webcam Capture Driver License

Copyright (C) 2012 - 2014 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## LTI-CIVIL Binaries License

[GNU Lesser General Public License, version 3.0](http://lti-civil.cvs.sourceforge.net/viewvc/lti-civil/lti-civil/LICENSE?revision=1.1&view=markup)
