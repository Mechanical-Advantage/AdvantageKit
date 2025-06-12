// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.inputs;

import edu.wpi.first.wpilibj.PowerDistribution;
import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;

/** Manages logging power distribution data. */
public class LoggedPowerDistribution {

  private static LoggedPowerDistribution instance;

  private int busID;
  private int moduleID;
  private int moduleType;

  private LoggedPowerDistribution(
      int busID, int moduleID, PowerDistribution.ModuleType moduleType) {
    this.busID = busID;
    this.moduleID = moduleID;
    this.moduleType = moduleType.value;
    ConduitApi.getInstance().configurePowerDistribution(busID, moduleID, this.moduleType);
  }

  public static LoggedPowerDistribution getInstance() {
    return instance;
  }

  public static LoggedPowerDistribution getInstance(
      int busID, int moduleID, PowerDistribution.ModuleType moduleType) {
    if (instance == null) {
      instance = new LoggedPowerDistribution(busID, moduleID, moduleType);
    } else if (instance.busID != busID
        || instance.moduleID != moduleID
        || instance.moduleType != moduleType.value) {
      instance = new LoggedPowerDistribution(busID, moduleID, moduleType);
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
