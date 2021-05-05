package com.github.sarxos.webcam.platform.macos;

import org.bridj.IntValuedEnum;

import java.util.Collections;
import java.util.Iterator;

/**
 * The status values for camera/microphone access
 *
 * @see <a href="https://developer.apple.com/documentation/avfoundation/avauthorizationstatus?language=objc">AVAuthorizationStatus Documentation</a>
 */
public enum AVAuthorizationStatus implements IntValuedEnum<AVAuthorizationStatus> {
  /**
   * The user has not yet granted nor denied permission.
   */
  AVAuthorizationStatusNotDetermined(0),
  /**
   * The user can't grant permission due to a restricted environment.
   */
  AVAuthorizationStatusRestricted(1),

  /**
   * The user denied access.
   */
  AVAuthorizationStatusDenied(2),

  /**
   * The user granted access.
   */
  AVAuthorizationStatusAuthorized(3);

  AVAuthorizationStatus(int value) {
    this.value = value;
  }

  final int value;

  public long value() {
    return value;
  }

  public Iterator<AVAuthorizationStatus> iterator() {
    return Collections.singleton(this).iterator();
  }
}
