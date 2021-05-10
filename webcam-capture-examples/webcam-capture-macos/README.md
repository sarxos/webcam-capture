# Webcam-Capture on MacOS

This example demonstrates how to use the Webcam Capture API on newer MacOS Systems.
To use jpackager you need  JDK 14 or greater.


## How To Build

Create the jar with maven:


```shell script
cd webcam-capture-examples/webcam-capture-macos
mvn -Djavacpp.platform=macosx-x86_64 package
```

Determine the dependencies:
```shell script
jdeps --multi-release 11 --print-module-deps --ignore-missing-deps -cp 'target/*:target/dependency/*' target/*.jar
```

That should yield the dependencies `java.base,java.desktop,java.logging,java.management,jdk.unsupported`.

Now create a custom runtime.
It might be necessary to specify the full path to jlink.

```shell script
/Library/Java/JavaVirtualMachines/openjdk.jdk/Contents/Home/bin/jlink --output jdk14 --add-modules \
       java.base,java.desktop,java.logging,java.management,jdk.unsupported \
       --strip-debug --no-man-pages --no-header-files
```

Next build the app image:

```shell script
/Library/Java/JavaVirtualMachines/openjdk.jdk/Contents/Home/bin/jpackage --runtime-image jdk14 \
       --verbose --name WebcamFrameExample --input target/ --main-jar $(basename target/*.jar) \
       --main-class WebcamFrameExample -t app-image --resource-dir ./res
```

Finally add the entitlements:

```shell script
codesign --force --sign - --entitlements res/Entitlements.plist WebcamFrameExample.app
```