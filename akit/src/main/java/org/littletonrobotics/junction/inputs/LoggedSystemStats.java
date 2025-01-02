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

/**
 * Manages logging general system data.
 */
public class LoggedSystemStats {
  private LoggedSystemStats() {
  }

  public static void saveToLog(LogTable table) {
    // Update inputs from conduit
    ConduitApi conduit = ConduitApi.getInstance();

    table.put("FPGAVersion", conduit.getFPGAVersion());
    table.put("FPGARevision", conduit.getFPGARevision());
    table.put("SerialNumber", conduit.getSerialNumber());
    table.put("Comments", conduit.getComments());
    table.put("TeamNumber", conduit.getTeamNumber());
    table.put("FPGAButton", conduit.getFPGAButton());
    table.put("SystemActive", conduit.getSystemActive());
    table.put("BrownedOut", conduit.getBrownedOut());
    table.put("CommsDisableCount", conduit.getCommsDisableCount());
    table.put("RSLState", conduit.getRSLState());
    table.put("SystemTimeValid", conduit.getSystemTimeValid());

    table.put("BatteryVoltage", conduit.getVoltageVin());
    table.put("BatteryCurrent", conduit.getCurrentVin());

    table.put("3v3Rail/Voltage", conduit.getUserVoltage3v3());
    table.put("3v3Rail/Current", conduit.getUserCurrent3v3());
    table.put("3v3Rail/Active", conduit.getUserActive3v3());
    table.put("3v3Rail/CurrentFaults", conduit.getUserCurrentFaults3v3());

    table.put("5vRail/Voltage", conduit.getUserVoltage5v());
    table.put("5vRail/Current", conduit.getUserCurrent5v());
    table.put("5vRail/Active", conduit.getUserActive5v());
    table.put("5vRail/CurrentFaults", conduit.getUserCurrentFaults5v());

    table.put("6vRail/Voltage", conduit.getUserVoltage6v());
    table.put("6vRail/Current", conduit.getUserCurrent6v());
    table.put("6vRail/Active", conduit.getUserActive6v());
    table.put("6vRail/CurrentFaults", conduit.getUserCurrentFaults6v());

    table.put("BrownoutVoltage", conduit.getBrownoutVoltage());
    table.put("CPUTempCelsius", conduit.getCPUTemp());

    table.put("CANBus/Utilization", conduit.getCANBusUtilization());
    table.put("CANBus/OffCount", conduit.getBusOffCount());
    table.put("CANBus/TxFullCount", conduit.getTxFullCount());
    table.put("CANBus/ReceiveErrorCount", conduit.getReceiveErrorCount());
    table.put("CANBus/TransmitErrorCount", conduit.getTransmitErrorCount());

    table.put("EpochTimeMicros", conduit.getEpochTime());
  }
}