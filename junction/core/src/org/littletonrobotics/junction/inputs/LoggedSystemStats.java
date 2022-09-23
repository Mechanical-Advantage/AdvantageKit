package org.littletonrobotics.junction.inputs;

import edu.wpi.first.util.WPIUtilJNI;
import edu.wpi.first.wpilibj.RobotController;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

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
    public float canBusUtilization;
    public long epochTime;

    @Override
    public void toLog(LogTable table) {
      table.put("BatteryVoltage", batteryVoltage);
      table.put("BrownedOut", brownedOut);
      table.put("CANBusUtilization", canBusUtilization);
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
      sysInputs.canBusUtilization = conduit.getCANBusUtilization();
      sysInputs.epochTime = conduit.getEpochTime();
    }

    logger.processInputs("SystemStats", sysInputs);
  }

}
