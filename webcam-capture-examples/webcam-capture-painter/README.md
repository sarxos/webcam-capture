# Webcam Capture Custom Painter Example

This simple example show how to create custom painter for `WebcamCapture` Swing 
component used to draw images from `Webcam` instance.
 
## What Is This

This example presents how to use custom painters to alter image displayed in 
`WebcamPanel` component. Usually, if user won't change it, `DefaultPainter` is
being used. This painter implements `Painter` interface which consists of two methods:

* `paintPanel(WebcamPanel wp, Graphics2D g2)` which paints panel when image from camera
is not disdplayed, and second one,
* `paintImage(WebcamPanel wp, BufferedImage img, Graphics2D g2)` which pains image from
camera when camera is turned on.

In this example I used filters from awesome image processing library by
_[Jerry Huxtable Labs](http://www.jhlabs.com/)_
which descriptions be found **[here](http://www.jhlabs.com/ip/filters/)** and which can be
[downloaded](http://search.maven.org/#artifactdetails|com.jhlabs|filters|2.0.235|jar) from Maven Central.

[Look and Feel](http://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html) 
used in all examples can be found in [Insubstantial](https://github.com/Insubstantial/insubstantial) project.
The one I used in this example is `SubstanceTwilightLookAndFeel`. Thank you 
_[Kirill](http://www.linkedin.com/in/kirillcool)_ for this awesome Look and Feel stuff! 

### Screenshoots:

Capturing is disabled:

![Capturing Disabled](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-painter/src/etc/resources/not-displayed.png "Capturing Disabled")

Possible filters (only few from whole JS Labs filters set):

![Filters](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-painter/src/etc/resources/filters.png "Filters")

Crystallize filter:

![Crystallize](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-painter/src/etc/resources/crystallize.png "Crystallize")

Dither filter:

![Dither](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-painter/src/etc/resources/dither.png "Dither")

Invert filter:

![Invert](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-painter/src/etc/resources/invert.png "Invert")

Kaleidoscope filter:

![Kaleidoscope](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-painter/src/etc/resources/kaleidoscope.png "Kaleidoscope")

Noise filter:

![Noise](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-painter/src/etc/resources/noise.png "Noise")

Sphere filter:

![Sphere](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-painter/src/etc/resources/sphere.png "Sphere")

Threshold filter:

![Threshold](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-painter/src/etc/resources/threshold.png "Threshold")

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
