// Copyright 2021-2024 FRC 6328
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
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.hal.can.CANStatus;

/**
 * Manages logging general system data.
 */
public class LoggedSystemStats {
  private static final SystemStatsInputs sysInputs = new SystemStatsInputs();

  private LoggedSystemStats() {
  }

  public static class SystemStatsInputs implements LoggableInputs {
    public int fpgaVersion = 0;
    public int fpgaRevision = 0;
    public String serialNumber = "";
    public String comments = "";
    public int teamNumber = 0;
    public boolean fpgaButton = false;
    public boolean systemActive = false;
    public boolean brownedOut = false;
    public boolean rslState = false;
    public boolean systemTimeValid = false;
    public double voltageVin = 12.0;
    public double currentVin = 0.0;
    public double userVoltage3v3 = 3.3;
    public double userCurrent3v3 = 0.0;
    public boolean userActive3v3 = true;
    public int userCurrentFaults3v3 = 0;
    public double userVoltage5v = 5.0;
    public double userCurrent5v = 0.0;
    public boolean userActive5v = true;
    public int userCurrentFaults5v = 0;
    public double userVoltage6v = 6.0;
    public double userCurrent6v = 0.0;
    public boolean userActive6v = true;
    public int userCurrentFaults6v = 0;
    public double brownoutVoltage = 0.0;
    public double cpuTemp = 0.0;
    public CANStatus canStatus = new CANStatus();
    public long epochTime = 0;

    @Override
    public void toLog(LogTable table) {
      table.put("FPGAVersion", fpgaVersion);
      table.put("FPGARevision", fpgaRevision);
      table.put("SerialNumber", serialNumber);
      table.put("Comments", comments);
      table.put("TeamNumber", teamNumber);
      table.put("FPGAButton", fpgaButton);
      table.put("SystemActive", systemActive);
      table.put("BrownedOut", brownedOut);
      table.put("RSLState", rslState);
      table.put("SystemTimeValid", systemTimeValid);

      table.put("BatteryVoltage", voltageVin);
      table.put("BatteryCurrent", currentVin);

      table.put("3v3Rail/Voltage", userVoltage3v3);
      table.put("3v3Rail/Current", userCurrent3v3);
      table.put("3v3Rail/Active", userActive3v3);
      table.put("3v3Rail/CurrentFaults", userCurrentFaults3v3);

      table.put("5vRail/Voltage", userVoltage5v);
      table.put("5vRail/Current", userCurrent5v);
      table.put("5vRail/Active", userActive5v);
      table.put("5vRail/CurrentFaults", userCurrentFaults5v);

      table.put("6vRail/Voltage", userVoltage6v);
      table.put("6vRail/Current", userCurrent6v);
      table.put("6vRail/Active", userActive6v);
      table.put("6vRail/CurrentFaults", userCurrentFaults6v);

      table.put("BrownoutVoltage", brownoutVoltage);
      table.put("CPUTempCelsius", cpuTemp);

      table.put("CANBus/Utilization", canStatus.percentBusUtilization);
      table.put("CANBus/OffCount", canStatus.busOffCount);
      table.put("CANBus/TxFullCount", canStatus.txFullCount);
      table.put("CANBus/ReceiveErrorCount", canStatus.receiveErrorCount);
      table.put("CANBus/TransmitErrorCount", canStatus.transmitErrorCount);

      table.put("EpochTimeMicros", epochTime);
    }

