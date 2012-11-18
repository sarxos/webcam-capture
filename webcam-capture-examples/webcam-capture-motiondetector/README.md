# Webcam Capture Motion Detector Example

This is simple application illustrating of how to use build-in motion detector.
 
## Detect Motion

Detector use data from webcam and perform numeric analysis to decide if there 
is a motion, or there is none.

This sample application uses ```while / isMotion()``` approach (the second way
to use motion detector is listener approach). It display Me Gusta Guy when motion
has been detected and Forever Alone Guy when no motion has been detected. In this
example motion inertia is 1000 milliseconds. 

### Some screenshoots:

When motion has ben detected (my son's teddy bear was in move):

![Movement](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-motiondetector/src/etc/resources/movement.png "Movement")

When there was no motion at all:

![No motion](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-motiondetector/src/etc/resources/nothing.png "No motion")

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
