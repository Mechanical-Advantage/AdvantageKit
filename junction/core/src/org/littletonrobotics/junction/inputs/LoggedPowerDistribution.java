package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.hal.PowerDistributionJNI;

/**
 * Manages logging power distribution data.
 */
public class LoggedPowerDistribution {

  private static LoggedPowerDistribution instance;

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

  public static LoggedPowerDistribution getInstance(int moduleID, PowerDistribution.ModuleType moduleType) {
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
    public int moduleId;
    public long faults;
    public long stickyFaults;

    @Override
    public void toLog(LogTable table) {
      table.put("Temperature", pdpTemperature);
      table.put("Voltage", pdpVoltage);
      table.put("ChannelCurrent", pdpChannelCurrents);
      table.put("TotalCurrent", pdpTotalCurrent);
      table.put("TotalPower", pdpTotalPower);
      table.put("TotalEnergy", pdpTotalEnergy);

      table.put("ChannelCount", channelCount);
      table.put("Faults", (int) faults);
      table.put("StickyFaults", (int) stickyFaults);
    }

    @Override
    public void fromLog(LogTable table) {
      pdpTemperature = table.get("Temperature", pdpTemperature);
      pdpVoltage = table.get("Voltage", pdpVoltage);
      pdpChannelCurrents = table.get("ChannelCurrent", pdpChannelCurrents);
      pdpTotalCurrent = table.get("TotalCurrent", pdpTotalCurrent);
      pdpTotalPower = table.get("TotalPower", pdpTotalPower);
      pdpTotalEnergy = table.get("TotalEnergy", pdpTotalEnergy);

      channelCount = (int) table.get("ChannelCount", channelCount);
      faults = table.get("Faults", faults);
      stickyFaults = table.get("StickyFaults", stickyFaults);
    }
  }

  public void periodic() {
    // Update inputs from conduit
    if (!Logger.hasReplaySource()) {
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
      pdpInputs.moduleId = conduit.getPDPModuleId();
      pdpInputs.faults = conduit.getPDPFaults();
      pdpInputs.stickyFaults = conduit.getPDPStickyFaults();
    }

    Logger.processInputs("PowerDistribution", pdpInputs);
  }

  public PowerDistributionInputs getInputs() {
    return pdpInputs;
  }

}
