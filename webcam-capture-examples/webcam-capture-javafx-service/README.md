## Webcam Capture Live Streaming Example

This example has been created by [james-d](https://github.com/james-d) in response to [this question](http://stackoverflow.com/questions/42816859/run-javafx-and-swing-application-at-the-same-time) on Stack Overflow and put in [this gist](https://gist.github.com/james-d/f826c9f38d53628114124a56fb7c4557).

James found that, in general, the FX application thread is prone to hanging if you interact with the webcam much at all on that thread. E.g. ```webcam.close()``` or ```Webcam.getWebcams()``` seem to cause deadlock (on Mac OS X Sierra on a Macbook Pro with built-in camera) and created a couple of wrapper classes that wrap the Webcam as a JavaFX service and a corresponding view.

### How To Use

1. Simply run [FXCamTest](https://github.com/sarxos/webcam-capture/blob/master/webcam-capture-examples/webcam-capture-javafx-service/src/main/java/FXCamTest.java).

#### Notes

1. Not a production quiality code.
2. Java 8 is required to build it.

