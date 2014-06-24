# webcam-capture-driver-ipcam

This is capture driver which gives Webcam Capture API possibility to
access IP camera devices. It allows it to
handle pictures from IP cameras supporting JPEG and MJPEG (Motion JPEG) compression
and can work in two modes - ```PULL``` for JPEG and ```PUSH``` for MJPEG.

What are the differences between these two modes:

* ```PULL``` - will request new JPEG image each time when it is required,
* ```PUSH``` - stream Motion JPEG in real time and serve newest image on-demand.

Support for few IP cameras is build-in, that list includes:

* [IP Robocam 641](http://www.marmitek.com/en/product-details/home-automation-security/ip-cameras/ip-robocam-641.php) by [Marmitek](http://www.marmitek.com/) (MJPEG)
* [Speed Dome X104S](http://www.ipcctv.com/product.php?xProd=10&xSec=26) by [Xvision](http://www.ipcctv.com/) (MJPEG)
* [B7210](http://www.zavio.com/product.php?id=45) by [Zavio](http://www.zavio.com/) (JPEG)
* [F3201](http://www.zavio.com/product.php?id=28) by [Zavio](http://www.zavio.com/) (JPEG)

If you would like to have more IP cameras supported, please create new issue, describe the camera
model, provide demo URL and API if available. You can also create your own IP camera class. In such 
a case please use provided models as a reference. In case of any issues don't hesitate to ask for
help :) Later, when your class is ready, I will be happy to merge it with official distribution.


## Download

The latest **development** version JAR (aka SNAPSHOT) can be downloaded [here](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.github.sarxos&a=webcam-capture-driver-ipcam&v=0.3.10-SNAPSHOT).

The latest **stable** version ZIP bundle can be downloaded [here](http://repo.sarxos.pl/maven2/com/github/sarxos/webcam-capture-driver-ipcam/0.3.10-RC7/webcam-capture-driver-ipcam-0.3.10-RC7-dist.zip).

## Maven

Stable:

```xml
<repository>
	<id>SarXos Repository</id>
	<url>http://www.sarxos.pl/repo/maven2</url>
</repository>
```
```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture-driver-ipcam</artifactId>
	<version>0.3.10-RC7</version>
</dependency>
```

Snapshot:

```xml
<repository>
    <id>Sonatype OSS Snapshot Repository</id>
    <url>http://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.sarxos</groupId>
    <artifactId>webcam-capture-driver-ipcam</artifactId>
    <version>0.3.10-SNAPSHOT</version>
</dependency>
```

## Examples

Few real live examples of how to use Webcam Capture together with IP camera driver.
Please note that some URLs can be out-of-date when you test this code, however you
can find some usable IP camera demos googling _"ip camera demo"_ or accessing 
[this page](http://www.axis.com/solutions/video/gallery.htm).

### Example 1

Example of how to display image from IP camera device which expose pictures 
as MJPEG stream (this is refered as PUSH mode, because camera push new images 
to the client as soon as new one is available).

```java
static {
	Webcam.setDriver(new IpCamDriver());
}

public static void main(String[] args) throws MalformedURLException {
	IpCamDeviceRegistry.register(new IpCamDevice("Lignano", "http://88.37.116.138/mjpg/video.mjpg", IpCamMode.PUSH));
	JFrame f = new JFrame("Live Views From Lignano Beach");
	f.add(new WebcamPanel(Webcam.getDefault()));
	f.pack();
	f.setVisible(true);
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
```

![Lignano Beach](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-drivers/webcam-capture-driver-ipcam/src/etc/resources/lignano-beach.png "Lignano Beach")

### Example 2

Example of how to display image from multiple IP camera devices which expose 
it as JPG pictures. In this example we are using IP camera storage feature which
use XML file to define all available cameras.

```java
/**
 * Here we are additionally using cameras storage which is based on simple
 * XML file where all cameras which will be used are listed. By using
 * cameras storage you do not have to register devices neither thru the
 * registry nor driver instance. It's the simplest to deal with multiple
 * cameras when you do not have to add/remove cameras in runtime.
 */
static {
	Webcam.setDriver(new IpCamDriver(new IpCamStorage("src/examples/resources/cameras.xml")));
}

public static void main(String[] args) throws MalformedURLException {

	JFrame f = new JFrame("Dasding Studio Live IP Cameras Demo");
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setLayout(new GridLayout(0, 3, 1, 1));

	List<WebcamPanel> panels = new ArrayList<WebcamPanel>();

	for (Webcam webcam : Webcam.getWebcams()) {

		WebcamPanel panel = new WebcamPanel(webcam, new Dimension(256, 144), false);
		panel.setFillArea(true);
		panel.setFPSLimited(true);
		panel.setFPS(0.2); // 0.1 FPS = 1 frame per 10 seconds
		panel.setBorder(BorderFactory.createEmptyBorder());

		f.add(panel);
		panels.add(panel);
	}

	f.pack();
	f.setVisible(true);

	for (WebcamPanel panel : panels) {
		panel.start();
	}
}
```

And here is the cameras.xml file used in the above example:

```xml
<?xml version="1.0" encoding="UTF-8" ?> 
<storage>
	<ipcam name="Dasding 01" url="http://www.dasding.de/ext/webcam/webcam770.php?cam=1" />
	<ipcam name="Dasding 02" url="http://www.dasding.de/ext/webcam/webcam770.php?cam=2" />
	<ipcam name="Dasding 04" url="http://www.dasding.de/ext/webcam/webcam770.php?cam=4" />
	<ipcam name="Dasding 06" url="http://www.dasding.de/ext/webcam/webcam770.php?cam=6" />
	<ipcam name="Dasding 07" url="http://www.dasding.de/ext/webcam/webcam770.php?cam=7" />
	<ipcam name="Dasding 10" url="http://www.dasding.de/ext/webcam/webcam770.php?cam=10" />
</storage>
```

![Dasding Studio Live IP Camera](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-drivers/webcam-capture-driver-ipcam/src/etc/resources/dasding-live.png "Dasding Studio Live IP Camera")

## License

Copyright (C) 2012 - 2014 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

