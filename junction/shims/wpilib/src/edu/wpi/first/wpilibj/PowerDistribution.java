// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

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

package edu.wpi.first.wpilibj;

import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.PowerDistributionFaults;
import edu.wpi.first.hal.PowerDistributionJNI;
import edu.wpi.first.hal.PowerDistributionStickyFaults;
import edu.wpi.first.hal.PowerDistributionVersion;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;

import org.littletonrobotics.junction.inputs.LoggedPowerDistribution;

/**
 * Class for getting voltage, current, temperature, power and energy from the
 * CTRE Power Distribution Panel (PDP) or REV Power Distribution Hub (PDH) over
 * CAN. Patched by AdvantageKit to support logging.
 */
public class PowerDistribution implements Sendable, AutoCloseable {

  public static final int kDefaultModule = PowerDistributionJNI.DEFAULT_MODULE;

  public enum ModuleType {
    kAutomatic(PowerDistributionJNI.AUTOMATIC_TYPE),
    kCTRE(PowerDistributionJNI.CTRE_TYPE),
    kRev(PowerDistributionJNI.REV_TYPE);

    public final int value;

    ModuleType(int value) {
      this.value = value;
    }
  }

  /**
   * Constructs a PowerDistribution object.
   *
   * @param module     The CAN ID of the PDP/PDH.
   * @param moduleType Module type (CTRE or REV).
   */
  public PowerDistribution(int module, ModuleType moduleType) {
    LoggedPowerDistribution.getInstance(module, moduleType);
    int m_module = LoggedPowerDistribution.getInstance().getInputs().moduleId;
    HAL.report(tResourceType.kResourceType_PDP, m_module + 1);
    SendableRegistry.addLW(this, "PowerDistribution", m_module);
  }

  /**
   * Constructs a PowerDistribution object.
   *
   * <p>
   * Detects the connected PDP/PDH using the default CAN ID (0 for CTRE and 1 for
   * REV).
   */
  public PowerDistribution() {
    LoggedPowerDistribution.getInstance(kDefaultModule, ModuleType.kAutomatic);
    int m_module = LoggedPowerDistribution.getInstance().getInputs().moduleId;
    HAL.report(tResourceType.kResourceType_PDP, m_module + 1);
    SendableRegistry.addLW(this, "PowerDistribution", m_module);
  }

  @Override
  public void close() {
    SendableRegistry.remove(this);
  }

  /**
   * Gets the number of channels for this power distribution object.
   *
   * @return Number of output channels (16 for PDP, 24 for PDH).
   */
  public int getNumChannels() {
    return LoggedPowerDistribution.getInstance().getInputs().channelCount;
  }

  /**
   * Query the input voltage of the PDP/PDH.
   *
   * @return The voltage in volts
   */
  public double getVoltage() {
    return LoggedPowerDistribution.getInstance().getInputs().pdpVoltage;
  }

  /**
   * Query the temperature of the PDP/PDH.
   *
   * @return The temperature in degrees Celsius
   */
  public double getTemperature() {
    return LoggedPowerDistribution.getInstance().getInputs().pdpTemperature;
  }

  /**
   * Query the current of a single channel of the PDP/PDH.
   *
   * @param channel The channel (0-15 for PDP, 0-23 for PDH) to query
   * @return The current of the channel in Amperes
   */
  public double getCurrent(int channel) {
    double current = LoggedPowerDistribution.getInstance().getInputs().pdpChannelCurrents[channel];

    return current;
  }

  /**
   * Query the current of all monitored channels.
   *
   * @return The current of all the channels in Amperes
   */
  public double getTotalCurrent() {
    return LoggedPowerDistribution.getInstance().getInputs().pdpTotalCurrent;
  }

  /**
   * Query the total power drawn from the monitored channels.
   *
   * @return the total power in Watts
   */
  public double getTotalPower() {
    return LoggedPowerDistribution.getInstance().getInputs().pdpTotalPower;
  }

