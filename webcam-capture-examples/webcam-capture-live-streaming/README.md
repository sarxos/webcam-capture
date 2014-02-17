## Webcam Capture Live Streaming Example

This example demonstrate how to encode set of buffered images obtained 
from ```Webcam``` instance, transcode into h.264 stream and send to remote peer
where it is decoded and rendered in panel.

Example provided by [hepin1989](https://github.com/hepin1989). Thank you! This is wonderful piece of the good code :)

### How To Use

1. Run [StreamServer](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-examples/webcam-capture-live-streaming/src/main/java/us/sosia/video/stream/agent/StreamServer.java) - this will open the webcam device and start listening for incoming streaming requests.
2. Run [StreamClient](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-examples/webcam-capture-live-streaming/src/main/java/us/sosia/video/stream/agent/StreamClient.java) - this will connect to the StreamServer and start displaying image from the remote camera.

The server / client hostname is set by default to ```localhost``` but you can change it to reflect the real IP / hostname.

#### Notes

1. The code supports sunny-day scenario only, just to demonstrate the idea, so there are is no complex error checking statements and the code may behave in unexpected manner when launched in _real_ network.
2. It uses [Xuggler](https://github.com/artclarke/xuggle-xuggler) which seems to be discontinued.
3. Remember, this example is to present an idea, so if you need enhancement or bug fix, please implement it and send pull request to share it with community. Be creative :)


