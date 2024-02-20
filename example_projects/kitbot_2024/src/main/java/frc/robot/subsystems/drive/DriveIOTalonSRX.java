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

package frc.robot.subsystems.drive;

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

/**
 * This drive implementation is for Talon SRXs driving brushed motors (e.g. CIMS) with no encoders
 * and no gyro.
 */
public class DriveIOTalonSRX implements DriveIO {
  private final TalonSRX leftLeader = new TalonSRX(0);
  private final TalonSRX leftFollower = new TalonSRX(1);
  private final TalonSRX rightLeader = new TalonSRX(2);
  private final TalonSRX rightFollower = new TalonSRX(3);

  public DriveIOTalonSRX() {
    var config = new TalonSRXConfiguration();
    config.peakCurrentLimit = 60;
    config.peakCurrentDuration = 250;
    config.continuousCurrentLimit = 40;
    config.voltageCompSaturation = 12.0;
    leftLeader.configAllSettings(config);
    leftFollower.configAllSettings(config);
    rightLeader.configAllSettings(config);
    rightFollower.configAllSettings(config);

    leftFollower.follow(leftLeader);
    rightFollower.follow(rightLeader);
  }

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    inputs.leftAppliedVolts = leftLeader.getMotorOutputVoltage();
    inputs.leftCurrentAmps =
        new double[] {leftLeader.getSupplyCurrent(), leftFollower.getSupplyCurrent()};

    inputs.rightAppliedVolts = rightLeader.getMotorOutputVoltage();
    inputs.rightCurrentAmps =
        new double[] {rightLeader.getSupplyCurrent(), rightFollower.getSupplyCurrent()};
  }

  @Override
  public void setVoltage(double leftVolts, double rightVolts) {
    leftLeader.set(TalonSRXControlMode.PercentOutput, leftVolts * 12.0);
    rightLeader.set(TalonSRXControlMode.PercentOutput, rightVolts * 12.0);
  }

  @Override
  public void setVelocity(
      double leftRadPerSec, double rightRadPerSec, double leftFFVolts, double rightFFVolts) {
    // Ignore for brushed motors, don't assume that the encoders are wired
  }
}
