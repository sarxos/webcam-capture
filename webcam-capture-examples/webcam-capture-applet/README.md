# Webcam Capture Example Applet

This example presents how to create Java Applet using Webcam Capture project.

You can check working example [here](http://webcam-capture.sarxos.pl/examples/applet).

## Generate Java Keystore

Since our Applet will be using users hardware, we have to either sign it, or 
specify appropriate permissions. Without doing this user will get ```SecurityPermissionException```
when trying to use Applet. This is because Java restrict access to various system elements 
(filesystem, hardware, etc). But when you sign application, Java assume it is safe (since 
you signed it with your personal certificate), and grant access to the system hardware after
user confirm security dialog. 

Therefore, before you start build you have to generate Java keystore which will be used
later by Maven to sign Applet JAR. To do that you have to use ```keytool``` program 
available within JDK ```bin``` directory.

```
$ keytool -genkey -alias example -keyalg RSA -keystore keystore.jks -keysize 2048
```

After executing this command, ```keytool``` will ask you to provide all required information,
such as:

* Keystore password,
* First and last name,
* Your organizational unit name,
* Your organization name,
* City or Locality,
* State or Province name,
* Two-letter country code for this unit,
* Key password for alias (in this case alias is _example_).

In this example, in root directory, I included predefined keystore with alias
_example_ with password _test1234_.

## How To Build

Like in all my other projects you have to have Maven 3.0 or later available in ```PATH```. 
You can download Maven binaries from **[here](http://maventest.apache.org/download.html)**.

When you have Maven on your PC, it's enough to run this command to build project:

```
$ mvn clean install
```

This will cause ```target``` directory to be created, where will find output JAR together 
with ```index.html``` file which both should be deployed to the server.

## Where Are All Required JARs Gone?

None is required in this example. That's because I used _maven-shade-plugin_ to merge all JARs together, so 
you will get only one JAR with all required classes inside (this is known as shaded JAR).

For more details I suggest to check ```pom.xml``` or [google](https://www.google.pl/search?q=shaded+jar+maven&ie=utf-8&oe=utf-8&aq=t&rls=org.mozilla:en-US:official&client=firefox-a).

## How To Sign JAR?

You don't have to. Maven will do that for you. This has been done by using _maven-jarsigner-plugin_
which is using ```keystore.jks``` file to get self-signed certificate and use it to sign the JAR.

## How To Upload Applet To Server?

Use FTP client of your choice and upload these files to the remote site:

* ```webcam-capture-example-applet-[version].jar```
* ```index.html```

You can also modify ```pom.xml``` and add [maven-upload-plugin](http://docs.atlassian.com/maven-upload-plugin/1.1/usage.html)
to automatically upload specific files to the server when running ```mvn deploy```.

## How To Run

Simply access remote site URL. Follow the security confirmation dialog and enjoy 
using this example.

![Security Dialog](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-applet/src/etc/resources/security.png "Security Dialog")

Et voilà! My son's picture is visible in Applet window.

![Running Applet](https://raw.github.com/sarxos/webcam-capture/master/webcam-capture-examples/webcam-capture-applet/src/etc/resources/applet.png "Running Applet")

## License

Copyright (C) 2012 Bartosz Firyn

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


