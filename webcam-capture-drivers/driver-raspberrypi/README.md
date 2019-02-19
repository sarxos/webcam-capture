# webcam-capture-driver-raspistill

This capture driver is special for raspberrypi. __raspistill， raspiyuv, ...__ is the command line tool on raspberrypi for capturing photographs or vedios with the camera module. The most important is that command line tool has feature grabbing image repeatly(mock FPS) from camera and print to file or console. They are:

1.  -o, --output : Output filename <filename> (to **write to stdout, use '-o -**'). If not specified, no file is saved
2.  -tl, --timelapse : Timelapse mode. Takes a picture every <t>ms
3.  -n, --nopreview : Do not display a preview window

so it is possible use java Runtime to launch raspixxx process and intercept its console output, makes it as
webcam driver. this is one simple and straightforward approach without native JNI or JNA call, no file system exchange. You will not struggle with performance issue and native code compiling. 

##Install

sudo apt-get update

sudo apt-get install raspistill

raspistill --help //to check installation


## Maven

```xml
<!--If you want to access snapshot repo, please add this to your pom -->
<repository>
    <id>Sonatype OSS Snapshot Repository</id>
    <url>http://oss.sonatype.org/content/repositories/snapshots</url>
</repository>
<dependency>
    <groupId>com.github.sarxos</groupId>
    <artifactId>webcam-capture-driver-raspberrypi</artifactId>
    <version>${your version}</version>
</dependency>
```

## How To Use

Set capture driver before you start using Webcam class. this is one basic demo, check test source for demo with option panel.

```java
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPanel.DrawMode;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.raspistill.*;


public class WebcamPanelExample {

	static {
		//Webcam.setDriver(new RaspistillDriver());
		Webcam.setDriver(new RaspiYUVDriver());
	}

	public static void main(String[] args) throws InterruptedException {

		final JFrame window = new JFrame("Screen Capture Example");
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().setLayout(new FlowLayout());

		final Dimension resolution = WebcamResolution.QVGA.getSize();

		for (final Webcam webcam : Webcam.getWebcams()) {

			webcam.setCustomViewSizes(resolution);
			webcam.setViewSize(resolution);
			webcam.open();

			final WebcamPanel panel = new WebcamPanel(webcam);
			panel.setFPSDisplayed(true);
			panel.setDrawMode(DrawMode.FIT);
			panel.setImageSizeDisplayed(true);
			panel.setPreferredSize(resolution);

			window.getContentPane().add(panel);
		}

		window.pack();
		window.setVisible(true);
	}
}
```
One demo snapshot as follows, please check WebcamPanelExample.java for detail.

![](https://raw.githubusercontent.com/alexmao86/webcam-capture/master/webcam-capture-drivers/driver-raspistill/src/etc/resources/snapshot.png)

## Known Problems

Becuase is pure java image processing, so performance is not good. The actually fps is low. According to test, about 500ms for PNG decoder to decode 640x480 image from raspistill stream.

## Driver Parameters
this driver makes above arguments configurable, when driver instance created, it will load arguments step by step as follows,
1. load built-in parameters
2. search system properties which starts with "raspi.". then merge to properties
3. You can set all command line arguments from device instance's setParameters

## Capture Driver License

Copyright (C) 2012 - 2019 Bartosz Firyn <https://github.com/sarxos> and contributors

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

