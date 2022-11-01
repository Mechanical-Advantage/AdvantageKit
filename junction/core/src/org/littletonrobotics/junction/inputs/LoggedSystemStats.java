package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.hal.can.CANStatus;

/**
 * Manages logging general system data.
 */
public class LoggedSystemStats {

  private static LoggedSystemStats instance;
  private static final Logger logger = Logger.getInstance();

  private final SystemStatsInputs sysInputs = new SystemStatsInputs();

  private LoggedSystemStats() {
  }

  public static LoggedSystemStats getInstance() {
    if (instance == null) {
      instance = new LoggedSystemStats();
    }
    return instance;
  }

  public static class SystemStatsInputs implements LoggableInputs {
    public double voltageVin;
    public double currentVin;
    public double userVoltage3v3;
    public double userCurrent3v3;
    public boolean userActive3v3;
    public long userCurrentFaults3v3;
    public double userVoltage5v;
    public double userCurrent5v;
    public boolean userActive5v;
    public long userCurrentFaults5v;
    public double userVoltage6v;
    public double userCurrent6v;
    public boolean userActive6v;
    public long userCurrentFaults6v;
    public boolean brownedOut;
    public boolean systemActive;
    public CANStatus canStatus = new CANStatus();
    public long epochTime;

    @Override
    public void toLog(LogTable table) {
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

      table.put("BrownedOut", brownedOut);
      table.put("SystemActive", systemActive);

      table.put("CANBus/Utilization", canStatus.percentBusUtilization);
      table.put("CANBus/OffCount", canStatus.busOffCount);
      table.put("CANBus/TxFullCount", canStatus.txFullCount);
      table.put("CANBus/ReceiveErrorCount", canStatus.receiveErrorCount);
      table.put("CANBus/TransmitErrorCount", canStatus.transmitErrorCount);
      table.put("EpochTimeMicros", epochTime);
    }

    @Override
    public void fromLog(LogTable table) {
      voltageVin = table.getDouble("BatteryVoltage", voltageVin);
      currentVin = table.getDouble("BatteryCurrent", currentVin);

      userVoltage3v3 = table.getDouble("3v3Rail/Voltage", userVoltage3v3);
      userCurrent3v3 = table.getDouble("3v3Rail/Current", userCurrent3v3);
      userActive3v3 = table.getBoolean("3v3Rail/Active", userActive3v3);
      userCurrentFaults3v3 = table.getInteger("3v3Rail/CurrentFaults", userCurrentFaults3v3);

      userVoltage5v = table.getDouble("5vRail/Voltage", userVoltage5v);
      userCurrent5v = table.getDouble("5vRail/Current", userCurrent5v);
      userActive5v = table.getBoolean("5vRail/Active", userActive5v);
      userCurrentFaults5v = table.getInteger("5vRail/CurrentFaults", userCurrentFaults5v);

      userVoltage6v = table.getDouble("6vRail/Voltage", userVoltage6v);
      userCurrent6v = table.getDouble("6vRail/Current", userCurrent6v);
      userActive6v = table.getBoolean("6vRail/Active", userActive6v);
      userCurrentFaults6v = table.getInteger("6vRail/CurrentFaults", userCurrentFaults6v);

      brownedOut = table.getBoolean("BrownedOut", brownedOut);
      systemActive = table.getBoolean("SystemActive", systemActive);

      canStatus.setStatus(
          table.getDouble("CANBus/Utilization", canStatus.percentBusUtilization),
          (int) table.getInteger("CANBus/OffCount", canStatus.busOffCount),
          (int) table.getInteger("CANBus/TxFullCount", canStatus.txFullCount),
          (int) table.getInteger("CANBus/ReceiveErrorCount", canStatus.receiveErrorCount),
          (int) table.getInteger("CANBus/TransmitErrorCount", canStatus.transmitErrorCount));
      epochTime = table.getInteger("EpochTimeMicros", epochTime);
    }
  }

  public void periodic() {
    // Update inputs from conduit
    if (!logger.hasReplaySource()) {
      ConduitApi conduit = ConduitApi.getInstance();

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

      sysInputs.brownedOut = conduit.getBrownedOut();
      sysInputs.systemActive = conduit.getSystemActive();
      sysInputs.canStatus.setStatus(
          conduit.getCANBusUtilization(),
          (int) conduit.getBusOffCount(),
          (int) conduit.getTxFullCount(),
          (int) conduit.getReceiveErrorCount(),
          (int) conduit.getTransmitErrorCount());
      sysInputs.epochTime = conduit.getEpochTime();
    }

    logger.processInputs("SystemStats", sysInputs);
  }

  public SystemStatsInputs getInputs() {
    return sysInputs;
  }
}