// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

// Copyright 2021-2023 FRC 6328
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