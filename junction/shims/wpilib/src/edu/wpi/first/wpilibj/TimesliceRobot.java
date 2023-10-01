// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj;

/**
 * @Deprecated The main robot class must inherit from LoggedRobot instead of
 *             TimesliceRobot when using AdvantageKit's WPILib shims. For more
 *             details, check the AdvantageKit installation documentation:
 *             https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/docs/INSTALLATION.md
 */
@Deprecated
public class TimesliceRobot extends TimedRobot {
  public TimesliceRobot(double robotPeriodicAllocation, double controllerPeriod) {
    super();
  }

  public void schedule(Runnable func, double allocation) {
  }
}