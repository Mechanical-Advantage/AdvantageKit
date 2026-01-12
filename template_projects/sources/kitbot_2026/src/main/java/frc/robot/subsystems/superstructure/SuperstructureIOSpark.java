// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.superstructure;

import static frc.robot.subsystems.superstructure.SuperstructureConstants.*;
import static frc.robot.util.SparkUtil.*;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import java.util.function.DoubleSupplier;

/**
 * This superstructure implementation is for Spark devices. It defaults to brushless control, but
 * can be easily adapted for a brushed motor. One or more Spark Flexes can be used by swapping
 * relevant instances of "SparkMax" with "SparkFlex".
 */
public class SuperstructureIOSpark implements SuperstructureIO {
  private final SparkMax feeder = new SparkMax(feederCanId, MotorType.kBrushless);
  private final SparkMax intakeLauncher = new SparkMax(intakeLauncherCanId, MotorType.kBrushless);
  private final RelativeEncoder feederEncoder = feeder.getEncoder();
  private final RelativeEncoder intakeLauncherEncoder = intakeLauncher.getEncoder();

  public SuperstructureIOSpark() {
    var feederConfig = new SparkMaxConfig();
    feederConfig
        .idleMode(IdleMode.kBrake)
        .smartCurrentLimit(feederCurrentLimit)
        .voltageCompensation(12.0);
    feederConfig
        .encoder
        .positionConversionFactor(
            2.0 * Math.PI / feederMotorReduction) // Rotor Rotations -> Roller Radians
        .velocityConversionFactor((2.0 * Math.PI) / 60.0 / feederMotorReduction)
        .uvwMeasurementPeriod(10)
        .uvwAverageDepth(2);
    tryUntilOk(
        feeder,
        5,
        () ->
            feeder.configure(
                feederConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters));

    var intakeLauncherConfig = new SparkMaxConfig();
    intakeLauncherConfig
        .idleMode(IdleMode.kBrake)
        .smartCurrentLimit(intakeLauncherCurrentLimit)
        .inverted(true)
        .voltageCompensation(12.0);
    intakeLauncherConfig
        .encoder
        .positionConversionFactor(
            2.0 * Math.PI / intakeLauncherMotorReduction) // Rotor Rotations -> Roller Radians
        .velocityConversionFactor((2.0 * Math.PI) / 60.0 / intakeLauncherMotorReduction)
        .uvwMeasurementPeriod(10)
        .uvwAverageDepth(2);
    tryUntilOk(
        intakeLauncher,
        5,
        () ->
            intakeLauncher.configure(
                intakeLauncherConfig,
                ResetMode.kResetSafeParameters,
                PersistMode.kPersistParameters));
  }

  @Override
  public void updateInputs(SuperstructureIOInputs inputs) {
    ifOk(feeder, feederEncoder::getPosition, (value) -> inputs.feederPositionRad = value);
    ifOk(feeder, feederEncoder::getVelocity, (value) -> inputs.feederVelocityRadPerSec = value);
    ifOk(
        feeder,
        new DoubleSupplier[] {feeder::getAppliedOutput, feeder::getBusVoltage},
        (values) -> inputs.feederAppliedVolts = values[0] * values[1]);
    ifOk(feeder, feeder::getOutputCurrent, (value) -> inputs.feederCurrentAmps = value);

    ifOk(
        intakeLauncher,
        intakeLauncherEncoder::getPosition,
        (value) -> inputs.intakeLauncherPositionRad = value);
    ifOk(
        intakeLauncher,
        intakeLauncherEncoder::getVelocity,
        (value) -> inputs.intakeLauncherVelocityRadPerSec = value);
    ifOk(
        intakeLauncher,
        new DoubleSupplier[] {intakeLauncher::getAppliedOutput, intakeLauncher::getBusVoltage},
        (values) -> inputs.intakeLauncherAppliedVolts = values[0] * values[1]);
    ifOk(
        intakeLauncher,
        intakeLauncher::getOutputCurrent,
        (value) -> inputs.intakeLauncherCurrentAmps = value);
  }

  @Override
  public void setFeederVoltage(double volts) {
    feeder.setVoltage(volts);
  }

  @Override
  public void setIntakeLauncherVoltage(double volts) {
    intakeLauncher.setVoltage(volts);
  }
}
