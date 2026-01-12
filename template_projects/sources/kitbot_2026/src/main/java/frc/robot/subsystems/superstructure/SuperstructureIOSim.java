// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.superstructure;

import static frc.robot.subsystems.superstructure.SuperstructureConstants.*;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.wpilibj.simulation.DCMotorSim;

public class SuperstructureIOSim implements SuperstructureIO {
  private DCMotorSim feederSim =
      new DCMotorSim(
          LinearSystemId.createDCMotorSystem(DCMotor.getCIM(1), 0.004, feederMotorReduction),
          DCMotor.getCIM(1));
  private DCMotorSim intakeLauncherSim =
      new DCMotorSim(
          LinearSystemId.createDCMotorSystem(
              DCMotor.getCIM(1), 0.004, intakeLauncherMotorReduction),
          DCMotor.getCIM(1));

  private double feederAppliedVolts = 0.0;
  private double intakeLauncherAppliedVolts = 0.0;

  @Override
  public void updateInputs(SuperstructureIOInputs inputs) {
    feederSim.setInputVoltage(feederAppliedVolts);
    feederSim.update(0.02);

    intakeLauncherSim.setInputVoltage(intakeLauncherAppliedVolts);
    intakeLauncherSim.update(0.02);

    inputs.feederPositionRad = feederSim.getAngularPositionRad();
    inputs.feederVelocityRadPerSec = feederSim.getAngularVelocityRadPerSec();
    inputs.feederAppliedVolts = feederAppliedVolts;
    inputs.feederCurrentAmps = feederSim.getCurrentDrawAmps();

    inputs.intakeLauncherPositionRad = intakeLauncherSim.getAngularPositionRad();
    inputs.intakeLauncherVelocityRadPerSec = intakeLauncherSim.getAngularVelocityRadPerSec();
    inputs.intakeLauncherAppliedVolts = intakeLauncherAppliedVolts;
    inputs.intakeLauncherCurrentAmps = intakeLauncherSim.getCurrentDrawAmps();
  }

  @Override
  public void setFeederVoltage(double volts) {
    feederAppliedVolts = MathUtil.clamp(volts, -12.0, 12.0);
  }

  @Override
  public void setIntakeLauncherVoltage(double volts) {
    intakeLauncherAppliedVolts = MathUtil.clamp(volts, -12.0, 12.0);
  }
}
