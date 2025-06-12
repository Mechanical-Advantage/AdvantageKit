// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;

/** Manages logging general system data. */
public class LoggedSystemStats {
  private LoggedSystemStats() {}

  public static void saveToLog(LogTable table) {
    // Update inputs from conduit
    ConduitApi conduit = ConduitApi.getInstance();

    table.put("FPGAVersion", conduit.getFPGAVersion());
    table.put("FPGARevision", conduit.getFPGARevision());
    table.put("SerialNumber", conduit.getSerialNumber());
    table.put("Comments", conduit.getComments());
    table.put("TeamNumber", conduit.getTeamNumber());
    table.put("SystemActive", conduit.getSystemActive());
    table.put("BrownedOut", conduit.getBrownedOut());
    table.put("CommsDisableCount", conduit.getCommsDisableCount());
    table.put("RSLState", conduit.getRSLState());
    table.put("SystemTimeValid", conduit.getSystemTimeValid());

    table.put("BatteryVoltage", conduit.getVoltageVin());

    table.put("3v3Rail/Voltage", conduit.getUserVoltage3v3());
    table.put("3v3Rail/Current", conduit.getUserCurrent3v3());
    table.put("3v3Rail/Active", conduit.getUserActive3v3());
    table.put("3v3Rail/CurrentFaults", conduit.getUserCurrentFaults3v3());

    table.put("BrownoutVoltage", conduit.getBrownoutVoltage());
    table.put("CPUTempCelsius", conduit.getCPUTemp());
    table.put("EpochTimeMicros", conduit.getEpochTime());

    for (int busId = 0; busId < ConduitApi.NUM_CAN_BUSES; busId++) {
      table.put("CANBus/" + busId + "/Utilization", conduit.getCANBusUtilization(busId));
      table.put("CANBus/" + busId + "/OffCount", conduit.getBusOffCount(busId));
      table.put("CANBus/" + busId + "/TxFullCount", conduit.getTxFullCount(busId));
      table.put("CANBus/" + busId + "/ReceiveErrorCount", conduit.getReceiveErrorCount(busId));
      table.put("CANBus/" + busId + "/TransmitErrorCount", conduit.getTransmitErrorCount(busId));
    }
  }
}
