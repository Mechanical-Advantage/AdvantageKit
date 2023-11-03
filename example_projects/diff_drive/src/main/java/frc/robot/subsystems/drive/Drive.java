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

package frc.robot.subsystems.drive;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.Logger;

public class Drive extends SubsystemBase {
  public static final double WHEEL_RADIUS = Units.inchesToMeters(3.0);

  private final DriveIO io;
  private final DriveIOInputsAutoLogged inputs = new DriveIOInputsAutoLogged();
  private final DifferentialDriveOdometry odometry =
      new DifferentialDriveOdometry(new Rotation2d(), 0.0, 0.0);

  /** Creates a new Drive. */
  public Drive(DriveIO io) {
    this.io = io;
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Drive", inputs);

    // Update odometry and log the new pose
    odometry.update(inputs.gyroYaw, getLeftPositionMeters(), getRightPositionMeters());
    Logger.recordOutput("Odometry", getPose());
  }

  /** Run open loop at the specified percentage. */
  public void drivePercent(double leftPercent, double rightPercent) {
    io.setVoltage(leftPercent * 12.0, rightPercent * 12.0);
  }

  /** Run open loop based on stick positions. */
  public void driveArcade(double xSpeed, double zRotation) {
    var speeds = DifferentialDrive.arcadeDriveIK(xSpeed, zRotation, true);
    io.setVoltage(speeds.left * 12.0, speeds.right * 12.0);
  }

  /** Stops the drive. */
  public void stop() {
    io.setVoltage(0.0, 0.0);
  }

  /** Returns the current odometry pose in meters. */
  public Pose2d getPose() {
    return odometry.getPoseMeters();
  }

  /** Resets the current odometry pose. */
  public void setPose(Pose2d pose) {
    odometry.resetPosition(inputs.gyroYaw, getLeftPositionMeters(), getRightPositionMeters(), pose);
  }

  /** Returns the position of the left wheels in meters. */
  public double getLeftPositionMeters() {
    return inputs.leftPositionRad * WHEEL_RADIUS;
  }

  /** Returns the position of the right wheels in meters. */
  public double getRightPositionMeters() {
    return inputs.rightPositionRad * WHEEL_RADIUS;
  }

  /** Returns the velocity of the left wheels in meters/second. */
  public double getLeftVelocityMeters() {
    return inputs.leftVelocityRadPerSec * WHEEL_RADIUS;
  }

  /** Returns the velocity of the right wheels in meters/second. */
  public double getRightVelocityMeters() {
    return inputs.rightVelocityRadPerSec * WHEEL_RADIUS;
  }
}
