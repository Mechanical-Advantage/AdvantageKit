// Copyright 2021-2023 FRC 6328
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

import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Pigeon2Configuration;
import com.ctre.phoenix6.hardware.Pigeon2;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;

public class DriveIOSparkMax implements DriveIO {
  private static final double GEAR_RATIO = 6.0;

  private final CANSparkMax leftLeader = new CANSparkMax(1, MotorType.kBrushless);
  private final CANSparkMax rightLeader = new CANSparkMax(2, MotorType.kBrushless);
  private final CANSparkMax leftFollower = new CANSparkMax(3, MotorType.kBrushless);
  private final CANSparkMax rightFollower = new CANSparkMax(4, MotorType.kBrushless);
  private final RelativeEncoder leftEncoder = leftLeader.getEncoder();
  private final RelativeEncoder rightEncoder = rightLeader.getEncoder();

  private final Pigeon2 pigeon = new Pigeon2(20);
  private final StatusSignal<Double> yaw = pigeon.getYaw();

  public DriveIOSparkMax() {
    leftLeader.restoreFactoryDefaults();
    rightLeader.restoreFactoryDefaults();
    leftFollower.restoreFactoryDefaults();
    rightFollower.restoreFactoryDefaults();

    leftLeader.setCANTimeout(250);
    rightLeader.setCANTimeout(250);
    leftFollower.setCANTimeout(250);
    rightFollower.setCANTimeout(250);

    leftLeader.setInverted(false);
    rightLeader.setInverted(true);
    leftFollower.follow(leftLeader, false);
    rightFollower.follow(rightLeader, false);

    leftLeader.enableVoltageCompensation(12.0);
    rightLeader.enableVoltageCompensation(12.0);
    leftLeader.setSmartCurrentLimit(30);
    rightLeader.setSmartCurrentLimit(30);

    leftLeader.burnFlash();
    rightLeader.burnFlash();
    leftFollower.burnFlash();
    rightFollower.burnFlash();

    pigeon.getConfigurator().apply(new Pigeon2Configuration());
    pigeon.getConfigurator().setYaw(0.0);
    yaw.setUpdateFrequency(100.0);
    pigeon.optimizeBusUtilization();
  }

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    inputs.leftPositionRad = Units.rotationsToRadians(leftEncoder.getPosition() / GEAR_RATIO);
    inputs.leftVelocityRadPerSec =
        Units.rotationsPerMinuteToRadiansPerSecond(leftEncoder.getVelocity() / GEAR_RATIO);
    inputs.leftAppliedVolts = leftLeader.getAppliedOutput() * leftLeader.getBusVoltage();
    inputs.leftCurrentAmps =
        new double[] {leftLeader.getOutputCurrent(), leftFollower.getOutputCurrent()};

    inputs.rightPositionRad = Units.rotationsToRadians(rightEncoder.getPosition() / GEAR_RATIO);
    inputs.rightVelocityRadPerSec =
        Units.rotationsPerMinuteToRadiansPerSecond(rightEncoder.getVelocity() / GEAR_RATIO);
    inputs.leftAppliedVolts = rightLeader.getAppliedOutput() * rightLeader.getBusVoltage();
    inputs.leftCurrentAmps =
        new double[] {rightLeader.getOutputCurrent(), rightFollower.getOutputCurrent()};

    inputs.gyroYaw = Rotation2d.fromDegrees(-yaw.refresh().getValueAsDouble());
  }

  @Override
  public void setVoltage(double leftVolts, double rightVolts) {
    leftLeader.setVoltage(leftVolts);
    rightLeader.setVoltage(rightVolts);
  }
}
