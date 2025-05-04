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

import edu.wpi.first.networktables.DoubleEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;

/** Manages a number value published to the root table of NT. */
public class LoggedNetworkNumber extends ObservableLoggedNetworkInput<Double> {

  private static final double DEFAULT_VALUE = 0.0;

  private final String key;
  private final DoubleEntry entry;
  private double defaultValue = DEFAULT_VALUE;
  private double value;

  /**
   * Creates a new LoggedNetworkNumber, for handling a number input sent via
   * NetworkTables.
   *
   * @param key The key for the number, published to the root table of NT or
   *            "/DashboardInputs/{key}" when logged.
   */
  public LoggedNetworkNumber(String key) {
    super(DEFAULT_VALUE);
    this.key = key;
    this.entry = NetworkTableInstance.getDefault()
      .getDoubleTopic(key)
      .getEntry(
        DEFAULT_VALUE,
        PubSubOption.keepDuplicates(false),
        PubSubOption.pollStorage(1)
      );
    this.value = defaultValue;
    Logger.registerDashboardInput(this);
  }

  /**
   * Creates a new LoggedNetworkNumber, for handling a number input sent via
   * NetworkTables.
   *
   * @param key          The key for the number, published to the root table of NT
   *                     or "/DashboardInputs/{key}" when logged.
   * @param defaultValue The default value if no value in NT is found.
   */
  public LoggedNetworkNumber(String key, double defaultValue) {
    this(key);
    setDefault(defaultValue);
    this.value = defaultValue;
  }

  /** Updates the default value, which is used if no value in NT is found. */
  public void setDefault(double defaultValue) {
    this.defaultValue = defaultValue;
    entry.set(entry.get(defaultValue));
  }

  /**
   * Publishes a new value. Note that the value will not be returned by
   * {@link #get()} until the next cycle.
   */
  public void set(double value) {
    entry.set(value);
  }

  /** Returns the current value. */
  public Double get() {
    return value;
  }

  private final LoggableInputs inputs = new LoggableInputs() {
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

      // Only do tunables when not in a match
      if (DriverStation.getMatchType() == DriverStation.MatchType.None) {
        var changes = entry.readQueueValues();
        if (changes.length == 1) {
          notifyListeners();
        }
      }
    }
    Logger.processInputs(prefix, inputs);
  }
}
