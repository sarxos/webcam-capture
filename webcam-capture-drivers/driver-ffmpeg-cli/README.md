# FFmpeg CLI Capture Driver

This is a proof-of-concept, experimental capture driver. It uses FFmpeg to 
stream MJPEG-encoded video from webcam device into named pipe. 
This pipe is later read by Java process which converts MJPEG-stream to the set of
```BufferedImage``` objects returned one after the other from ```Webcam.getImage()``` 
method invokation. 

This driver works **only** on Linux, and you **have** to have
[ffmpeg](http://linuxers.org/tutorial/how-install-ffmpeg-linux)
instaled on your PC, it's buggy and not well designed (has 
terrible memory consumption), and will probably remain as such 
untill somebody redesign it. Therefore, if you found a bug in this driver
code, or you would like to perform any enhancement, feel free to 
send pull request. Due to other urgent issues, I wont put any effort
to enhance and/or fix this driver.

## How To Use It

Set new driver before you start using Webcam class:

```java
Webcam.setDriver(new FFmpegCliDriver());
Webcam webcam = Webcam.getDefault();
webcam.open();
// ... do your stuff
webcam.close();
```

## License

This particular driver code is released under Public Domain license.

---

I, the copyright holder of this work, hereby release it into the
public domain. This applies worldwide.

In case this is not legally possible, I grant any entity the right
to use this work for any purpose, without any conditions, unless 
such conditions are required by law.

