package com.github.sarxos.webcam.platform.macos;

import org.bridj.BridJ;
import org.bridj.Platform;
import org.bridj.Pointer;
import org.bridj.ann.Library;
import org.bridj.ann.Runtime;
import org.bridj.objc.NSDictionary;
import org.bridj.objc.NSObject;
import org.bridj.objc.ObjectiveCRuntime;

@Library("Foundation")
@Runtime(ObjectiveCRuntime.class)
public class NSBundle extends NSObject {
  static {
    if(Platform.isMacOSX()) {
      BridJ.register();
    }
  }

  public static native Pointer<NSBundle> mainBundle();

  public native Pointer<NSDictionary> infoDictionary();
}
