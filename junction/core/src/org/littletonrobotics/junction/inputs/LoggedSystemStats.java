package org.littletonrobotics.junction.inputs;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.RobotController;
import org.littletonrobotics.junction.LogTable;

/**
 * Manages logging general system data. This is NOT replayed to the simulator.
 */
public class LoggedSystemStats implements LoggableInputs {

  private static LoggedSystemStats instance;

  private Integer powerDistributionModule;
  private ModuleType powerDistributionType;
  private PowerDistribution powerDistribution;

  private LoggedSystemStats() {
  }

  public static LoggedSystemStats getInstance() {
    if (instance == null) {
      instance = new LoggedSystemStats();
    }
    return instance;
  }

  public void setPowerDistributionConfig(int module, ModuleType moduleType) {
    powerDistributionModule = module;
    powerDistributionType = moduleType;
  }

  public void toLog(LogTable table) {
    if (powerDistribution == null) {
      if (powerDistributionModule == null) {
        powerDistribution = new PowerDistribution();
      } else {
        powerDistribution = new PowerDistribution(powerDistributionModule, powerDistributionType);
      }
    }

    table.put("BatteryVoltage", RobotController.getBatteryVoltage());
    table.put("BrownedOut", RobotController.isBrownedOut());
    table.put("CANBusUtilization", RobotController.getCANStatus().percentBusUtilization);
    int channelCount = powerDistribution.getNumChannels();
    double[] powerDistributionCurrents = new double[channelCount];
    for (int channel = 0; channel < channelCount; channel++) {
      powerDistributionCurrents[channel] = powerDistribution.getCurrent(channel);
    }
    table.put("PowerDistributionCurrents", powerDistributionCurrents);
  }

  public void fromLog(LogTable table) {
    // Ignore replayed inputs
  }
}
