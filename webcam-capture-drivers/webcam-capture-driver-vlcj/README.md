# webcam-capture-driver-vlcj

This capture driver use [vlcj](http://caprica.github.io/vlcj/) as a
underlying capturing framework.

This driver should not be used when you need good performance and high
FPS rate. Because vlcj save each image to the file prior it's returned
by the API, the FPS rate is pretty small. In my case (HP Elitebook 8460p,
quad core, 4 GB RAM, fast SSD disk) it was about ~12 FPS, which is very
low when you compare it to the other capture drivers (e.g. 
[gstreamer capture driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/webcam-capture-driver-gstreamer) or 
[openimaj capture driver](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-drivers/webcam-capture-driver-openimaj), where both can easily
stream 30 FPS).

## Maven

Not yet available.

## License

Copyright (C) 2012 - 2013 Bartosz Firyn <https://github.com/sarxos>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

