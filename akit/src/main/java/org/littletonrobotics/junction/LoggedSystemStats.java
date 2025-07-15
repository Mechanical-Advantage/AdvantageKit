// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import edu.wpi.first.networktables.ConnectionInfo;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import org.littletonrobotics.conduit.ConduitApi;

/** Manages logging general system data. */
class LoggedSystemStats {
  private static Set<String> lastNTRemoteIds = new HashSet<>();
  private static ByteBuffer ntIntBuffer = ByteBuffer.allocate(4);

  private LoggedSystemStats() {}

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

    // Log NT client list
    final var ntClientsTable = table.getSubtable("NTClients");
    ConnectionInfo[] ntConnections = NetworkTableInstance.getDefault().getConnections();
    Set<String> ntRemoteIds = new HashSet<>();

    for (int i = 0; i < ntConnections.length; i++) {
      lastNTRemoteIds.remove(ntConnections[i].remote_id);
      ntRemoteIds.add(ntConnections[i].remote_id);
      final var ntClientTable = ntClientsTable.getSubtable(ntConnections[i].remote_id);

      ntClientTable.put("Connected", true);
      ntClientTable.put("IPAddress", ntConnections[i].remote_ip);
      ntClientTable.put("RemotePort", ntConnections[i].remote_port);
      ntIntBuffer.rewind();
      ntClientTable.put(
          "ProtocolVersion", ntIntBuffer.putInt(ntConnections[i].protocol_version).array());
    }

    for (var remoteId : lastNTRemoteIds) {
      ntClientsTable.put(remoteId + "/Connected", false);
    }
    lastNTRemoteIds = ntRemoteIds;
  }
}
