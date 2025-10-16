// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.networktables;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringEntry;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;

/** Manages a String value published to the root table of NT. */
public class LoggedNetworkString extends LoggedNetworkInput {
  private final String key;
  private final StringEntry entry;
  private String defaultValue = "";
  private String value;

  /**
   * Creates a new LoggedNetworkString, for handling a string input sent via NetworkTables.
   *
   * @param key The key for the number, published to the root table of NT or
   *     "/DashboardInputs/{key}" when logged.
   */
  public LoggedNetworkString(String key) {
    this.key = key;
    this.entry = NetworkTableInstance.getDefault().getStringTopic(key).getEntry("");
    this.value = defaultValue;
    Logger.registerDashboardInput(this);
  }

  /**
   * Creates a new LoggedNetworkString, for handling a string input sent via NetworkTables.
   *
   * @param key The key for the number, published to the root table of NT or
   *     "/DashboardInputs/{key}" when logged.
   * @param defaultValue The default value if no value in NT is found.
   */
  public LoggedNetworkString(String key, String defaultValue) {
    this(key);
    setDefault(defaultValue);
    this.value = defaultValue;
  }

  /**
   * Updates the default value, which is used if no value in NT is found.
   *
   * @param defaultValue The new default value.
   */
  public void setDefault(String defaultValue) {
    this.defaultValue = defaultValue;
    entry.set(entry.get(defaultValue));
  }

  /**
   * Publishes a new value. Note that the value will not be returned by {@link #get()} until the
   * next cycle.
   *
   * @param value The new value.
   */
  public void set(String value) {
    entry.set(value);
  }

  /**
   * Returns the current value.
   *
   * @return The current value.
   */
  public String get() {
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
