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

package org.littletonrobotics.junction.networktables;

public abstract class LoggedNetworkInput {
  public static final String prefix = "NetworkInputs";

  /**
   * Update the current value and save/replay the input. This function should not
   * be called by the user.
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
