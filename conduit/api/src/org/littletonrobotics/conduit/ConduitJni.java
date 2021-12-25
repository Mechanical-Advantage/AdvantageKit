package org.littletonrobotics.conduit;

import java.nio.ByteBuffer;

/**
 * ConduitJni
 */
class ConduitJni {

  static {
    System.loadLibrary("wpilibio");
  }

  public static native ByteBuffer getBuffer();
  public static native void capture();
  public static native void start();
}