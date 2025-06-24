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

    table.put("Memory/UsageBytes", conduit.getMemoryUsageBytes());
    table.put("Memory/TotalBytes", conduit.getMemoryTotalBytes());
    table.put("Memory/Percent", conduit.getMemoryPercent());

    table.put("Storage/UsageBytes", conduit.getStorageUsageBytes());
    table.put("Storage/TotalBytes", conduit.getStorageTotalBytes());
    table.put("Storage/Percent", conduit.getStoragePercent());

    table.put("IMU/RawAccel", conduit.getIMURawAccel());
    table.put("IMU/RawGyro", conduit.getIMURawGyro());
    table.put("IMU/Gyro3d", conduit.getIMURotation3d());
    table.put("IMU/YawFlat", conduit.getIMUYawFlat());
    table.put("IMU/YawLandscape", conduit.getIMUYawFlat());
    table.put("IMU/YawPortrait", conduit.getIMUYawFlat());
  }
}
