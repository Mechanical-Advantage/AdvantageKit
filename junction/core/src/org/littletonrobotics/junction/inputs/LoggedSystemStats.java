package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.hal.can.CANStatus;

/**
 * Manages logging general system data. This is NOT replayed to the simulator.
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
    public int userCurrentFaults3v3;
    public double userVoltage5v;
    public double userCurrent5v;
    public boolean userActive5v;
    public int userCurrentFaults5v;
    public double userVoltage6v;
    public double userCurrent6v;
    public boolean userActive6v;
    public int userCurrentFaults6v;
    public boolean brownedOut;
    public boolean systemActive;
    public CANStatus canStatus = new CANStatus();
    public long epochTime;

    @Override
    public void toLog(LogTable table) {
      table.put("BatteryVoltage", voltageVin);
      table.put("BatteryCurrent", currentVin);

      table.put("3vRailVoltage", userVoltage3v3);
      table.put("3vRailCurrent", userCurrent3v3);
      table.put("3vRailActive", userActive3v3);
      table.put("3vRailCurrentFaults", userCurrentFaults3v3);

      table.put("5vRailVoltage", userVoltage5v);
      table.put("5vRailCurrent", userCurrent5v);
      table.put("5vRailActive", userActive5v);
      table.put("5vRailCurrentFaults", userCurrentFaults5v);

      table.put("6vRailVoltage", userVoltage6v);
      table.put("6vRailCurrent", userCurrent6v);
      table.put("6vRailActive", userActive6v);
      table.put("6vRailCurrentFaults", userCurrentFaults6v);

      table.put("BrownedOut", brownedOut);
      table.put("SystemActive", systemActive);
      
      table.put("CANBusUtilization", canStatus.percentBusUtilization);
      table.put("CANBusOffCount", canStatus.busOffCount);
      table.put("CANBusTxFullCount", canStatus.txFullCount);
      table.put("CANBusReceiveErrorCount", canStatus.receiveErrorCount);
      table.put("CANBusTransmitErrorCount", canStatus.transmitErrorCount);
      table.put("EpochTime", epochTime);      
    }

    @Override
    public void fromLog(LogTable table) {
      // ignore replayed inputs
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
        (int) conduit.getCANBusUtilization(),
        (int) conduit.getBusOffCount(),
        (int) conduit.getTxFullCount(),
        (int) conduit.getReceiveErrorCount(),
        (int) conduit.getTransmitErrorCount()
      );
      sysInputs.epochTime = conduit.getEpochTime();
    }

    logger.processInputs("SystemStats", sysInputs);
  }

  public SystemStatsInputs getInputs() {
    return sysInputs;
  }

}
