// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.superstructure;

import static frc.robot.subsystems.superstructure.SuperstructureConstants.*;
import static frc.robot.util.PhoenixUtil.tryUntilOkV5;

import com.ctre.phoenix.motorcontrol.TalonSRXControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;

/** This superstructure implementation is for Talon SRXs driving brushed motors. */
public class SuperstructureIOTalonSRX implements SuperstructureIO {
  private final TalonSRX feeder = new TalonSRX(feederCanId);
  private final TalonSRX intakeLauncher = new TalonSRX(intakeLauncherCanId);

  public SuperstructureIOTalonSRX() {
    var feederConfig = new TalonSRXConfiguration();
    feederConfig.peakCurrentLimit = feederCurrentLimit;
    feederConfig.continuousCurrentLimit = feederCurrentLimit - 15;
    feederConfig.peakCurrentDuration = 250;
    feederConfig.voltageCompSaturation = 12.0;
    tryUntilOkV5(5, () -> feeder.configAllSettings(feederConfig));

    var intakeLauncherConfig = new TalonSRXConfiguration();
    intakeLauncherConfig.peakCurrentLimit = intakeLauncherCurrentLimit;
    intakeLauncherConfig.continuousCurrentLimit = intakeLauncherCurrentLimit - 15;
    intakeLauncherConfig.peakCurrentDuration = 250;
    intakeLauncherConfig.voltageCompSaturation = 12.0;
    tryUntilOkV5(5, () -> intakeLauncher.configAllSettings(intakeLauncherConfig));
    intakeLauncher.setInverted(true);
  }

  @Override
  public void updateInputs(SuperstructureIOInputs inputs) {
    inputs.feederAppliedVolts = feeder.getMotorOutputVoltage();
    inputs.feederCurrentAmps = feeder.getStatorCurrent();
    inputs.intakeLauncherAppliedVolts = intakeLauncher.getMotorOutputVoltage();
    inputs.intakeLauncherCurrentAmps = intakeLauncher.getStatorCurrent();
  }

  @Override
  public void setFeederVoltage(double volts) {
    feeder.set(TalonSRXControlMode.PercentOutput, volts / 12.0);
  }

  @Override
  public void setIntakeLauncherVoltage(double volts) {
    intakeLauncher.set(TalonSRXControlMode.PercentOutput, volts / 12.0);
  }
}
