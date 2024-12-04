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

import static frc.robot.util.SparkUtil.*;

import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import java.util.function.DoubleSupplier;

/**
 * This drive implementation is for Spark Maxes driving brushed motors (e.g. CIMS) with no encoders.
 * For the Spark Flex in docked mode, replace all instances of "CANSparkMax" with "CANSparkFlex".
 */
public class LauncherIOSparkMaxBrushed implements LauncherIO {
  private final SparkMax launchMotor = new SparkMax(10, MotorType.kBrushed);
  private final SparkMax feedMotor = new SparkMax(11, MotorType.kBrushed);

  public LauncherIOSparkMaxBrushed() {
    var config = new SparkMaxConfig();
    config.voltageCompensation(12.0).smartCurrentLimit(80);

    tryUntilOk(
        launchMotor,
        5,
        () ->
            launchMotor.configure(
                config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters));
    tryUntilOk(
        feedMotor,
        5,
        () ->
            launchMotor.configure(
                config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters));
  }

  @Override
  public void updateInputs(LauncherIOInputs inputs) {
    ifOk(
        launchMotor,
        new DoubleSupplier[] {launchMotor::getAppliedOutput, launchMotor::getBusVoltage},
        (values) -> inputs.launchAppliedVolts = values[0] * values[1]);
    ifOk(launchMotor, launchMotor::getOutputCurrent, (value) -> inputs.launchCurrentAmps = value);

    ifOk(
        feedMotor,
        new DoubleSupplier[] {feedMotor::getAppliedOutput, launchMotor::getBusVoltage},
        (values) -> inputs.feedAppliedVolts = values[0] * values[1]);
    ifOk(feedMotor, feedMotor::getOutputCurrent, (value) -> inputs.feedCurrentAmps = value);
  }

  @Override
  public void setLaunchVoltage(double volts) {
    launchMotor.setVoltage(volts);
  }

  @Override
  public void setFeedVoltage(double volts) {
    feedMotor.setVoltage(volts);
  }
}
