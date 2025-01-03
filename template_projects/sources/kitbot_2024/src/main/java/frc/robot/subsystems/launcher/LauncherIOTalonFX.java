// Copyright 2021-2025 FRC 6328
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

import static frc.robot.util.PhoenixUtil.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/**
 * This drive implementation is for Talon FXs driving brushless motors like the Falon 500 or Kraken
 * X60.
 */
public class LauncherIOTalonFX implements LauncherIO {
  private final TalonFX launchMotor = new TalonFX(10);
  private final TalonFX feedMotor = new TalonFX(11);

  private final StatusSignal<Angle> launchPosition = launchMotor.getPosition();
  private final StatusSignal<AngularVelocity> launchVelocity = launchMotor.getVelocity();
  private final StatusSignal<Voltage> launchAppliedVolts = launchMotor.getMotorVoltage();
  private final StatusSignal<Current> launchCurrent = launchMotor.getSupplyCurrent();

  private final StatusSignal<Angle> feedPosition = feedMotor.getPosition();
  private final StatusSignal<AngularVelocity> feedVelocity = feedMotor.getVelocity();
  private final StatusSignal<Voltage> feedAppliedVolts = feedMotor.getMotorVoltage();
  private final StatusSignal<Current> feedCurrent = feedMotor.getSupplyCurrent();

  public LauncherIOTalonFX() {
    var config = new TalonFXConfiguration();
    config.CurrentLimits.SupplyCurrentLimit = 80.0;
    config.CurrentLimits.SupplyCurrentLimitEnable = true;
    config.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    tryUntilOk(5, () -> launchMotor.getConfigurator().apply(config, 0.25));
    tryUntilOk(5, () -> feedMotor.getConfigurator().apply(config, 0.25));

    BaseStatusSignal.setUpdateFrequencyForAll(
        50.0,
        launchPosition,
        launchVelocity,
        launchAppliedVolts,
        launchCurrent,
        feedPosition,
        feedVelocity,
        feedAppliedVolts,
        feedCurrent);
    launchMotor.optimizeBusUtilization();
    feedMotor.optimizeBusUtilization();
  }

  @Override
  public void updateInputs(LauncherIOInputs inputs) {
    BaseStatusSignal.refreshAll(
        launchPosition,
        launchVelocity,
        launchAppliedVolts,
        launchCurrent,
        feedPosition,
        feedVelocity,
        feedAppliedVolts,
        feedCurrent);

    inputs.launchPositionRad = Units.rotationsToRadians(launchPosition.getValueAsDouble());
    inputs.launchVelocityRadPerSec = Units.rotationsToRadians(launchVelocity.getValueAsDouble());
    inputs.launchAppliedVolts = launchAppliedVolts.getValueAsDouble();
    inputs.launchCurrentAmps = launchCurrent.getValueAsDouble();

    inputs.feedPositionRad = Units.rotationsToRadians(feedPosition.getValueAsDouble());
    inputs.feedVelocityRadPerSec = Units.rotationsToRadians(feedVelocity.getValueAsDouble());
    inputs.feedAppliedVolts = feedAppliedVolts.getValueAsDouble();
    inputs.feedCurrentAmps = feedCurrent.getValueAsDouble();
  }

  @Override
  public void setLaunchVoltage(double volts) {
    launchMotor.setControl(new VoltageOut(volts));
  }

  @Override
  public void setFeedVoltage(double volts) {
    feedMotor.setControl(new VoltageOut(volts));
  }
}
