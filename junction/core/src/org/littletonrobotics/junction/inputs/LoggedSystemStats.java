package org.littletonrobotics.junction.inputs;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotController;
import org.littletonrobotics.junction.LogTable;

/**
 * Manages logging general system data. This is NOT replayed to the simulator.
 */
public class LoggedSystemStats implements LoggableInputs {

  private static LoggedSystemStats instance;
  private static final PowerDistribution pdp = new PowerDistribution();

  private LoggedSystemStats() {
  }

  public static LoggedSystemStats getInstance() {
    if (instance == null) {
      instance = new LoggedSystemStats();
    }
    return instance;
  }

  public void toLog(LogTable table) {
    table.put("BatteryVoltage", RobotController.getBatteryVoltage());
    table.put("BrownedOut", RobotController.isBrownedOut());
    table.put("CANBusUtilization", RobotController.getCANStatus().percentBusUtilization);
    int channelCount = pdp.getNumChannels();
    double[] pdpCurrents = new double[channelCount];
    for (int channel = 0; channel < channelCount; channel++) {
      pdpCurrents[channel] = pdp.getCurrent(channel);
    }
    table.put("PDPCurrents", pdpCurrents);
  }

  public void fromLog(LogTable table) {
    // Ignore replayed inputs
  }
}
