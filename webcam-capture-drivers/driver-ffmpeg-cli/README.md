# FFmpeg CLI Capture Driver

This is a proof-of-concept, experimental capture driver. It uses FFmpeg to 
stream video from webcam device through process standard output.

This driver has been tested on:

* Fedora 26 (FFmpeg 3.1.11 and Oracle JDK 8),
* Ubuntu 14.04 (FFmpeg 3.3.3 and Oracle JDK 8) ,
* Raspbian GNU/Linux 9.3 (stretch) (FFmpeg 3.2.10 and OpenJDK 1.8.0_151),
* Windows 7 64bits (FFmpeg 3.4.1 and Oracle JDK 9).

It has some problems because
it starts own sub-process to stream data from `ffmpeg` and this process needs to be killed,
but in general works well.

## How To Use

Set new driver before you start using Webcam class:

```java
public class TakePictureExample {

	// set capture driver for ffmpeg tool
	static {
		Webcam.setDriver(new FFmpegCliDriver());
	}

	public static void main(String[] args) throws IOException {

		// get default webcam and open it
		Webcam webcam = Webcam.getDefault();
		webcam.open();

		// get image from webcam device
		BufferedImage image = webcam.getImage();

		// save image to PNG file
		ImageIO.write(image, "JPG", new File("test.jpg"));

		// close webcam
		webcam.close();
	}
}
```

## License

This particular driver code is released under Public Domain license.

---

I, the copyright holder of this work, hereby release it into the
public domain. This applies worldwide.

In case this is not legally possible, I grant any entity the right
to use this work for any purpose, without any conditions, unless 
such conditions are required by law.

