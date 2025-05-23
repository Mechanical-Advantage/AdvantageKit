// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.roller;

import static frc.robot.subsystems.roller.RollerConstants.*;
import static frc.robot.util.SparkUtil.*;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import java.util.function.DoubleSupplier;

/**
 * This roller implementation is for Spark devices. It defaults to brushless control, but can be
 * easily adapted for a brushed motor. A Spark Flex can be used by swapping all instances of
 * "SparkMax" with "SparkFlex".
 */
public class RollerIOSpark implements RollerIO {
  private final SparkMax roller = new SparkMax(rollerCanId, MotorType.kBrushless);
  private final RelativeEncoder encoder = roller.getEncoder();

  public RollerIOSpark() {
    var config = new SparkMaxConfig();
    config.idleMode(IdleMode.kBrake).smartCurrentLimit(currentLimit).voltageCompensation(12.0);
    config
        .encoder
        .positionConversionFactor(
            2.0 * Math.PI / motorReduction) // Rotor Rotations -> Roller Radians
        .velocityConversionFactor((2.0 * Math.PI) / 60.0 / motorReduction)
        .uvwMeasurementPeriod(10)
        .uvwAverageDepth(2);

    tryUntilOk(
        roller,
        5,
        () ->
            roller.configure(
                config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters));
  }

  @Override
  public void updateInputs(RollerIOInputs inputs) {
    ifOk(roller, encoder::getPosition, (value) -> inputs.positionRad = value);
    ifOk(roller, encoder::getVelocity, (value) -> inputs.velocityRadPerSec = value);
    ifOk(
        roller,
        new DoubleSupplier[] {roller::getAppliedOutput, roller::getBusVoltage},
        (values) -> inputs.appliedVolts = values[0] * values[1]);
    ifOk(roller, roller::getOutputCurrent, (value) -> inputs.currentAmps = value);
  }

  @Override
  public void setVoltage(double volts) {
    roller.setVoltage(volts);
  }
}
