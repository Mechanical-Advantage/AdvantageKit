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

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

/**
 * This drive implementation is for Talon SRXs driving brushed motors (e.g. CIMS) with no encoders.
 */
public class LauncherIOTalonSRX implements LauncherIO {
  private final TalonSRX launchMotor = new TalonSRX(10);
  private final TalonSRX feedMotor = new TalonSRX(11);

  public LauncherIOTalonSRX() {
    var config = new TalonSRXConfiguration();
    config.peakCurrentLimit = 80;
    config.peakCurrentDuration = 250;
    config.continuousCurrentLimit = 60;
    config.voltageCompSaturation = 12.0;
    launchMotor.configAllSettings(config);
    feedMotor.configAllSettings(config);
  }

  @Override
  public void updateInputs(LauncherIOInputs inputs) {
    inputs.launchAppliedVolts = launchMotor.getMotorOutputVoltage();
    inputs.launchCurrentAmps = new double[] {launchMotor.getSupplyCurrent()};

    inputs.feedAppliedVolts = feedMotor.getMotorOutputVoltage();
    inputs.feedCurrentAmps = new double[] {feedMotor.getSupplyCurrent()};
  }

  @Override
  public void setLaunchVoltage(double volts) {
    launchMotor.set(TalonSRXControlMode.PercentOutput, volts * 12.0);
  }

  @Override
  public void setFeedVoltage(double volts) {
    feedMotor.set(TalonSRXControlMode.PercentOutput, volts * 12.0);
  }
}
