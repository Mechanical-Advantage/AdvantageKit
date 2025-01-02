// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

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

  public static native void configurePowerDistribution(int moduleID, int moduleType);
}