    @Override
    public void fromLog(LogTable table) {
      fpgaVersion = table.get("FPGAVersion", fpgaVersion);
      fpgaRevision = table.get("FPGARevision", fpgaRevision);
      serialNumber = table.get("SerialNumber", serialNumber);
      comments = table.get("Comments", comments);
      teamNumber = table.get("TeamNumber", teamNumber);
      fpgaButton = table.get("FPGAButton", fpgaButton);
      systemActive = table.get("SystemActive", systemActive);
      brownedOut = table.get("BrownedOut", brownedOut);
      rslState = table.get("RSLState", rslState);
      systemTimeValid = table.get("SystemTimeValid", systemTimeValid);

      voltageVin = table.get("BatteryVoltage", voltageVin);
      currentVin = table.get("BatteryCurrent", currentVin);

      userVoltage3v3 = table.get("3v3Rail/Voltage", userVoltage3v3);
      userCurrent3v3 = table.get("3v3Rail/Current", userCurrent3v3);
      userActive3v3 = table.get("3v3Rail/Active", userActive3v3);
      userCurrentFaults3v3 = table.get("3v3Rail/CurrentFaults", userCurrentFaults3v3);

      userVoltage5v = table.get("5vRail/Voltage", userVoltage5v);
      userCurrent5v = table.get("5vRail/Current", userCurrent5v);
      userActive5v = table.get("5vRail/Active", userActive5v);
      userCurrentFaults5v = table.get("5vRail/CurrentFaults", userCurrentFaults5v);

      userVoltage6v = table.get("6vRail/Voltage", userVoltage6v);
      userCurrent6v = table.get("6vRail/Current", userCurrent6v);
      userActive6v = table.get("6vRail/Active", userActive6v);
      userCurrentFaults6v = table.get("6vRail/CurrentFaults", userCurrentFaults6v);

      brownoutVoltage = table.get("BrownoutVoltage", brownoutVoltage);
      cpuTemp = table.get("CPUTempCelsius", cpuTemp);

      canStatus.setStatus(
          table.get("CANBus/Utilization", canStatus.percentBusUtilization),
          table.get("CANBus/OffCount", canStatus.busOffCount),
          table.get("CANBus/TxFullCount", canStatus.txFullCount),
          table.get("CANBus/ReceiveErrorCount", canStatus.receiveErrorCount),
          table.get("CANBus/TransmitErrorCount", canStatus.transmitErrorCount));

      epochTime = table.get("EpochTimeMicros", epochTime);
    }
  }

  public static void periodic() {
    // Update inputs from conduit
    if (!Logger.hasReplaySource()) {
      ConduitApi conduit = ConduitApi.getInstance();

      sysInputs.fpgaVersion = conduit.getFPGAVersion();
      sysInputs.fpgaRevision = conduit.getFPGARevision();
      sysInputs.serialNumber = conduit.getSerialNumber();
      sysInputs.comments = conduit.getComments();
      sysInputs.teamNumber = conduit.getTeamNumber();
      sysInputs.fpgaButton = conduit.getFPGAButton();
      sysInputs.systemActive = conduit.getSystemActive();
      sysInputs.brownedOut = conduit.getBrownedOut();
      sysInputs.rslState = conduit.getRSLState();
      sysInputs.systemTimeValid = conduit.getSystemTimeValid();

      sysInputs.voltageVin = conduit.getVoltageVin();
      sysInputs.currentVin = conduit.getCurrentVin();

      sysInputs.userVoltage3v3 = conduit.getUserVoltage3v3();
      sysInputs.userCurrent3v3 = conduit.getUserCurrent3v3();
      sysInputs.userActive3v3 = conduit.getUserActive3v3();
      sysInputs.userCurrentFaults3v3 = conduit.getUserCurrentFaults3v3();

      sysInputs.userVoltage5v = conduit.getUserVoltage5v();
      sysInputs.userCurrent5v = conduit.getUserCurrent5v();
      sysInputs.userActive5v = conduit.getUserActive5v();
      sysInputs.userCurrentFaults5v = conduit.getUserCurrentFaults5v();

      sysInputs.userVoltage6v = conduit.getUserVoltage6v();
      sysInputs.userCurrent6v = conduit.getUserCurrent6v();
      sysInputs.userActive6v = conduit.getUserActive6v();
      sysInputs.userCurrentFaults6v = conduit.getUserCurrentFaults6v();

      sysInputs.brownoutVoltage = conduit.getBrownoutVoltage();
      sysInputs.cpuTemp = conduit.getCPUTemp();

      sysInputs.canStatus.setStatus(
          conduit.getCANBusUtilization(),
          (int) conduit.getBusOffCount(),
          (int) conduit.getTxFullCount(),
          (int) conduit.getReceiveErrorCount(),
          (int) conduit.getTransmitErrorCount());

      sysInputs.epochTime = conduit.getEpochTime();
    }

    Logger.processInputs("SystemStats", sysInputs);
  }

  public static SystemStatsInputs getInputs() {
    return sysInputs;
  }
}