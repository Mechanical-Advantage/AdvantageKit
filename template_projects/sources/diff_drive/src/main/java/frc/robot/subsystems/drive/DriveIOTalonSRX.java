// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.drive;

import static frc.robot.subsystems.drive.DriveConstants.*;
import static frc.robot.util.PhoenixUtil.*;

import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;
import edu.wpi.first.math.util.Units;

/** This drive implementation is for Talon SRXs driving brushed motors (e.g. CIMS) with encoders. */
public class DriveIOTalonSRX implements DriveIO {
  private static final double ticksPerRevolution = 1440;

  private final TalonSRX leftLeader = new TalonSRX(leftLeaderCanId);
  private final TalonSRX leftFollower = new TalonSRX(leftFollowerCanId);
  private final TalonSRX rightLeader = new TalonSRX(rightLeaderCanId);
  private final TalonSRX rightFollower = new TalonSRX(rightFollowerCanId);

  public DriveIOTalonSRX() {
    var config = new TalonSRXConfiguration();
    config.peakCurrentLimit = currentLimit;
    config.continuousCurrentLimit = currentLimit - 15;
    config.peakCurrentDuration = 250;
    config.voltageCompSaturation = 12.0;
    config.primaryPID.selectedFeedbackSensor = FeedbackDevice.QuadEncoder;

    tryUntilOkV5(5, () -> leftLeader.configAllSettings(config));
    tryUntilOkV5(5, () -> leftFollower.configAllSettings(config));
    tryUntilOkV5(5, () -> rightLeader.configAllSettings(config));
    tryUntilOkV5(5, () -> rightFollower.configAllSettings(config));

    leftLeader.setInverted(leftInverted);
    rightLeader.setInverted(rightInverted);

    leftFollower.follow(leftLeader);
    rightFollower.follow(rightLeader);
  }

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    inputs.leftPositionRad =
        Units.rotationsToRadians(leftLeader.getSelectedSensorPosition() / ticksPerRevolution);
    inputs.leftVelocityRadPerSec =
        Units.rotationsToRadians(
            leftLeader.getSelectedSensorVelocity()
                / ticksPerRevolution
                * 10.0); // Raw units are ticks per 100ms :(
    inputs.leftAppliedVolts = leftLeader.getMotorOutputVoltage();
    inputs.leftCurrentAmps =
        new double[] {leftLeader.getStatorCurrent(), leftFollower.getStatorCurrent()};

    inputs.rightPositionRad =
        Units.rotationsToRadians(rightLeader.getSelectedSensorPosition() / ticksPerRevolution);
    inputs.rightVelocityRadPerSec =
        Units.rotationsToRadians(
            rightLeader.getSelectedSensorVelocity()
                / ticksPerRevolution
                * 10.0); // Raw units are ticks per 100ms :(
    inputs.rightAppliedVolts = rightLeader.getMotorOutputVoltage();
    inputs.rightCurrentAmps =
        new double[] {rightLeader.getStatorCurrent(), rightFollower.getStatorCurrent()};
  }

  @Override
  public void setVoltage(double leftVolts, double rightVolts) {
    // OK to just divide by 12 because voltage compensation is enabled
    leftLeader.set(TalonSRXControlMode.PercentOutput, leftVolts / 12.0);
    rightLeader.set(TalonSRXControlMode.PercentOutput, rightVolts / 12.0);
  }

  @Override
  public void setVelocity(
      double leftRadPerSec, double rightRadPerSec, double leftFFVolts, double rightFFVolts) {
    // OK to just divide FF by 12 because voltage compensation is enabled
    leftLeader.set(
        TalonSRXControlMode.Velocity,
        Units.radiansToRotations(leftRadPerSec)
            * ticksPerRevolution
            / 10.0, // Raw units are ticks per 100ms :(
        DemandType.ArbitraryFeedForward,
        leftFFVolts / 12.0);
    rightLeader.set(
        TalonSRXControlMode.Velocity,
        Units.radiansToRotations(rightRadPerSec)
            * ticksPerRevolution
            / 10.0, // Raw units are ticks per 100ms :(
        DemandType.ArbitraryFeedForward,
        rightFFVolts / 12.0);
  }
}
