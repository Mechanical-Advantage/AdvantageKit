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

package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.hal.PowerDistributionJNI;

/**
 * Manages logging power distribution data.
 */
public class LoggedPowerDistribution {

  private static LoggedPowerDistribution instance;

  private int moduleID;
  private int moduleType;

  private LoggedPowerDistribution(int moduleID, PowerDistribution.ModuleType moduleType) {
    this.moduleID = moduleID;
    this.moduleType = moduleType.value;
    ConduitApi.getInstance().configurePowerDistribution(moduleID, this.moduleType);
  }

  private LoggedPowerDistribution() {
    moduleID = PowerDistributionJNI.DEFAULT_MODULE;
    moduleType = PowerDistributionJNI.AUTOMATIC_TYPE;
    ConduitApi.getInstance().configurePowerDistribution(moduleID, this.moduleType);
  }

  public static LoggedPowerDistribution getInstance() {
    if (instance == null) {
      instance = new LoggedPowerDistribution();
    }
    return instance;
  }

  public static LoggedPowerDistribution getInstance(int moduleID, PowerDistribution.ModuleType moduleType) {
    if (instance == null) {
      instance = new LoggedPowerDistribution(moduleID, moduleType);
    } else if (instance.moduleID != moduleID || instance.moduleType != moduleType.value) {
      instance = new LoggedPowerDistribution(moduleID, moduleType);
    }

    return instance;
  }

  public void saveToLog(LogTable table) {
    ConduitApi conduit = ConduitApi.getInstance();
    table.put("Temperature", conduit.getPDPTemperature());
    table.put("Voltage", conduit.getPDPVoltage());
    double[] currents = new double[24];
    for (int i = 0; i < 24; i++) {
      currents[i] = conduit.getPDPChannelCurrent(i);
    }
    table.put("ChannelCurrent", currents);
    table.put("TotalCurrent", conduit.getPDPTotalCurrent());
    table.put("TotalPower", conduit.getPDPTotalPower());
    table.put("TotalEnergy", conduit.getPDPTotalEnergy());

    table.put("ChannelCount", conduit.getPDPChannelCount());
    table.put("Faults", conduit.getPDPFaults());
    table.put("StickyFaults", conduit.getPDPStickyFaults());
  }
}
