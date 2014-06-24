# webcam-capture-driver-jmf

This is capture driver which gives Webcam Capture API
possibility to use [JMF](http://www.oracle.com/technetwork/java/javase/download-142937.html)
(Java Media Framework) as a middleware accessing webcam devices 
(those build-in and USB enabled). The JMF needs to be installed 
and configured on the PC before this driver can be used.

It can also be used with the alternative [FMJ](http://fmj-sf.net/).

## Download

The latest **development** version JAR (aka SNAPSHOT) can be downloaded [here](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.github.sarxos&a=webcam-capture-driver-jmf&v=0.3.10-SNAPSHOT).

The latest **stable** version ZIP bundle can be downloaded [here](http://repo.sarxos.pl/maven2/com/github/sarxos/webcam-capture-driver-jmf/0.3.10-RC7/webcam-capture-driver-jmf-0.3.10-RC7-dist.zip).


## Maven

Release:

```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture-driver-jmf</artifactId>
	<version>0.3.9</version>
</dependency>
```

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
	<artifactId>webcam-capture-driver-jmf</artifactId>
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
    <artifactId>webcam-capture-driver-jmf</artifactId>
    <version>0.3.10-SNAPSHOT</version>
</dependency>
```

## Example

```java
static {
	Webcam.setDriver(new JmfDriver());
}

public static void main(String[] args) {
	JFrame frame = new JFrame("JMF Webcam Capture Driver Demo");
	frame.add(new WebcamPanel(Webcam.getDefault()));
	frame.pack();
	frame.setVisible(true);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
```

## License

Copyright (C) 2012 - 2014 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

