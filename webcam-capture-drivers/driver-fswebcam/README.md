# FsWebcam Capture Driver

This capture driver is designed to allow developers to use console program called
_fswebcam_ written by Philip Heron as the source of images for Webcam Capture API.
This capture driver works on **only** on *nix and requires 
[fswebcam](https://github.com/fsphil/fswebcam) command line too to be installed
on the environment where Webcam Capture API is used.

To install fswebcam:

```plain
sudo apt-get install fswebcam
```

## How To Use

Set capture driver before you start using Webcam class:

```java
public class TakePictureExample {

	// set capture driver for fswebcam tool
	static {
		Webcam.setDriver(new FsWebcamDriver());
	}

	public static void main(String[] args) throws IOException {

		// get default webcam and open it
		Webcam webcam = Webcam.getDefault();
		webcam.open();

		// get image from webcam device
		BufferedImage image = webcam.getImage();

		// save image to PNG file
		ImageIO.write(image, "PNG", new File("test.png"));

		// close webcam
		webcam.close();
	}
}
```

## Issues

There are several known issues. If you have an idea of how those can
be fixed, please send the pull request with the code change and I will
be happy to merge it into the master branch.

1. Single call to getImage() causes webcam to be re-open again,
2. Because of 1, webcam diode is blinking,
3. Because of 1, FPS is pretty slow (0.2 FPS on my Ubuntu laptop),
4. In some cases when the main Java process is killed, the fswebcam subprocess keeps running because no one is reading from the pipe.


## License

Copyright (C) 2014 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
