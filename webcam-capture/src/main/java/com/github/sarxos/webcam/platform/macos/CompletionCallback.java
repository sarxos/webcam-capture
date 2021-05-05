package com.github.sarxos.webcam.platform.macos;

import org.bridj.objc.ObjCBlock;

public abstract class CompletionCallback extends ObjCBlock {
  public abstract void callback(boolean granted);
}
