# Webcam Capture Example Applet

This example presents how to create Java Applet using Webcam Capture project.

## Generate Java Keystore

Since our Applet will be using users hardware, we have to either sign it, or 
specify appropriate permissions. Without doing this user will get ```SecurityPermissionException```
when trying to use Applet. This is because Java restrict access to various system elements 
(filesystem, hardware, etc). But when you sign application, Java assume it is safe (since 
you signed it with your personal certificate), and grant access to the system hardware. 

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

This will caus ```target``` directory to be created. You will find output JAR there together 
with ```index.html``` which should be deployed along with this JAR.

## Where Are All Required JARs?

None is required. That's because I used _maven-shade-plugin_ to merge all JARs together so 
you will get only one JAR with all required classes inside (known as shaded JAR).

For more details I suggest to check ```pom.xml```.

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
this example.



