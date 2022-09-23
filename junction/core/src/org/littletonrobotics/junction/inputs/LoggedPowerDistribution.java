package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTable;

/**
 * Manages logging power distribution data (current, power, etc.)
 */
public class LoggedPowerDistribution implements LoggableInputs {
  private double pdpTemperature;
  private double pdpVoltage;
  private double[] pdpChannelCurrents;
  private double pdpTotalCurrent;
  private double pdpTotalPower;
  private double pdpTotalEnergy;

  private static LoggedPowerDistribution instance;
  private static final Logger logger = Logger.getInstance();

  private LoggedPowerDistribution() {
  }

  public static LoggedPowerDistribution getInstance() {
    if (instance == null) {
      instance = new LoggedPowerDistribution();
    }
    return instance;
  }

  /**
   * Records inputs from the real driver station via conduit
   */
  public void periodic() {
    // Update inputs from conduit
    if (!logger.hasReplaySource()) {
      ConduitApi conduit = ConduitApi.getInstance();

      pdpTemperature = conduit.getPDPTemperature();
      pdpVoltage = conduit.getPDPVoltage();
      pdpChannelCurrents = conduit.getPDPCurrent();
      pdpTotalCurrent = conduit.getPDPTotalCurrent();
      pdpTotalPower = conduit.getPDPTotalPower();
      pdpTotalEnergy = conduit.getPDPTotalEnergy();
    }
  }

  @Override
  public void toLog(LogTable table) {
    table.put("PowerDistributionTemperature", pdpTemperature);
    table.put("PowerDistributionVoltage", pdpVoltage);
    table.put("PowerDistributionChannelCurrent", pdpChannelCurrents);
    table.put("PowerDistributionTotalCurrent", pdpTotalCurrent);
    table.put("PowerDistributionTotalPower", pdpTotalPower);
    table.put("PowerDistributionTotalEnergy", pdpTotalEnergy);
  }

  @Override
  public void fromLog(LogTable table) {
    // Ignore replayed inputs
  }
}
