package org.littletonrobotics.junction.inputs;

import edu.wpi.first.util.WPIUtilJNI;
import edu.wpi.first.wpilibj.RobotController;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

/**
 * Manages logging power distribution data. This is NOT replayed to the simulator.
 */
public class LoggedPowerDistribution {

  private static LoggedPowerDistribution instance;
  private static final Logger logger = Logger.getInstance();

  private final PowerDistributionInputs pdpInputs = new PowerDistributionInputs();

  private LoggedPowerDistribution() {
  }

  public static LoggedPowerDistribution getInstance() {
    if (instance == null) {
      instance = new LoggedPowerDistribution();
    }
    return instance;
  }

  public static class PowerDistributionInputs implements LoggableInputs {
    public double pdpTemperature;
    public double pdpVoltage;
    public double[] pdpChannelCurrents;
    public double pdpTotalCurrent;
    public double pdpTotalPower;
    public double pdpTotalEnergy;

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

  public void periodic() {
    // Update inputs from conduit
    if (!logger.hasReplaySource()) {
      ConduitApi conduit = ConduitApi.getInstance();
      pdpInputs.pdpTemperature = conduit.getPDPTemperature();
      pdpInputs.pdpVoltage = conduit.getPDPVoltage();
      pdpInputs.pdpChannelCurrents = conduit.getPDPCurrent();
      pdpInputs.pdpTotalCurrent = conduit.getPDPTotalCurrent();
      pdpInputs.pdpTotalPower = conduit.getPDPTotalPower();
      pdpInputs.pdpTotalEnergy = conduit.getPDPTotalEnergy();
    }

    logger.processInputs("PowerDistribution", pdpInputs);
  }

}
