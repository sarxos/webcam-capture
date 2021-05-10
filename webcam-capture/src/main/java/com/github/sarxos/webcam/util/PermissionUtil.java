package com.github.sarxos.webcam.util;

import com.github.sarxos.webcam.platform.macos.AVAuthorizationStatus;
import com.github.sarxos.webcam.platform.macos.AVCaptureDevice;
import com.github.sarxos.webcam.platform.macos.AVMediaType;
import org.bridj.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionUtil {
  private static final Logger LOG = LoggerFactory.getLogger(PermissionUtil.class);

  /**
   * Check the permissions on MacOS.
   * @return True if granted, false otherwise.
   */
  private static boolean checkPermissionsMac() {
    AVAuthorizationStatus authorizationStatus = AVCaptureDevice
        .getAuthorizationStatus(AVMediaType.AVMediaTypeVideo);

    switch (authorizationStatus) {
      case AVAuthorizationStatusDenied:
        LOG.info("Authorization denied.");
        return false;
      case AVAuthorizationStatusRestricted:
        LOG.info("User can't grant camera permission.");
        return false;
      case AVAuthorizationStatusAuthorized:
        LOG.debug("Already authorized");
        return true;
      case AVAuthorizationStatusNotDetermined:
        LOG.debug("Authorization needed.");
        boolean granted = AVCaptureDevice.requestAccessForMediaType(AVMediaType.AVMediaTypeVideo);

        if (!granted) {
          LOG.info("User denied access to camera.");
        }

        return granted;
      default:
        throw new IllegalStateException();
    }
  }

  /**
   * Check for required permissions.
   * @return True if granted, false otherwise.
   */
  public static boolean checkPermissions() {
    if (Platform.isMacOSX()) {
      return checkPermissionsMac();
    }

    return true;
  }
}
