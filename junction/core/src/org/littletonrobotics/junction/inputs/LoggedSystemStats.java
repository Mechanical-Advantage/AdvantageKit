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
    public double batteryVoltage;
    public boolean brownedOut;
    public CANStatus canStatus = new CANStatus();
    public long epochTime;

    @Override
    public void toLog(LogTable table) {
      table.put("BatteryVoltage", batteryVoltage);
      table.put("BrownedOut", brownedOut);
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

      sysInputs.batteryVoltage = conduit.getVoltage();
      sysInputs.brownedOut = conduit.getBrownedOut();
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
