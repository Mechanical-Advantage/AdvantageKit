package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.hal.PowerDistributionJNI;


/**
 * Manages logging power distribution data. This is NOT replayed to the simulator.
 */
public class LoggedPowerDistribution {

  private static LoggedPowerDistribution instance;
  private static final Logger logger = Logger.getInstance();

  private final PowerDistributionInputs pdpInputs = new PowerDistributionInputs();

  private int moduleID;
  private int moduleType;

  private LoggedPowerDistribution(int moduleID, PowerDistribution.ModuleType moduleType) {
    this.moduleID = moduleID;
    this.moduleType = moduleType.value;
    ConduitApi.getInstance().configurePowerDistribution(moduleID, this.moduleType);
  }

  private LoggedPowerDistribution() {
    moduleID = PowerDistributionJNI.DEFAULT_MODULE;
    moduleType = PowerDistributionJNI.AUTOMATIC_TYPE;
    ConduitApi.getInstance().configurePowerDistribution(moduleID, this.moduleType);
  }

  public static LoggedPowerDistribution getInstance() {
    if (instance == null) {
      instance = new LoggedPowerDistribution();
    }
    return instance;
  }

  public static LoggedPowerDistribution getInstance(int moduleID, PowerDistribution.ModuleType moduleType)
  {
    if (instance == null) {
      instance = new LoggedPowerDistribution(moduleID, moduleType);
    } else if (instance.moduleID != moduleID || instance.moduleType != moduleType.value) {
      instance = new LoggedPowerDistribution(moduleID, moduleType);
    }

    return instance;
  }

  public static class PowerDistributionInputs implements LoggableInputs {
    public double pdpTemperature;
    public double pdpVoltage;
    public double[] pdpChannelCurrents = new double[24];
    public double pdpTotalCurrent;
    public double pdpTotalPower;
    public double pdpTotalEnergy;
    public int channelCount;
    public int handle;
    public int type;
    public int module_id;
    public long faults;
    public long sticky_faults;

    @Override
    public void toLog(LogTable table) {
      table.put("Temperature", pdpTemperature);
      table.put("Voltage", pdpVoltage);
      table.put("ChannelCurrent", pdpChannelCurrents);
      table.put("TotalCurrent", pdpTotalCurrent);
      table.put("TotalPower", pdpTotalPower);
      table.put("TotalEnergy", pdpTotalEnergy);
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
      for (int i = 0; i < 24; i++) {
        pdpInputs.pdpChannelCurrents[i] = conduit.getPDPChannelCurrent(i);
      }
      pdpInputs.pdpTotalCurrent = conduit.getPDPTotalCurrent();
      pdpInputs.pdpTotalPower = conduit.getPDPTotalPower();
      pdpInputs.pdpTotalEnergy = conduit.getPDPTotalEnergy();
      pdpInputs.channelCount = conduit.getPDPChannelCount();
      pdpInputs.handle = conduit.getPDPHandle();
      pdpInputs.type = conduit.getPDPType();
      pdpInputs.module_id = conduit.getPDPModuleId();
      pdpInputs.faults = conduit.getPDPFaults();
      pdpInputs.sticky_faults = conduit.getPDPStickyFaults();
    }

    logger.processInputs("PowerDistribution", pdpInputs);
  }

  public PowerDistributionInputs getInputs() {
    return pdpInputs;
  }

}