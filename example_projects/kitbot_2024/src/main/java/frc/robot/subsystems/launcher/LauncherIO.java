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

package frc.robot.subsystems.launcher;

import org.littletonrobotics.junction.AutoLog;

public interface LauncherIO {
  @AutoLog
  public static class LauncherIOInputs {
    public double launchPositionRad = 0.0;
    public double launchVelocityRadPerSec = 0.0;
    public double launchAppliedVolts = 0.0;
    public double[] launchCurrentAmps = new double[] {};

    public double feedPositionRad = 0.0;
    public double feedVelocityRadPerSec = 0.0;
    public double feedAppliedVolts = 0.0;
    public double[] feedCurrentAmps = new double[] {};
  }

  /** Updates the set of loggable inputs. */
  public default void updateInputs(LauncherIOInputs inputs) {}

  /** Run the launcher wheel at the specified voltage. */
  public default void setLaunchVoltage(double volts) {}

  /** Run the feeder wheel at the specified voltage. */
  public default void setFeedVoltage(double volts) {}
}
