package org.littletonrobotics.conduit;

import java.nio.ByteBuffer;

/**
 * ConduitJni
 */
class ConduitJni {
  public static native ByteBuffer getBuffer();
  public static native void capture();
}