// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

// Copyright 2021-2024 FRC 6328
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
 *             TimedRobot when using AdvantageKit's WPILib shims. For more
 *             details, check the AdvantageKit installation documentation:
 *             https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/docs/INSTALLATION.md
 */
@Deprecated
public class TimedRobot extends IterativeRobotBase {
  static class Callback implements Comparable<Callback> {
    public Runnable func;
    public double period;
    public double expirationTime;

    Callback(Runnable func, double startTimeSeconds, double periodSeconds, double offsetSeconds) {
    }

    @Override
    public boolean equals(Object rhs) {
      return false;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public int compareTo(Callback rhs) {
      return 0;
    }
  }

  public static final double kDefaultPeriod = 0.02;

  protected TimedRobot() {
    this(kDefaultPeriod);
  }

  protected TimedRobot(double period) {
    super(period);
    DriverStation.reportError(
        "The main robot class must inherit from LoggedRobot when using AdvantageKit's WPILib shims. For more details, check the AdvantageKit installation documentation: https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/docs/INSTALLATION.md\n\n*** EXITING DUE TO INVALID ADVANTAGEKIT INSTALLATION, SEE ABOVE. ***",
        false);
    System.exit(1);
  }

  public void close() {
  }

  public void startCompetition() {
  }

  public void endCompetition() {
  }

  public void addPeriodic(Runnable callback, double periodSeconds) {
  }

  public void addPeriodic(Runnable callback, double periodSeconds, double offsetSeconds) {
  }
}