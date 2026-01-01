// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.roller;

import static frc.robot.subsystems.roller.RollerConstants.*;
import static frc.robot.util.PhoenixUtil.*;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/**
 * This roller implementation is for a Talon FX driving a motor like the Falon 500 or Kraken X60.
 */
public class RollerIOTalonFX implements RollerIO {
  private final TalonFX roller = new TalonFX(rollerCanId);
  private final StatusSignal<Angle> positionRot = roller.getPosition();
  private final StatusSignal<AngularVelocity> velocityRotPerSec = roller.getVelocity();
  private final StatusSignal<Voltage> appliedVolts = roller.getMotorVoltage();
  private final StatusSignal<Current> currentAmps = roller.getSupplyCurrent();

  private final VoltageOut voltageRequest = new VoltageOut(0.0);

  public RollerIOTalonFX() {
    var config = new TalonFXConfiguration();
    config.CurrentLimits.SupplyCurrentLimit = currentLimit;
    config.CurrentLimits.SupplyCurrentLimitEnable = true;
    config.MotorOutput.NeutralMode = NeutralModeValue.Brake;

    tryUntilOk(5, () -> roller.getConfigurator().apply(config, 0.25));

    BaseStatusSignal.setUpdateFrequencyForAll(
        50.0, positionRot, velocityRotPerSec, appliedVolts, currentAmps);
    roller.optimizeBusUtilization();
  }

  @Override
  public void updateInputs(RollerIOInputs inputs) {
    BaseStatusSignal.refreshAll(positionRot, velocityRotPerSec, appliedVolts, currentAmps);

    inputs.positionRad = Units.rotationsToRadians(positionRot.getValueAsDouble());
    inputs.velocityRadPerSec = Units.rotationsToRadians(velocityRotPerSec.getValueAsDouble());
    inputs.appliedVolts = appliedVolts.getValueAsDouble();
    inputs.currentAmps = currentAmps.getValueAsDouble();
  }

  @Override
  public void setVoltage(double volts) {
    roller.setControl(voltageRequest.withOutput(volts));
  }
}
