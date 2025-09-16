// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.hal.DriverStationJNI;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.MatchType;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import org.littletonrobotics.conduit.ConduitApi;

/** Manages logging and replaying data from the driver station (robot state, joysticks, etc.) */
class LoggedDriverStation {
  private LoggedDriverStation() {}

  /** Save the current DS state to the log table. */
  public static void saveToLog(LogTable table) {
    ConduitApi conduit = ConduitApi.getInstance();

    table.put("AllianceStation", conduit.getAllianceStation());
    table.put("EventName", conduit.getEventName().trim());
    table.put("GameSpecificMessage", conduit.getGameSpecificMessage().trim());
    table.put("MatchNumber", conduit.getMatchNumber());
    table.put("ReplayNumber", conduit.getReplayNumber());
    table.put("MatchType", conduit.getMatchType());
    table.put("MatchTime", conduit.getMatchTime());

    int controlWord = conduit.getControlWord();
    table.put("Enabled", (controlWord & 1) != 0);
    table.put("Autonomous", (controlWord & 2) != 0);
    table.put("Test", (controlWord & 4) != 0);
    table.put("EmergencyStop", (controlWord & 8) != 0);
    table.put("FMSAttached", (controlWord & 16) != 0);
    table.put("DSAttached", (controlWord & 32) != 0);

    for (int id = 0; id < DriverStation.kJoystickPorts; id++) {
      LogTable joystickTable = table.getSubtable("Joystick" + Integer.toString(id));
      joystickTable.put("Name", conduit.getJoystickName(id).trim());
      joystickTable.put("Type", conduit.getJoystickType(id));
      joystickTable.put("Xbox", conduit.isXbox(id));
      joystickTable.put("ButtonCount", conduit.getButtonCount(id));
      joystickTable.put("ButtonValues", conduit.getButtonValues(id));

      int povCount = conduit.getPovCount(id);
      int[] povValues = new int[povCount];
      System.arraycopy(conduit.getPovValues(id), 0, povValues, 0, povCount);
      joystickTable.put("POVs", povValues);

      int axisCount = conduit.getAxisCount(id);
      float[] axisValues = new float[axisCount];
      int[] axisTypes = new int[axisCount];
      System.arraycopy(conduit.getAxisValues(id), 0, axisValues, 0, axisCount);
      System.arraycopy(conduit.getAxisTypes(id), 0, axisTypes, 0, axisCount);
      joystickTable.put("AxisValues", axisValues);
      joystickTable.put("AxisTypes", axisTypes);
    }
  }

  /** Read the DS state to the log table and publish to the HAL sim. */
  public static void replayFromLog(LogTable table) {
    DriverStationSim.setAllianceStationId(
        switch (table.get("AllianceStation", 0)) {
          case DriverStationJNI.kRed1AllianceStation -> AllianceStationID.Red1;
          case DriverStationJNI.kRed2AllianceStation -> AllianceStationID.Red2;
          case DriverStationJNI.kRed3AllianceStation -> AllianceStationID.Red3;
          case DriverStationJNI.kBlue1AllianceStation -> AllianceStationID.Blue1;
          case DriverStationJNI.kBlue2AllianceStation -> AllianceStationID.Blue2;
          case DriverStationJNI.kBlue3AllianceStation -> AllianceStationID.Blue3;
          default -> AllianceStationID.Unknown;
        });
    DriverStationSim.setEventName(table.get("EventName", ""));
    DriverStationSim.setGameSpecificMessage(table.get("GameSpecificMessage", ""));
    DriverStationSim.setMatchNumber(table.get("MatchNumber", 0));
    DriverStationSim.setReplayNumber(table.get("ReplayNumber", 0));
    DriverStationSim.setMatchType(
        switch (table.get("MatchType", 0)) {
          case 1 -> MatchType.Practice;
          case 2 -> MatchType.Qualification;
          case 3 -> MatchType.Elimination;
          default -> MatchType.None;
        });
    DriverStationSim.setMatchTime(table.get("MatchTime", -1.0));

    boolean dsAttached = table.get("DSAttached", false);
    DriverStationSim.setEnabled(table.get("Enabled", false));
    DriverStationSim.setAutonomous(table.get("Autonomous", false));
    DriverStationSim.setTest(table.get("Test", false));
    DriverStationSim.setEStop(table.get("EmergencyStop", false));
    DriverStationSim.setFmsAttached(table.get("FMSAttached", false));
    DriverStationSim.setDsAttached(dsAttached);

    for (int id = 0; id < DriverStation.kJoystickPorts; id++) {
      LogTable joystickTable = table.getSubtable("Joystick" + Integer.toString(id));
      DriverStationSim.setJoystickName(id, joystickTable.get("Name", ""));
      DriverStationSim.setJoystickType(id, joystickTable.get("Type", 0));
      DriverStationSim.setJoystickIsXbox(id, joystickTable.get("Xbox", false));
      DriverStationSim.setJoystickButtonCount(id, joystickTable.get("ButtonCount", 0));
      DriverStationSim.setJoystickButtons(id, joystickTable.get("ButtonValues", 0));

      int[] povValues = joystickTable.get("POVs", new int[0]);
      DriverStationSim.setJoystickPOVCount(id, povValues.length);
      for (int i = 0; i < povValues.length; i++) {
        DriverStationSim.setJoystickPOV(id, i, povValues[i]);
      }

      float[] axisValues = joystickTable.get("AxisValues", new float[0]);
      int[] axisTypes = joystickTable.get("AxisTypes", new int[0]);
      DriverStationSim.setJoystickAxisCount(id, axisValues.length);
      for (int i = 0; i < axisValues.length; i++) {
        DriverStationSim.setJoystickAxis(id, i, axisValues[i]);
        DriverStationSim.setJoystickAxisType(id, i, axisTypes[i]);
      }
    }

    if (dsAttached) {
      // https://wpilib.slack.com/archives/C05ATJ3A2TC/p1730995258684039
      DriverStationSim.notifyNewData();
    }
  }
}
