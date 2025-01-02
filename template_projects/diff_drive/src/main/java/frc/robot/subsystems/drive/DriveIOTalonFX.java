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
import static frc.robot.util.PhoenixUtil.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/** This drive implementation is for Talon FXs driving motors like the Falon 500 or Kraken X60. */
public class DriveIOTalonFX implements DriveIO {
  private final TalonFX leftLeader = new TalonFX(leftLeaderCanId);
  private final TalonFX leftFollower = new TalonFX(leftFollowerCanId);
  private final TalonFX rightLeader = new TalonFX(rightLeaderCanId);
  private final TalonFX rightFollower = new TalonFX(rightFollowerCanId);

  private final StatusSignal<Angle> leftPosition = leftLeader.getPosition();
  private final StatusSignal<AngularVelocity> leftVelocity = leftLeader.getVelocity();
  private final StatusSignal<Voltage> leftAppliedVolts = leftLeader.getMotorVoltage();
  private final StatusSignal<Current> leftLeaderCurrent = leftLeader.getSupplyCurrent();
  private final StatusSignal<Current> leftFollowerCurrent = leftFollower.getSupplyCurrent();

  private final StatusSignal<Angle> rightPosition = rightLeader.getPosition();
  private final StatusSignal<AngularVelocity> rightVelocity = rightLeader.getVelocity();
  private final StatusSignal<Voltage> rightAppliedVolts = rightLeader.getMotorVoltage();
  private final StatusSignal<Current> rightLeaderCurrent = rightLeader.getSupplyCurrent();
  private final StatusSignal<Current> rightFollowerCurrent = rightFollower.getSupplyCurrent();

  private VoltageOut voltageRequest = new VoltageOut(0.0);
  private VelocityVoltage velocityRequest = new VelocityVoltage(0.0);

  public DriveIOTalonFX() {
    var config = new TalonFXConfiguration();
    config.CurrentLimits.SupplyCurrentLimit = currentLimit;
    config.CurrentLimits.SupplyCurrentLimitEnable = true;
    config.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    config.Feedback.SensorToMechanismRatio = motorReduction;
    config.Slot0.kP = realKp;
    config.Slot0.kD = realKd;

    config.MotorOutput.Inverted =
        leftInverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
    tryUntilOk(5, () -> leftLeader.getConfigurator().apply(config, 0.25));
    tryUntilOk(5, () -> leftFollower.getConfigurator().apply(config, 0.25));

    config.MotorOutput.Inverted =
        rightInverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
    tryUntilOk(5, () -> rightLeader.getConfigurator().apply(config, 0.25));
    tryUntilOk(5, () -> rightFollower.getConfigurator().apply(config, 0.25));

    leftFollower.setControl(new Follower(leftLeader.getDeviceID(), false));
    rightFollower.setControl(new Follower(rightLeader.getDeviceID(), false));

    BaseStatusSignal.setUpdateFrequencyForAll(
        50.0,
        leftPosition,
        leftVelocity,
        leftAppliedVolts,
        leftLeaderCurrent,
        leftFollowerCurrent,
        rightPosition,
        rightVelocity,
        rightAppliedVolts,
        rightLeaderCurrent,
        rightFollowerCurrent);
    leftLeader.optimizeBusUtilization();
    leftFollower.optimizeBusUtilization();
    rightLeader.optimizeBusUtilization();
    rightFollower.optimizeBusUtilization();
  }

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    BaseStatusSignal.refreshAll(
        leftPosition,
        leftVelocity,
        leftAppliedVolts,
        leftLeaderCurrent,
        leftFollowerCurrent,
        rightPosition,
        rightVelocity,
        rightAppliedVolts,
        rightLeaderCurrent,
        rightFollowerCurrent);

    inputs.leftPositionRad = Units.rotationsToRadians(leftPosition.getValueAsDouble());
    inputs.leftVelocityRadPerSec = Units.rotationsToRadians(leftVelocity.getValueAsDouble());
    inputs.leftAppliedVolts = leftAppliedVolts.getValueAsDouble();
    inputs.leftCurrentAmps =
        new double[] {leftLeaderCurrent.getValueAsDouble(), leftFollowerCurrent.getValueAsDouble()};

    inputs.rightPositionRad = Units.rotationsToRadians(rightPosition.getValueAsDouble());
    inputs.rightVelocityRadPerSec = Units.rotationsToRadians(rightVelocity.getValueAsDouble());
    inputs.rightAppliedVolts = rightAppliedVolts.getValueAsDouble();
    inputs.rightCurrentAmps =
        new double[] {
          rightLeaderCurrent.getValueAsDouble(), rightFollowerCurrent.getValueAsDouble()
        };
  }

  @Override
  public void setVoltage(double leftVolts, double rightVolts) {
    leftLeader.setControl(voltageRequest.withOutput(leftVolts));
    rightLeader.setControl(voltageRequest.withOutput(rightVolts));
  }

  @Override
  public void setVelocity(
      double leftRadPerSec, double rightRadPerSec, double leftFFVolts, double rightFFVolts) {
    leftLeader.setControl(
        velocityRequest
            .withVelocity(Units.radiansToRotations(leftRadPerSec))
            .withFeedForward(leftFFVolts));
    rightLeader.setControl(
        velocityRequest
            .withVelocity(Units.radiansToRotations(rightRadPerSec))
            .withFeedForward(rightFFVolts));
  }
}
