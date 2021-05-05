package com.github.sarxos.webcam.platform.macos;

import org.bridj.BridJ;
import org.bridj.IntValuedEnum;
import org.bridj.Pointer;
import org.bridj.ann.Library;
import org.bridj.ann.Runtime;
import org.bridj.objc.NSDictionary;
import org.bridj.objc.NSObject;
import org.bridj.objc.NSString;
import org.bridj.objc.ObjectiveCRuntime;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.bridj.objc.FoundationLibrary.pointerToNSString;

@Library("AVFoundation")
@Runtime(ObjectiveCRuntime.class)
public class AVCaptureDevice extends NSObject {

  static {
      BridJ.register();
  }

  /**
   * Validate that the application has the necessary permission usage description set in the Info.plist
   * @param mediaType The media type to check the permissions for.
   */
  private static void checkForPermission(AVMediaType mediaType) {
    Pointer<NSBundle> mainBundlePtr = NSBundle.mainBundle();

    if(mainBundlePtr == null) {
      throw new RuntimeException("Missing main bundle");
    }

    NSBundle mainBundle = mainBundlePtr.get();

    NSDictionary infoDictionary = mainBundle.infoDictionary().get();
    Pointer<NSObject> value = infoDictionary.valueForKey(pointerToNSString(mediaType.getPermissionKey()));

    if (value == null) {
      throw new RuntimeException("Please configure " + mediaType.getPermissionKey() + " in Info.plist");
    }
  }

  /**
   * Request permission to use the hardware associated with {@param mediaType}.
   *
   * This implementation blocks until the user chose an answer.
   * @param mediaType The media type to request permission for.
   * @return True if granted, false otherwise.
   */
  public static boolean requestAccessForMediaType(AVMediaType mediaType) {
    checkForPermission(mediaType);

    final CountDownLatch latch = new CountDownLatch(1);

    final AtomicBoolean granted = new AtomicBoolean();

    CompletionCallback callback = new CompletionCallback() {
      @Override
      public void callback(boolean grantedValue) {
        latch.countDown();
        granted.set(grantedValue);
      }
    };

    requestAccessForMediaType_completionHandler(mediaType.get(), Pointer.getPointer(callback));

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    return granted.get();
  }

  /**
   * Get the current authorization status for the given media type.
   * @param mediaType The media type to check the status for.
   * @return The current authorization status.
   */
  public static AVAuthorizationStatus getAuthorizationStatus(AVMediaType mediaType) {
    return (AVAuthorizationStatus)authorizationStatusForMediaType(mediaType.get());
  }

  protected static native IntValuedEnum<AVAuthorizationStatus> authorizationStatusForMediaType(Pointer<NSString> mediaType);

  protected static native void requestAccessForMediaType_completionHandler(Pointer<NSString> mediaType, Pointer<CompletionCallback> completionHandler);
}
