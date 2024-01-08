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

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim.KitbotGearing;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim.KitbotMotor;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim.KitbotWheelSize;

public class DriveIOSim implements DriveIO {
  private static final double KP = 0.2;
  private static final double KD = 0.0;
  private DifferentialDrivetrainSim sim =
      DifferentialDrivetrainSim.createKitbotSim(
          KitbotMotor.kDualCIMPerSide, KitbotGearing.k10p71, KitbotWheelSize.kSixInch, null);

  private double leftAppliedVolts = 0.0;
  private double rightAppliedVolts = 0.0;
  private boolean closedLoop = false;
  private PIDController leftPID = new PIDController(KP, 0.0, KD);
  private PIDController rightPID = new PIDController(KP, 0.0, KD);
  private double leftFFVolts = 0.0;
  private double rightFFVolts = 0.0;

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    if (closedLoop) {
      leftAppliedVolts =
          MathUtil.clamp(
              leftPID.calculate(sim.getLeftVelocityMetersPerSecond() / Drive.WHEEL_RADIUS)
                  + leftFFVolts,
              -12.0,
              12.0);
      rightAppliedVolts =
          MathUtil.clamp(
              leftPID.calculate(sim.getRightVelocityMetersPerSecond() / Drive.WHEEL_RADIUS)
                  + rightFFVolts,
              -12.0,
              12.0);
      sim.setInputs(leftAppliedVolts, rightAppliedVolts);
    }

    sim.update(0.02);
    inputs.leftPositionRad = sim.getLeftPositionMeters() / Drive.WHEEL_RADIUS;
    inputs.leftVelocityRadPerSec = sim.getLeftVelocityMetersPerSecond() / Drive.WHEEL_RADIUS;
    inputs.leftAppliedVolts = leftAppliedVolts;
    inputs.leftCurrentAmps = new double[] {sim.getLeftCurrentDrawAmps()};

    inputs.rightPositionRad = sim.getRightPositionMeters() / Drive.WHEEL_RADIUS;
    inputs.rightVelocityRadPerSec = sim.getRightVelocityMetersPerSecond() / Drive.WHEEL_RADIUS;
    inputs.rightAppliedVolts = rightAppliedVolts;
    inputs.rightCurrentAmps = new double[] {sim.getRightCurrentDrawAmps()};

    inputs.gyroYaw = sim.getHeading();
  }

  @Override
  public void setVoltage(double leftVolts, double rightVolts) {
    closedLoop = false;
    leftAppliedVolts = MathUtil.clamp(leftVolts, -12.0, 12.0);
    rightAppliedVolts = MathUtil.clamp(rightVolts, -12.0, 12.0);
    sim.setInputs(leftAppliedVolts, rightAppliedVolts);
  }

  @Override
  public void setVelocity(
      double leftRadPerSec, double rightRadPerSec, double leftFFVolts, double rightFFVolts) {
    closedLoop = true;
    leftPID.setSetpoint(leftRadPerSec);
    rightPID.setSetpoint(rightRadPerSec);
    this.leftFFVolts = leftFFVolts;
    this.rightFFVolts = rightFFVolts;
  }
}
