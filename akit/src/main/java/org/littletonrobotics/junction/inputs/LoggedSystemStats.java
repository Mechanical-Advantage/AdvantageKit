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

    table.put("BatteryVoltage", conduit.getBatteryVoltage());
    table.put("WatchdogActive", conduit.getWatchdogActive());
    table.put("CANBandwidth", conduit.getCANBandwidth());
    table.put("IOFrequency", conduit.getIOFrequency());
    table.put("TeamNumber", conduit.getTeamNumber());
    table.put("EpochTimeMicros", conduit.getEpochTime());

    table.put("CPU/Percent", conduit.getCPUPercent());
    table.put("CPU/TempCelsius", conduit.getCPUTempCelcius());

    table.put("Memory/UsageMB", conduit.getMemoryUsageBytes() * 1.0e-6);
    table.put("Memory/TotalMB", conduit.getMemoryTotalBytes() * 1.0e-6);
    table.put("Memory/Percent", conduit.getMemoryPercent());

    table.put("Storage/UsageMB", conduit.getStorageUsageBytes() * 1.0e-6);
    table.put("Storage/TotalMB", conduit.getStorageTotalBytes() * 1.0e-6);
    table.put("Storage/Percent", conduit.getStoragePercent());

    final var accelRaw = conduit.getIMUAccelRaw();
    table.put("IMU/AccelRaw/X", accelRaw.x());
    table.put("IMU/AccelRaw/Y", accelRaw.y());
    table.put("IMU/AccelRaw/Z", accelRaw.z());

    final var gyroRates = conduit.getIMUGyroRates();
    table.put("IMU/GyroRates/X", gyroRates.x());
    table.put("IMU/GyroRates/Y", gyroRates.y());
    table.put("IMU/GyroRates/Z", gyroRates.z());

    final var gyroEuler = conduit.getIMUGyroEuler();
    table.put("IMU/GyroEuler/X", gyroEuler.x());
    table.put("IMU/GyroEuler/Y", gyroEuler.y());
    table.put("IMU/GyroEuler/Z", gyroEuler.z());

    table.put("IMU/Gyro3d", conduit.getIMUGyroRotation3d());
    table.put("IMU/GyroYawFlat", conduit.getIMUGyroYawFlat());
    table.put("IMU/GyroYawLandscape", conduit.getIMUGyroYawLandscape());
    table.put("IMU/GyroYawPortrait", conduit.getIMUGyroYawPortrait());
  }
}
