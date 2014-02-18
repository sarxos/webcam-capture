# webcam-capture-driver-jmf

This is driver containing video grabber for Webcam Capture project. It utilizes JMF 
capabilities to access
PC webcam (those USB-connected too) and can be used as a replacement for default 
build-in driver. Its advantage is the
fact it's pretty fast, but unformtunately, from the other hand, it requires 
**[JMF](http://www.oracle.com/technetwork/java/javase/download-142937.html)** to be 
installed on your PC.

## Maven

```xml
<dependency>
	<groupId>com.github.sarxos</groupId>
	<artifactId>webcam-capture-driver-jmf</artifactId>
	<version>0.3.9</version>
</dependency>
```

If you are not using Maven, then **[here](http://www.sarxos.pl/repo/maven2/com/github/sarxos/webcam-capture-driver-jmf/0.3.9/webcam-capture-driver-jmf-0.3.9-dist.zip)**
you can download ZIP containing all required 3rd-party JARs.

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

