// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import edu.wpi.first.hal.PowerDistributionJNI;
import edu.wpi.first.wpilibj.PowerDistribution;
import org.littletonrobotics.conduit.ConduitApi;

/** Manages logging power distribution data. */
class LoggedPowerDistribution {
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

  public static LoggedPowerDistribution getInstance(
      int moduleID, PowerDistribution.ModuleType moduleType) {
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
