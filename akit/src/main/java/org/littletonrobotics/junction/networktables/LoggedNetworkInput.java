// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.networktables;

public abstract class LoggedNetworkInput {
  public static final String prefix = "NetworkInputs";

  /**
   * Update the current value and save/replay the input. This function should not be called by the
   * user.
   */
  public abstract void periodic();

  /** Removes the leading slash from a key. */
  protected static String removeSlash(String key) {
    if (key.startsWith("/")) {
      return key.substring(1);
    } else {
      return key;
    }
  }
}
