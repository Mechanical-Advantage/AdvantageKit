// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.conduit.schema.CANInfo;
import org.littletonrobotics.conduit.schema.NetworkDirStatus;
import org.littletonrobotics.conduit.schema.NetworkStatus;
import org.littletonrobotics.conduit.schema.Vector3;
import org.wpilib.networktables.ConnectionInfo;
import org.wpilib.networktables.NetworkTableInstance;

/** Manages logging general system data. */
class LoggedSystemStats {
  private static Set<String> lastNTRemoteIds = new HashSet<>();
  private static ByteBuffer ntIntBuffer = ByteBuffer.allocate(4);

  private LoggedSystemStats() {}

  public static void saveToLog(LogTable table) {
    // Update inputs from conduit
    ConduitApi conduit = ConduitApi.getInstance();

    table.put("BatteryVoltage", conduit.getBatteryVoltage(), "volts");
    table.put("WatchdogActive", conduit.getWatchdogActive());
    table.put("IOFrequency", conduit.getIOFrequency());
    table.put("IORXFrequency", conduit.getIORXFrequency());
    table.put("TeamNumber", conduit.getTeamNumber());
    table.put("EpochTime", (double) (conduit.getEpochTime() / 1000), "microseconds");
    table.put("EpochTimeValid", conduit.getEpochTimeValid());

    table.put("Faults/Brownout", conduit.getFaultBrownout());
    table.put("Faults/CANBusDown", conduit.getFaultCanbusDown());
    table.put("Faults/CANBusUnavail", conduit.getFaultCanbusUnavail());
    table.put("Faults/Display", conduit.getFaultDisplay());
    table.put("Faults/IMU", conduit.getFaultIMU());
    table.put("Faults/IO", conduit.getFaultIO());
    table.put("Faults/RSL", conduit.getFaultRSL());
    table.put("Faults/USB", conduit.getFaultUSB());

    table.put("FaultCounts/Brownout", conduit.getFaultCountBrownout());
    table.put("FaultCounts/CANBusDown", conduit.getFaultCountCanbusDown());
    table.put("FaultCounts/CANBusUnavail", conduit.getFaultCountCanbusUnavail());
    table.put("FaultCounts/Display", conduit.getFaultCountDisplay());
    table.put("FaultCounts/IMU", conduit.getFaultCountIMU());
    table.put("FaultCounts/IO", conduit.getFaultCountIO());
    table.put("FaultCounts/RSL", conduit.getFaultCountRSL());
    table.put("FaultCounts/USB", conduit.getFaultCountUSB());

    logNetworkStatus(table.getSubtable("Network/Ethernet"), conduit.getNetworkEthernet());
    logNetworkStatus(table.getSubtable("Network/WiFi"), conduit.getNetworkWiFi());
    logNetworkStatus(table.getSubtable("Network/USBTether"), conduit.getNetworkUSBTether());
    for (int bus = 0; bus < ConduitApi.NUM_CAN_BUSES; bus++) {
      logNetworkStatus(table.getSubtable("Network/CAN" + bus), conduit.getNetworkCAN(bus));
      logCANInfo(table.getSubtable("Network/CAN" + bus), conduit.getNetworkCANInfo(bus));
    }

    table.put("CPU/Utilization", conduit.getCPUPercent(), "percent");
    table.put("CPU/Temperature", conduit.getCPUTempCelcius(), "celcius");

    table.put("Memory/Usage", conduit.getMemoryUsageBytes() * 1.0e-6, "megabytes");
    table.put("Memory/Total", conduit.getMemoryTotalBytes() * 1.0e-6, "megabytes");
    table.put("Memory/Utilization", conduit.getMemoryPercent(), "percent");

    table.put("Storage/Usage", conduit.getStorageUsageBytes() * 1.0e-6, "megabytes");
    table.put("Storage/Total", conduit.getStorageTotalBytes() * 1.0e-6, "megabytes");
    table.put("Storage/Utilization", conduit.getStoragePercent(), "percent");

    table.put("3v3Current", conduit.getCurrent3V3(), "amps");
    table.put("OS/Hash", conduit.getOSHash());
    table.put("OS/Slot", conduit.getOSSlot());
    table.put("OS/Version", conduit.getOSVersion());

    logVector3(table.getSubtable("IMU/AccelRaw"), conduit.getIMUAccelRaw(), "G");
    logVector3(table.getSubtable("IMU/GyroRates"), conduit.getIMUGyroRates(), "degrees per second");
    logVector3(table.getSubtable("IMU/GyroEuler/Flat"), conduit.getIMUGyroEulerFlat(), "degrees");
    logVector3(
        table.getSubtable("IMU/GyroEuler/Landscape"),
        conduit.getIMUGyroEulerLandscape(),
        "degrees");
    logVector3(
        table.getSubtable("IMU/GyroEuler/Portrait"), conduit.getIMUGyroEulerPortrait(), "degrees");
    table.put("IMU/Gyro3d", conduit.getIMUGyroRotation3d());
    table.put("IMU/GyroYaw/Flat", conduit.getIMUGyroYawFlat());
    table.put("IMU/GyroYaw/Landscape", conduit.getIMUGyroYawLandscape());
    table.put("IMU/GyroYaw/Portrait", conduit.getIMUGyroYawPortrait());

    // Log NT client list
    final var ntClientsTable = table.getSubtable("NTClients");
    ConnectionInfo[] ntConnections = NetworkTableInstance.getDefault().getConnections();
    Set<String> ntRemoteIds = new HashSet<>();

    for (int i = 0; i < ntConnections.length; i++) {
      lastNTRemoteIds.remove(ntConnections[i].remoteId);
      ntRemoteIds.add(ntConnections[i].remoteId);
      final var ntClientTable = ntClientsTable.getSubtable(ntConnections[i].remoteId);

      ntClientTable.put("Connected", true);
      ntClientTable.put("IPAddress", ntConnections[i].remoteIp);
      ntClientTable.put("RemotePort", ntConnections[i].remotePort);
      ntIntBuffer.rewind();
      ntClientTable.put(
          "ProtocolVersion", ntIntBuffer.putInt(ntConnections[i].protocolVersion).array());
    }

    for (var remoteId : lastNTRemoteIds) {
      ntClientsTable.put(remoteId + "/Connected", false);
    }
    lastNTRemoteIds = ntRemoteIds;
  }

  private static void logNetworkStatus(LogTable table, NetworkStatus status) {
    logNetworkDirectionStatus(table.getSubtable("RX"), status.rx());
    logNetworkDirectionStatus(table.getSubtable("TX"), status.tx());
  }

  private static void logNetworkDirectionStatus(LogTable table, NetworkDirStatus status) {
    table.put("Bandwidth", status.bandwidthKbps() * 1.0e-3, "megabits per second");
    table.put("Kilobytes", status.bytes() / 1024.0, "kilobytes");
    table.put("Dropped", status.dropped());
    table.put("Errors", status.errors());
    table.put("Packets", status.packets());
  }

  private static void logCANInfo(LogTable table, CANInfo info) {
    table.put("MaxBandwidth", info.maxBandwidthMbps(), "megabits per second");
    table.put("FD", info.isFd());
    table.put("Available", info.isAvailable());
    table.put("InterfaceUp", info.isUp());
    table.put("Utilization", info.utilizationPercent(), "percent");
    table.put("Framerate", info.fps());
  }

  private static void logVector3(LogTable table, Vector3 vector, String unit) {
    table.put("X", vector.x(), unit);
    table.put("Y", vector.y(), unit);
    table.put("Z", vector.z(), unit);
  }
}
