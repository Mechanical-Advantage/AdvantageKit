package org.littletonrobotics.junction.networktables;

import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class LoggedDashboardBoolean implements LoggedDashboardInput {
  private final String key;
  private boolean defaultValue;
  private boolean value;

  private final LoggableInputs inputs = new LoggableInputs() {
    public void toLog(LogTable table) {
      table.put(key, value);
    }

    public void fromLog(LogTable table) {
      value = table.get(key, defaultValue);
    }
  };

  /**
   * Creates a new LoggedDashboardBoolean, for handling a string input sent via
   * NetworkTables.
   * 
   * @param key The key for the boolean, published to
   *            "/SmartDashboard/{key}" for NT or
   *            "/DashboardInputs/{key}" when logged.
   */
  public LoggedDashboardBoolean(String key) {
    this(key, false);
  }

  /**
   * Creates a new LoggedDashboardBoolean, for handling a string input sent via
   * NetworkTables.
   * 
   * @param key          The key for the boolean, published to
   *                     "/SmartDashboard/{key}" for NT or
   *                     "/DashboardInputs/{key}" when logged.
   * @param defaultValue The default value if no value in NT is found.
   */
  public LoggedDashboardBoolean(String key, boolean defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.value = defaultValue;
    SmartDashboard.putBoolean(key, SmartDashboard.getBoolean(key, defaultValue));
    periodic();
    Logger.registerDashboardInput(this);
  }

  /** Updates the default value, which is used if no value in NT is found. */
  public void setDefault(boolean defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * Publishes a new value. Note that the value will not be returned by
   * {@link #get()} until the next cycle.
   */
  public void set(boolean value) {
    SmartDashboard.putBoolean(key, value);
  }

  /** Returns the current value. */
  public boolean get() {
    return value;
  }

  public void periodic() {
    if (!Logger.hasReplaySource()) {
      value = SmartDashboard.getBoolean(key, defaultValue);
    }
    Logger.processInputs(prefix, inputs);
  }
}