  /**
   * Query the total energy drawn from the monitored channels.
   *
   * @return the total energy in Joules
   */
  public double getTotalEnergy() {
    return LoggedPowerDistribution.getInstance().getInputs().pdpTotalEnergy;
  }

  /** Reset the total energy to 0. */
  public void resetTotalEnergy() {
    int handle = LoggedPowerDistribution.getInstance().getInputs().handle;
    if (handle == 0) {
      return;
    }

    PowerDistributionJNI.resetTotalEnergy(handle);
  }

  /** Clear all PDP/PDH sticky faults. */
  public void clearStickyFaults() {
    int handle = LoggedPowerDistribution.getInstance().getInputs().handle;
    if (handle == 0) {
      return;
    }
    PowerDistributionJNI.clearStickyFaults(handle);
  }

  /**
   * Gets module number (CAN ID).
   *
   * @return The module number (CAN ID).
   */
  public int getModule() {
    return LoggedPowerDistribution.getInstance().getInputs().moduleId;
  }

  /**
   * Gets the module type for this power distribution object.
   *
   * @return The module type
   */
  public ModuleType getType() {
    int handle = LoggedPowerDistribution.getInstance().getInputs().handle;
    if (handle == 0) {
      return ModuleType.kCTRE;
    }

    int type = PowerDistributionJNI.getType(handle);
    if (type == PowerDistributionJNI.REV_TYPE) {
      return ModuleType.kRev;
    } else {
      return ModuleType.kCTRE;
    }
  }

  /**
   * Gets whether the PDH switchable channel is turned on or off. Returns false
   * with the CTRE PDP.
   *
   * @return The output state of the PDH switchable channel
   */
  public boolean getSwitchableChannel() {
    int handle = LoggedPowerDistribution.getInstance().getInputs().handle;
    if (handle == 0) {
      return false;
    }

    return PowerDistributionJNI.getSwitchableChannel(handle);
  }

  /**
   * Sets the PDH switchable channel on or off. Does nothing with the CTRE PDP.
   *
   * @param enabled Whether to turn the PDH switchable channel on or off
   */
  public void setSwitchableChannel(boolean enabled) {
    int handle = LoggedPowerDistribution.getInstance().getInputs().handle;
    if (handle == 0) {
      return;
    }

    PowerDistributionJNI.setSwitchableChannel(handle, enabled);
  }

  public PowerDistributionVersion getVersion() {
    int handle = LoggedPowerDistribution.getInstance().getInputs().handle;
    if (handle == 0) {
      return new PowerDistributionVersion(0, 0, 0, 0, 0, 0);
    }

    return PowerDistributionJNI.getVersion(handle);
  }

  public PowerDistributionFaults getFaults() {
    long faults = LoggedPowerDistribution.getInstance().getInputs().faults;
    return new PowerDistributionFaults((int) faults);
  }

  public PowerDistributionStickyFaults getStickyFaults() {
    long stickyFaults = LoggedPowerDistribution.getInstance().getInputs().stickyFaults;
    return new PowerDistributionStickyFaults((int) stickyFaults);
  }

  @Override
  public void initSendable(SendableBuilder builder) {
    builder.setSmartDashboardType("PowerDistribution");
    int numChannels = getNumChannels();
    for (int i = 0; i < numChannels; ++i) {
      final int chan = i;
      builder.addDoubleProperty(
          "Chan" + i, () -> LoggedPowerDistribution.getInstance().getInputs().pdpChannelCurrents[chan], null);
    }
    builder.addDoubleProperty(
        "Voltage", () -> LoggedPowerDistribution.getInstance().getInputs().pdpVoltage, null);
    builder.addDoubleProperty(
        "TotalCurrent", () -> LoggedPowerDistribution.getInstance().getInputs().pdpTotalCurrent, null);
    builder.addBooleanProperty(
        "SwitchableChannel",
        () -> getSwitchableChannel(),
        value -> setSwitchableChannel(value));
  }
}