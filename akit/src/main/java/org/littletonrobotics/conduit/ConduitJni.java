// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.conduit;

import java.nio.ByteBuffer;

/** ConduitJni */
class ConduitJni {

  static {
    System.loadLibrary("wpilibio");
  }

  public static native ByteBuffer getBuffer();

  public static native void capture();

  public static native void start();

  public static native void configurePowerDistribution(int moduleID, int moduleType);
}
