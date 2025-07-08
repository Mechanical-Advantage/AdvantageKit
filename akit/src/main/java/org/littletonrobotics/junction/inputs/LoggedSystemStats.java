// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.conduit.schema.NetworkDirStatus;
import org.littletonrobotics.conduit.schema.NetworkStatus;
import org.littletonrobotics.conduit.schema.Vector3;
import org.littletonrobotics.junction.LogTable;

/** Manages logging general system data. */
public class LoggedSystemStats {
  private LoggedSystemStats() {}

  public static void saveToLog(LogTable table) {
    // Update inputs from conduit
    ConduitApi conduit = ConduitApi.getInstance();

    table.put("BatteryVoltage", conduit.getBatteryVoltage());
    table.put("WatchdogActive", conduit.getWatchdogActive());
    table.put("IOFrequency", conduit.getIOFrequency());
    table.put("TeamNumber", conduit.getTeamNumber());
    table.put("EpochTimeMicros", conduit.getEpochTime());
    table.put("EpochTimeValid", conduit.getEpochTimeValid());

    logNetworkStatus(table.getSubtable("Network/Ethernet"), conduit.getNetworkEthernet());
    logNetworkStatus(table.getSubtable("Network/WiFi"), conduit.getNetworkWiFi());
    logNetworkStatus(table.getSubtable("Network/USBTether"), conduit.getNetworkUSBTether());
    for (int bus = 0; bus < ConduitApi.NUM_CAN_BUSES; bus++) {
      logNetworkStatus(table.getSubtable("Network/CAN" + bus), conduit.getNetworkCAN(bus));
    }

    table.put("CPU/Percent", conduit.getCPUPercent());
    table.put("CPU/TempCelsius", conduit.getCPUTempCelcius());

    table.put("Memory/UsageMB", conduit.getMemoryUsageBytes() * 1.0e-6);
    table.put("Memory/TotalMB", conduit.getMemoryTotalBytes() * 1.0e-6);
    table.put("Memory/Percent", conduit.getMemoryPercent());

    table.put("Storage/UsageMB", conduit.getStorageUsageBytes() * 1.0e-6);
    table.put("Storage/TotalMB", conduit.getStorageTotalBytes() * 1.0e-6);
    table.put("Storage/Percent", conduit.getStoragePercent());

    logVector3(table.getSubtable("IMU/AccelRaw"), conduit.getIMUAccelRaw());
    logVector3(table.getSubtable("IMU/GyroRates"), conduit.getIMUGyroRates());
    logVector3(table.getSubtable("IMU/GyroEuler/Flat"), conduit.getIMUGyroEulerFlat());
    logVector3(table.getSubtable("IMU/GyroEuler/Landscape"), conduit.getIMUGyroEulerLandscape());
    logVector3(table.getSubtable("IMU/GyroEuler/Portrait"), conduit.getIMUGyroEulerPortrait());
    table.put("IMU/Gyro3d", conduit.getIMUGyroRotation3d());
    table.put("IMU/GyroYaw/Flat", conduit.getIMUGyroYawFlat());
    table.put("IMU/GyroYaw/Landscape", conduit.getIMUGyroYawLandscape());
    table.put("IMU/GyroYaw/Portrait", conduit.getIMUGyroYawPortrait());
  }

  private static void logNetworkStatus(LogTable table, NetworkStatus status) {
    logNetworkDirectionStatus(table.getSubtable("RX"), status.rx());
    logNetworkDirectionStatus(table.getSubtable("TX"), status.tx());
  }

  private static void logNetworkDirectionStatus(LogTable table, NetworkDirStatus status) {
    table.put("BandwidthMbps", status.bandwidthKbps() * 1.0e-3);
    table.put("Bytes", status.bytes());
    table.put("Dropped", status.dropped());
    table.put("Errors", status.errors());
    table.put("Packets", status.packets());
  }

  private static void logVector3(LogTable table, Vector3 vector) {
    table.put("X", vector.x());
    table.put("Y", vector.y());
    table.put("Z", vector.z());
  }
}
