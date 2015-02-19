# webcam-capture-driver-v4l4j

This is capture driver which uses [V4L4J](http://code.google.com/p/v4l4j) project to access camera devices. It uses V4L4J customized fork available [here](https://github.com/sarxos/v4l4j).

Currently this is the most stable capture driver to be used with the Raspberry Pi boards.

**NOTE!** This driver supports only UVC devices and therefore it will not work with the Raspberry Pi dedicated camera module.

## Maven

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
    <artifactId>webcam-capture-driver-v4l4j</artifactId>
    <version>0.3.11-SNAPSHOT</version>
</dependency>
```

## Example

```java
static {
	Webcam.setDriver(new V4l4jDriver());
}

public static void main(String[] args) {
	JFrame frame = new JFrame("V4L4J Webcam Capture Driver Demo");
	frame.add(new WebcamPanel(Webcam.getDefault()));
	frame.pack();
	frame.setVisible(true);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}
```

## License

This specific driver is distributed under the terms of [GNU GPL v3](http://www.gnu.org/copyleft/gpl.html) license.
