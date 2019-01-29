package com.github.sarxos.webcam.ds.raspistill;

public final class ByteUtil {
  private ByteUtil() {}

  public final static String toHexString(final byte[] data) {
    StringBuilder builder = new StringBuilder(data.length << 2);
    for (int i = 0; i < data.length; i++) {
      builder.append("0x");
      if ((data[i] & 0xFF) <= 15) {
        builder.append("0");
      }
      builder.append(Integer.toHexString(data[i] & 0xFF).toUpperCase()).append(" ");
    }
    return builder.toString();
  }
}
