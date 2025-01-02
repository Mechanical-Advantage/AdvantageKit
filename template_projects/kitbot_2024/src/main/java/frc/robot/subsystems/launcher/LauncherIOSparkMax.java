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

import static frc.robot.util.SparkUtil.*;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.math.util.Units;
import java.util.function.DoubleSupplier;

/**
 * This launcher implementation is for Spark Maxes driving NEO motors. For the Spark Flex/NEO
 * Vortex, replace all instances of "CANSparkMax" with "CANSparkFlex".
 */
public class LauncherIOSparkMax implements LauncherIO {
  private final SparkMax launchMotor = new SparkMax(10, MotorType.kBrushless);
  private final SparkMax feedMotor = new SparkMax(11, MotorType.kBrushless);
  private final RelativeEncoder launchEncoder = launchMotor.getEncoder();
  private final RelativeEncoder feedEncoder = feedMotor.getEncoder();

  public LauncherIOSparkMax() {
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
        launchEncoder::getPosition,
        (value) -> inputs.launchPositionRad = Units.rotationsToRadians(value));
    ifOk(
        launchMotor,
        launchEncoder::getVelocity,
        (value) ->
            inputs.launchVelocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(value));
    ifOk(
        launchMotor,
        new DoubleSupplier[] {launchMotor::getAppliedOutput, launchMotor::getBusVoltage},
        (values) -> inputs.launchAppliedVolts = values[0] * values[1]);
    ifOk(launchMotor, launchMotor::getOutputCurrent, (value) -> inputs.launchCurrentAmps = value);

    ifOk(
        feedMotor,
        feedEncoder::getPosition,
        (value) -> inputs.feedPositionRad = Units.rotationsToRadians(value));
    ifOk(
        feedMotor,
        feedEncoder::getVelocity,
        (value) ->
            inputs.feedVelocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(value));
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
