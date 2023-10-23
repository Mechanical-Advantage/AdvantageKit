// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj;

import org.littletonrobotics.junction.inputs.LoggedDriverStation;

/**
 * A wrapper around Driver Station control word. Patched by AdvantageKit to
 * support logging.
 */
public class DSControlWord {
  private boolean enabled = false;
  private boolean autonomous = false;
  private boolean test = false;
  private boolean emergencyStop = false;
  private boolean fmsAttached = false;
  private boolean dsAttached = false;

  /**
   * DSControlWord constructor.
   *
   * <p>
   * Upon construction, the current Driver Station control word is read and stored
   * internally.
   */
  public DSControlWord() {
    refresh();
  }

  /** Update internal Driver Station control word. */
  public void refresh() {
    enabled = LoggedDriverStation.getDSData().enabled;
    autonomous = LoggedDriverStation.getDSData().autonomous;
    test = LoggedDriverStation.getDSData().test;
    emergencyStop = LoggedDriverStation.getDSData().emergencyStop;
    fmsAttached = LoggedDriverStation.getDSData().fmsAttached;
    dsAttached = LoggedDriverStation.getDSData().dsAttached;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * enabled.
   *
   * @return True if the robot is enabled, false otherwise.
   */
  public boolean isEnabled() {
    return enabled && dsAttached;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * disabled.
   *
   * @return True if the robot should be disabled, false otherwise.
   */
  public boolean isDisabled() {
    return !isEnabled();
  }

  /**
   * Gets a value indicating whether the Robot is e-stopped.
   *
   * @return True if the robot is e-stopped, false otherwise.
   */
  public boolean isEStopped() {
    return emergencyStop;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in autonomous mode.
   *
   * @return True if autonomous mode should be enabled, false otherwise.
   */
  public boolean isAutonomous() {
    return autonomous;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in autonomous mode and enabled.
   *
   * @return True if autonomous should be set and the robot should be enabled.
   */
  public boolean isAutonomousEnabled() {
    return autonomous && enabled && dsAttached;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in operator-controlled mode.
   *
   * @return True if operator-controlled mode should be enabled, false otherwise.
   */
  public boolean isTeleop() {
    return !(isAutonomous() || isTest());
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in operator-controller mode and enabled.
   *
   * @return True if operator-controlled mode should be set and the robot should
   *         be enabled.
   */
  public boolean isTeleopEnabled() {
    return !autonomous && !test && enabled && dsAttached;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in test mode.
   *
   * @return True if test mode should be enabled, false otherwise.
   */
  public boolean isTest() {
    return test;
  }

  /**
   * Gets a value indicating whether the Driver Station is attached.
   *
   * @return True if Driver Station is attached, false otherwise.
   */
  public boolean isDSAttached() {
    return dsAttached;
  }

  /**
   * Gets if the driver station attached to a Field Management System.
   *
   * @return true if the robot is competing on a field being controlled by a Field
   *         Management System
   */
  public boolean isFMSAttached() {
    return fmsAttached;
  }
}