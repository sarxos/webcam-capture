# Webcam WebSockets Example

This example demonstrates how images feed can be transported over the WebSocket. 
 
## What Is This

This example has been prepared to demonstrate very primitive way to transport images from server to frontend using HTML5 WebSockets.

After the ```WebcamWebSocketsExample``` is started, the server starts on port 8123. Front end can subscribe to new images by accessing ```ws://127.0.0.1:8123/```.

The example is composed of server and frontend parts:

* [Server - Java files](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-websockets/src/main/java)
* [Frontend - HTML, JavaScript and CSS](https://github.com/sarxos/webcam-capture/tree/master/webcam-capture-examples/webcam-capture-websockets/src/main/html)

## Screenshoots

![web page](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-examples/webcam-capture-websockets/src/etc/resources/screen.png?raw=true "web page")

![initializing](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-examples/webcam-capture-websockets/src/etc/resources/screen2.png?raw=true "initializing")


## License

Copyright (C) 2015 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
