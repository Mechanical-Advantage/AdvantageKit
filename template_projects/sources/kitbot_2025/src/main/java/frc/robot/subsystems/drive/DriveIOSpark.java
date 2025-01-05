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

package frc.robot.subsystems.drive;

import static frc.robot.subsystems.drive.DriveConstants.*;
import static frc.robot.util.SparkUtil.*;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import java.util.function.DoubleSupplier;

/**
 * This drive implementation is for Spark devices. It defaults to brushless control, but can be
 * easily adapted for brushed motors and external encoders. Spark Flexes can be used by swapping all
 * instances of "SparkMax" with "SparkFlex".
 */
public class DriveIOSpark implements DriveIO {
  private final SparkMax leftLeader = new SparkMax(leftLeaderCanId, MotorType.kBrushless);
  private final SparkMax rightLeader = new SparkMax(rightLeaderCanId, MotorType.kBrushless);
  private final SparkMax leftFollower = new SparkMax(leftFollowerCanId, MotorType.kBrushless);
  private final SparkMax rightFollower = new SparkMax(rightFollowerCanId, MotorType.kBrushless);
  private final RelativeEncoder leftEncoder = leftLeader.getEncoder();
  private final RelativeEncoder rightEncoder = rightLeader.getEncoder();
  private final SparkClosedLoopController leftController = leftLeader.getClosedLoopController();
  private final SparkClosedLoopController rightController = rightLeader.getClosedLoopController();

  public DriveIOSpark() {
    // Create config
    var config = new SparkMaxConfig();
    config.idleMode(IdleMode.kBrake).smartCurrentLimit(currentLimit).voltageCompensation(12.0);
    config.closedLoop.pidf(realKp, 0.0, realKd, 0.0);
    config
        .encoder
        .positionConversionFactor(2 * Math.PI / motorReduction) // Rotor Rotations -> Wheel Radians
        .velocityConversionFactor(
            (2 * Math.PI) / 60.0 / motorReduction) // Rotor RPM -> Wheel Rad/Sec
        .uvwMeasurementPeriod(10)
        .uvwAverageDepth(2);

    // Apply config to leaders
    config.inverted(leftInverted);
    tryUntilOk(
        leftLeader,
        5,
        () ->
            leftLeader.configure(
                config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters));
    config.inverted(rightInverted);
    tryUntilOk(
        rightLeader,
        5,
        () ->
            rightLeader.configure(
                config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters));

    // Apply config to followers
    config.inverted(leftInverted).follow(leftLeader);
    tryUntilOk(
        leftFollower,
        5,
        () ->
            leftFollower.configure(
                config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters));
    config.inverted(rightInverted).follow(rightLeader);
    tryUntilOk(
        rightFollower,
        5,
        () ->
            rightFollower.configure(
                config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters));
  }

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    ifOk(leftLeader, leftEncoder::getPosition, (value) -> inputs.leftPositionRad = value);
    ifOk(leftLeader, leftEncoder::getVelocity, (value) -> inputs.leftVelocityRadPerSec = value);
    ifOk(
        leftLeader,
        new DoubleSupplier[] {leftLeader::getAppliedOutput, leftLeader::getBusVoltage},
        (values) -> inputs.leftAppliedVolts = values[0] * values[1]);
    ifOk(
        leftLeader,
        new DoubleSupplier[] {leftLeader::getOutputCurrent, leftLeader::getOutputCurrent},
        (values) -> inputs.leftCurrentAmps = values);

    ifOk(rightLeader, rightEncoder::getPosition, (value) -> inputs.rightPositionRad = value);
    ifOk(rightLeader, rightEncoder::getVelocity, (value) -> inputs.rightVelocityRadPerSec = value);
    ifOk(
        rightLeader,
        new DoubleSupplier[] {rightLeader::getAppliedOutput, rightLeader::getBusVoltage},
        (values) -> inputs.rightAppliedVolts = values[0] * values[1]);
    ifOk(
        rightLeader,
        new DoubleSupplier[] {rightLeader::getOutputCurrent, rightLeader::getOutputCurrent},
        (values) -> inputs.rightCurrentAmps = values);
  }

  @Override
  public void setVoltage(double leftVolts, double rightVolts) {
    leftLeader.setVoltage(leftVolts);
    rightLeader.setVoltage(rightVolts);
  }

  @Override
  public void setVelocity(
      double leftRadPerSec, double rightRadPerSec, double leftFFVolts, double rightFFVolts) {
    leftController.setReference(
        leftRadPerSec, ControlType.kVelocity, ClosedLoopSlot.kSlot0, leftFFVolts);
    rightController.setReference(
        rightRadPerSec, ControlType.kVelocity, ClosedLoopSlot.kSlot0, rightFFVolts);
  }
}
