package com.github.sarxos.webcam.platform.macos;

import org.bridj.BridJ;
import org.bridj.NativeLibrary;
import org.bridj.Pointer;
import org.bridj.objc.NSString;

import java.io.IOException;

public enum AVMediaType {
  AVMediaTypeVideo("AVMediaTypeVideo", "NSCameraUsageDescription"),
  AVMediaTypeAudio("AVMediaTypeAudio", "NSMicrophoneUsageDescription");

  private final Pointer<NSString> ptr;
  private final String permissionKey;

  AVMediaType(String symbol, String permissionKey) {
    this.permissionKey = permissionKey;

    try {
      NativeLibrary library = BridJ.getNativeLibrary(AVCaptureDevice.class);
      Pointer<?> ptrToSymbol = library.getSymbolPointer(symbol);
      this.ptr = ptrToSymbol.getPointer(NSString.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  Pointer<NSString> get() {
    return this.ptr;
  }

  String getPermissionKey() {
    return this.permissionKey;
  }
}
