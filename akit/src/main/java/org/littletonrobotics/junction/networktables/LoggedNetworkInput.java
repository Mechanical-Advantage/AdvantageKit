// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.networktables;

/** Base class for periodic dashboard inputs. */
public abstract class LoggedNetworkInput {
  /** The subtable prefix for input logging. */
  protected static final String prefix = "NetworkInputs";

  /**
   * Update the current value and save/replay the input. <b>This function should never be called by
   * user code.</b>
   */
  public abstract void periodic();

  /**
   * Removes the leading slash from a key.
   *
   * @param key The input key that may include a leading slash.
   * @return The output key without a leading slash.
   */
  protected static String removeSlash(String key) {
    if (key.startsWith("/")) {
      return key.substring(1);
    } else {
      return key;
    }
  }

  /** Creates a new LoggedNetworkInput. */
  protected LoggedNetworkInput() {}
}
