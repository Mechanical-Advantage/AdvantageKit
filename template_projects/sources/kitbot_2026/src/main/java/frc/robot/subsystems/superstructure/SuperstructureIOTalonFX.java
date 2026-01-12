// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.superstructure;

import static frc.robot.subsystems.superstructure.SuperstructureConstants.*;
import static frc.robot.util.PhoenixUtil.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.ParentDevice;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/**
 * This superstructure implementation is for Talon FXs driving motors like the Falon 500, Kraken
 * X44, or Kraken X60.
 */
public class SuperstructureIOTalonFX implements SuperstructureIO {
  private final TalonFX feeder = new TalonFX(feederCanId);
  private final StatusSignal<Angle> feederPositionRot = feeder.getPosition();
  private final StatusSignal<AngularVelocity> feederVelocityRotPerSec = feeder.getVelocity();
  private final StatusSignal<Voltage> feederAppliedVolts = feeder.getMotorVoltage();
  private final StatusSignal<Current> feederCurrentAmps = feeder.getSupplyCurrent();

  private final TalonFX intakeLauncher = new TalonFX(intakeLauncherCanId);
  private final StatusSignal<Angle> intakeLauncherPositionRot = intakeLauncher.getPosition();
  private final StatusSignal<AngularVelocity> intakeLauncherVelocityRotPerSec =
      intakeLauncher.getVelocity();
  private final StatusSignal<Voltage> intakeLauncherAppliedVolts = intakeLauncher.getMotorVoltage();
  private final StatusSignal<Current> intakeLauncherCurrentAmps = intakeLauncher.getSupplyCurrent();

  private final VoltageOut voltageRequest = new VoltageOut(0.0);

  public SuperstructureIOTalonFX() {
    var feederConfig = new TalonFXConfiguration();
    feederConfig.CurrentLimits.SupplyCurrentLimit = feederCurrentLimit;
    feederConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
    feederConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    tryUntilOk(5, () -> feeder.getConfigurator().apply(feederConfig, 0.25));

    var intakeLauncherConfig = new TalonFXConfiguration();
    intakeLauncherConfig.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;
    intakeLauncherConfig.CurrentLimits.SupplyCurrentLimit = intakeLauncherCurrentLimit;
    intakeLauncherConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
    intakeLauncherConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    tryUntilOk(5, () -> intakeLauncher.getConfigurator().apply(intakeLauncherConfig, 0.25));

    BaseStatusSignal.setUpdateFrequencyForAll(
        50.0,
        feederPositionRot,
        feederVelocityRotPerSec,
        feederAppliedVolts,
        feederCurrentAmps,
        intakeLauncherPositionRot,
        intakeLauncherVelocityRotPerSec,
        intakeLauncherAppliedVolts,
        intakeLauncherCurrentAmps);
    ParentDevice.optimizeBusUtilizationForAll(feeder, intakeLauncher);
  }

  @Override
  public void updateInputs(SuperstructureIOInputs inputs) {
    BaseStatusSignal.refreshAll(
        feederPositionRot,
        feederVelocityRotPerSec,
        feederAppliedVolts,
        feederCurrentAmps,
        intakeLauncherPositionRot,
        intakeLauncherVelocityRotPerSec,
        intakeLauncherAppliedVolts,
        intakeLauncherCurrentAmps);

    inputs.feederPositionRad = Units.rotationsToRadians(feederPositionRot.getValueAsDouble());
    inputs.feederVelocityRadPerSec =
        Units.rotationsToRadians(feederVelocityRotPerSec.getValueAsDouble());
    inputs.feederAppliedVolts = feederAppliedVolts.getValueAsDouble();
    inputs.feederCurrentAmps = feederCurrentAmps.getValueAsDouble();
    inputs.intakeLauncherPositionRad =
        Units.rotationsToRadians(intakeLauncherPositionRot.getValueAsDouble());
    inputs.intakeLauncherVelocityRadPerSec =
        Units.rotationsToRadians(intakeLauncherVelocityRotPerSec.getValueAsDouble());
    inputs.intakeLauncherAppliedVolts = intakeLauncherAppliedVolts.getValueAsDouble();
    inputs.intakeLauncherCurrentAmps = intakeLauncherCurrentAmps.getValueAsDouble();
  }

  @Override
  public void setFeederVoltage(double volts) {
    feeder.setControl(voltageRequest.withOutput(volts));
  }

  @Override
  public void setIntakeLauncherVoltage(double volts) {
    intakeLauncher.setControl(voltageRequest.withOutput(volts));
  }
}
