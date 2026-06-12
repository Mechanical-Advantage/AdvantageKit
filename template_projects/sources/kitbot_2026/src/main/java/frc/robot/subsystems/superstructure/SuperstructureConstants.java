// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.superstructure;

public class SuperstructureConstants {
  public static final int feederCanId = 5;
  public static final double feederMotorReduction = 1.0;
  public static final int feederCurrentLimit = 60;

  public static final int intakeLauncherCanId = 6;
  public static final double intakeLauncherMotorReduction = 1.0;
  public static final int intakeLauncherCurrentLimit = 60;

  public static final double intakingFeederVoltage = -12.0;
  public static final double intakingIntakeVoltage = 10.0;
  public static final double launchingFeederVoltage = 9.0;
  public static final double launchingLauncherVoltage = 10.6;
  public static final double spinUpFeederVoltage = -6.0;
  public static final double spinUpSeconds = 1.0;
}
