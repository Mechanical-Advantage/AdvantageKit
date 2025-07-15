// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.networktables;

import edu.wpi.first.networktables.BooleanEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;

/** Manages a boolean value published to the root table of NT. */
public class LoggedNetworkBoolean extends LoggedNetworkInput {
  private final String key;
  private final BooleanEntry entry;
  private boolean defaultValue = false;
  private boolean value;

  /**
   * Creates a new LoggedNetworkBoolean, for handling a boolean input sent via NetworkTables.
   *
   * @param key The key for the number, published to the root table of NT or
   *     "/DashboardInputs/{key}" when logged.
   */
  public LoggedNetworkBoolean(String key) {
    this.key = key;
    this.entry = NetworkTableInstance.getDefault().getBooleanTopic(key).getEntry(false);
    this.value = defaultValue;
    Logger.registerDashboardInput(this);
  }

  /**
   * Creates a new LoggedNetworkBoolean, for handling a boolean input sent via NetworkTables.
   *
   * @param key The key for the number, published to the root table of NT or
   *     "/DashboardInputs/{key}" when logged.
   * @param defaultValue The default value if no value in NT is found.
   */
  public LoggedNetworkBoolean(String key, boolean defaultValue) {
    this(key);
    setDefault(defaultValue);
    this.value = defaultValue;
  }

  /**
   * Updates the default value, which is used if no value in NT is found.
   *
   * @param defaultValue The new default value.
   */
  public void setDefault(boolean defaultValue) {
    this.defaultValue = defaultValue;
    entry.set(entry.get(defaultValue));
  }

  /**
   * Publishes a new value. Note that the value will not be returned by {@link #get()} until the
   * next cycle.
   *
   * @param value The new value.
   */
  public void set(boolean value) {
    entry.set(value);
  }

  /**
   * Returns the current value.
   *
   * @return The current value.
   */
  public boolean get() {
    return value;
  }

  private final LoggableInputs inputs =
      new LoggableInputs() {
        public void toLog(LogTable table) {
          table.put(removeSlash(key), value);
        }

        public void fromLog(LogTable table) {
          value = table.get(removeSlash(key), defaultValue);
        }
      };

  public void periodic() {
    if (!Logger.hasReplaySource()) {
      value = entry.get(defaultValue);
    }
    Logger.processInputs(prefix, inputs);
  }
}
