// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package first.robot.mechanisms.drive;

import static org.wpilib.units.Units.*;

import org.littletonrobotics.junction.AutoLog;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Voltage;

public interface ModuleIO {
  @AutoLog
  public static class ModuleIOInputs {
    public boolean driveConnected = false;
    public Angle drivePosition = Radians.of(Double.NaN);
    public AngularVelocity driveVelocity = RadiansPerSecond.of(Double.NaN);
    public Voltage driveAppliedVoltage = Volts.of(Double.NaN);
    public Current driveCurrent = Amps.of(Double.NaN);

    public boolean turnConnected = false;
    public boolean turnEncoderConnected = false;
    public Rotation2d turnAbsolutePosition = Rotation2d.ZERO;
    public Rotation2d turnPosition = Rotation2d.ZERO;
    public AngularVelocity turnVelocity = RadiansPerSecond.of(Double.NaN);
    public Voltage turnAppliedVoltage = Volts.of(Double.NaN);
    public Current turnCurrent = Amps.of(Double.NaN);

    public double[] odometryTimestamps = new double[] {};
    public Angle[] odometryDrivePositionsRad = new Angle[] {};
    public Rotation2d[] odometryTurnPositions = new Rotation2d[] {};
  }

  /** Updates the set of loggable inputs. */
  public default void updateInputs(ModuleIOInputs inputs) {}

  /** Run the drive motor at the specified open loop value. */
  public default void setDriveOpenLoop(double output) {}

  /** Run the turn motor at the specified open loop value. */
  public default void setTurnOpenLoop(double output) {}

  /** Run the drive motor at the specified velocity. */
  public default void setDriveVelocity(double velocityRadPerSec) {}

  /** Run the turn motor to the specified rotation. */
  public default void setTurnPosition(Rotation2d rotation) {}
}
