// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.littletonrobotics.junction.inputs;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.RobotBase;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

/**
 * Manages logging and replaying data from the driver station (robot state,
 * joysticks, etc.)
 */
public class LoggedDriverStation {

  private static LoggedDriverStation instance;
  private static final Logger logger = Logger.getInstance();
  private static final DriverStation driverStation = DriverStation.getInstance();

  private final DriverStationInputs dsInputs = new DriverStationInputs();
  private final JoystickInputs[] joystickInputs = { new JoystickInputs(), new JoystickInputs(), new JoystickInputs(),
      new JoystickInputs(), new JoystickInputs(), new JoystickInputs() };

  private LoggedDriverStation() {
  }

  public static LoggedDriverStation getInstance() {
    if (instance == null) {
      instance = new LoggedDriverStation();
    }
    return instance;
  }

  /**
   * General driver station data that needs to be updated throughout the match.
   */
  private static class DriverStationInputs implements LoggableInputs {
    public boolean enabled = false;
    public boolean autonomous = false;
    public boolean test = false;
    public boolean emergencyStop = false;
    public boolean fmsAttached = false;
    public boolean dsAttached = false;

    public double matchTime = 0.0;
    public String gameSpecificMessage = "";

    public void toLog(LogTable table) {
      table.put("Enabled", enabled);
      table.put("Autonomous", autonomous);
      table.put("Test", test);
      table.put("EmergencyStop", emergencyStop);
      table.put("FMSAttached", fmsAttached);
      table.put("DSAttached", dsAttached);
      table.put("MatchTime", matchTime);
      table.put("GameSpecificMessage", gameSpecificMessage);
    }

    public void fromLog(LogTable table) {
      enabled = table.getBoolean("Enabled", enabled);
      autonomous = table.getBoolean("Autonomous", autonomous);
      test = table.getBoolean("Test", test);
      emergencyStop = table.getBoolean("EmergencyStop", emergencyStop);
      fmsAttached = table.getBoolean("FMSAttached", fmsAttached);
      dsAttached = table.getBoolean("DSAttached", dsAttached);
      matchTime = table.getDouble("MatchTime", matchTime);
      gameSpecificMessage = table.getString("GameSpecificMessage", gameSpecificMessage);
    }
  }

  /**
   * All of the required inputs for a single joystick.
   */
  private static class JoystickInputs implements LoggableInputs {
    public String name = "";
    public int type = 0;
    public boolean xbox = false;
    public int buttons = 0;
    public double[] axes = {};
    public int[] axisTypes = {};
    public int[] povs = {};

    public void toLog(LogTable table) {
      table.put("Name", name);
      table.put("Type", type);
      table.put("Xbox", xbox);
      table.put("Buttons", buttons);
      table.put("Axes", axes);
      table.put("AxisTypes", axisTypes);
      table.put("POVs", povs);
    }

    public void fromLog(LogTable table) {
      name = table.getString("Name", name);
      type = table.getInteger("Type", type);
      xbox = table.getBoolean("Xbox", xbox);
      buttons = table.getInteger("Buttons", buttons);
      axes = table.getDoubleArray("Axes", axes);
      axisTypes = table.getIntegerArray("AxisTypes", axisTypes);
      povs = table.getIntegerArray("POVs", povs);
    }
  }

  /**
   * Records inputs from the real driver station or replays to the simulator.
   */
  public void periodic() {
    // Update inputs from real driver station
    if (RobotBase.isReal()) {
      dsInputs.enabled = driverStation.isEnabled();
      dsInputs.autonomous = driverStation.isAutonomous();
      dsInputs.test = driverStation.isTest();
      dsInputs.emergencyStop = driverStation.isEStopped();
      dsInputs.fmsAttached = driverStation.isFMSAttached();
      dsInputs.dsAttached = driverStation.isDSAttached();
      dsInputs.matchTime = driverStation.getMatchTime();
      dsInputs.gameSpecificMessage = driverStation.getGameSpecificMessage();

      for (int id = 0; id < joystickInputs.length; id++) {
        JoystickInputs joystick = joystickInputs[id];
        String oldName = joystick.name;
        joystick.name = driverStation.getJoystickName(id);
        joystick.buttons = driverStation.getStickButtons(id);

        joystick.axes = new double[driverStation.getStickAxisCount(id)];
        for (int axis = 0; axis < joystick.axes.length; axis++) {
          joystick.axes[axis] = driverStation.getStickAxis(id, axis);
        }

        joystick.povs = new int[driverStation.getStickPOVCount(id)];
        for (int pov = 0; pov < joystick.povs.length; pov++) {
          joystick.povs[pov] = driverStation.getStickPOV(id, pov);
        }

        // These values are not updated on every cycle and need to interface with the
        // HAL. This is slower, so wait until the joystick changes.
        if (!oldName.equals(joystick.name)) {
          joystick.type = driverStation.getJoystickType(id);
          joystick.xbox = driverStation.getJoystickIsXbox(id);
          joystick.axisTypes = new int[driverStation.getStickAxisCount(id)];
          for (int axis = 0; axis < joystick.axes.length; axis++) {
            joystick.axisTypes[axis] = driverStation.getJoystickAxisType(id, axis);
          }
        }
      }
    }

    // Send/receive log data
    logger.processInputs("DriverStation", dsInputs);
    for (int id = 0; id < joystickInputs.length; id++) {
      logger.processInputs("DriverStation/Joystick" + Integer.toString(id), joystickInputs[id]);
    }

    // Send inputs to sim driver station
    if (RobotBase.isSimulation()) {
      DriverStationSim.setEnabled(dsInputs.enabled);
      DriverStationSim.setAutonomous(dsInputs.autonomous);
      DriverStationSim.setTest(dsInputs.test);
      DriverStationSim.setEStop(dsInputs.emergencyStop);
      DriverStationSim.setFmsAttached(dsInputs.fmsAttached);
      DriverStationSim.setDsAttached(dsInputs.dsAttached);
      DriverStationSim.setMatchTime(dsInputs.matchTime);
      DriverStationSim.setGameSpecificMessage(dsInputs.gameSpecificMessage);

      for (int id = 0; id < joystickInputs.length; id++) {
        JoystickInputs joystick = joystickInputs[id];
        DriverStationSim.setJoystickName(id, joystick.name);
        DriverStationSim.setJoystickType(id, joystick.type);
        DriverStationSim.setJoystickIsXbox(id, joystick.xbox);
        DriverStationSim.setJoystickButtons(id, joystick.buttons);

        DriverStationSim.setJoystickAxisCount(id, joystick.axes.length);
        for (int axis = 0; axis < joystick.axes.length; axis++) {
          DriverStationSim.setJoystickAxis(id, axis, joystick.axes[axis]);
          DriverStationSim.setJoystickAxisType(id, axis, joystick.axisTypes[axis]);
        }

        DriverStationSim.setJoystickPOVCount(id, joystick.povs.length);
        for (int pov = 0; pov < joystick.povs.length; pov++) {
          DriverStationSim.setJoystickPOV(id, pov, joystick.povs[pov]);
        }
      }

      // Necessary to update certain fields like axes
      DriverStationSim.notifyNewData();
    }
  }
}
