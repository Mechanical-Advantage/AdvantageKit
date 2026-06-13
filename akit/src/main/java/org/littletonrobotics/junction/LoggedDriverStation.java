// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import org.littletonrobotics.conduit.ConduitApi;
import org.wpilib.driverstation.MatchType;
import org.wpilib.hardware.hal.AllianceStationID;
import org.wpilib.hardware.hal.RobotMode;
import org.wpilib.hardware.hal.simulation.DriverStationDataJNI;
import org.wpilib.simulation.DriverStationSim;

/** Manages logging and replaying data from the driver station (robot state, joysticks, etc.) */
class LoggedDriverStation {
  private LoggedDriverStation() {}

  /** Save the current DS state to the log table. */
  public static void saveToLog(LogTable table) {
    ConduitApi conduit = ConduitApi.getInstance();

    table.put("AllianceStation", conduit.getAllianceStation());
    table.put("EventName", conduit.getEventName());
    table.put("GameData", conduit.getGameData());
    table.put("MatchNumber", conduit.getMatchNumber());
    table.put("ReplayNumber", conduit.getReplayNumber());
    table.put("MatchType", conduit.getMatchType());
    table.put("MatchTime", conduit.getMatchTime());

    long controlWord = conduit.getControlWord();
    table.put("Enabled", (controlWord & (1L << 58)) != 0);
    table.put("RobotMode", RobotMode.fromInt((int) (controlWord >> 56) & 3));
    table.put("OpMode/Id", conduit.getOpModeId());
    table.put("OpMode/Name", conduit.getOpModeName());
    table.put("OpMode/Group", conduit.getOpModeGroup());
    table.put("OpMode/Description", conduit.getOpModeDescription());
    table.put("OpMode/TextColor", conduit.getOpModeTextColor());
    table.put("OpMode/BackgroundColor", conduit.getOpModeBackgroundColor());
    table.put("EmergencyStop", (controlWord & (1L << 59)) != 0);
    table.put("FMSAttached", (controlWord & (1L << 60)) != 0);
    table.put("DSAttached", (controlWord & (1L << 61)) != 0);

    for (int id = 0; id < ConduitApi.NUM_JOYSTICKS; id++) {
      LogTable joystickTable = table.getSubtable("Joystick" + Integer.toString(id));
      joystickTable.put("Name", conduit.getJoystickName(id).trim());
      joystickTable.put("Type", conduit.getJoystickType(id));
      joystickTable.put("IsGamepad", conduit.isGamepad(id));
      joystickTable.put("SupportedOutputs", conduit.getJoystickSupportedOutputs(id));
      joystickTable.put("ButtonsAvailable", conduit.getButtonsAvailable(id));
      joystickTable.put("ButtonValues", conduit.getButtonValues(id));

      int povCount = conduit.getPovCount(id);
      int[] povValues = new int[povCount];
      System.arraycopy(conduit.getPovValues(id), 0, povValues, 0, povCount);
      joystickTable.put("POVs", povValues);

      int axisCount = conduit.getAxisCount(id);
      float[] axisValues = new float[axisCount];
      int[] axisRawValues = new int[axisCount];
      System.arraycopy(conduit.getAxisValues(id), 0, axisValues, 0, axisCount);
      short[] rawFromConduit = conduit.getJoystickAxisRaw(id);
      for (int i = 0; i < axisCount; i++) {
        axisRawValues[i] = rawFromConduit[i];
      }
      joystickTable.put("AxisValues", axisValues);
      joystickTable.put("AxisRawValues", axisRawValues);

      int touchpadCount = conduit.getTouchpadCount(id);
      joystickTable.put("TouchpadCount", touchpadCount);
      for (int t = 0; t < touchpadCount; t++) {
        int fingerCount = conduit.getTouchpadFingerCount(id, t);
        joystickTable.put("Touchpad/" + t + "/FingerCount", fingerCount);
        for (int f = 0; f < fingerCount; f++) {
          joystickTable.put(
              "Touchpad/" + t + "/Finger/" + f + "/Down", conduit.getTouchpadFingerDown(id, t, f));
          joystickTable.put(
              "Touchpad/" + t + "/Finger/" + f + "/X", conduit.getTouchpadFingerX(id, t, f));
          joystickTable.put(
              "Touchpad/" + t + "/Finger/" + f + "/Y", conduit.getTouchpadFingerY(id, t, f));
        }
      }
    }
  }

  /** Read the DS state to the log table and publish to the HAL sim. */
  public static void replayFromLog(LogTable table) {
    DriverStationSim.setAllianceStationId(
        switch (table.get("AllianceStation", 0)) {
          case 1 -> AllianceStationID.RED_1;
          case 2 -> AllianceStationID.RED_2;
          case 3 -> AllianceStationID.RED_3;
          case 4 -> AllianceStationID.BLUE_1;
          case 5 -> AllianceStationID.BLUE_2;
          case 6 -> AllianceStationID.BLUE_3;
          default -> AllianceStationID.UNKNOWN;
        });
    DriverStationSim.setEventName(table.get("EventName", ""));
    DriverStationSim.setGameData(table.get("GameData", ""));
    DriverStationSim.setMatchNumber(table.get("MatchNumber", 0));
    DriverStationSim.setReplayNumber(table.get("ReplayNumber", 0));
    DriverStationSim.setMatchType(
        switch (table.get("MatchType", 0)) {
          case 1 -> MatchType.PRACTICE;
          case 2 -> MatchType.QUALIFICATION;
          case 3 -> MatchType.ELIMINATION;
          default -> MatchType.NONE;
        });
    DriverStationSim.setMatchTime(table.get("MatchTime", -1.0));

    boolean dsAttached = table.get("DSAttached", false);
    DriverStationSim.setEnabled(table.get("Enabled", false));
    DriverStationSim.setRobotMode(table.get("RobotMode", RobotMode.UNKNOWN));
    DriverStationSim.setOpMode(table.get("OpMode/Id", 0L));
    DriverStationSim.setEStop(table.get("EmergencyStop", false));
    DriverStationSim.setFmsAttached(table.get("FMSAttached", false));
    DriverStationSim.setDsAttached(dsAttached);

    for (int id = 0; id < ConduitApi.NUM_JOYSTICKS; id++) {
      LogTable joystickTable = table.getSubtable("Joystick" + Integer.toString(id));
      DriverStationSim.setJoystickName(id, joystickTable.get("Name", ""));
      DriverStationSim.setJoystickGamepadType(id, joystickTable.get("Type", 0));
      DriverStationSim.setJoystickIsGamepad(id, joystickTable.get("IsGamepad", false));
      DriverStationSim.setJoystickSupportedOutputs(id, joystickTable.get("SupportedOutputs", 0));
      DriverStationSim.setJoystickButtonsAvailable(
          id, joystickTable.get("ButtonsAvailable", joystickTable.get("ButtonCount", 0L)));
      DriverStationDataJNI.setJoystickButtonsValue(id, joystickTable.get("ButtonValues", 0L));

      int[] povValues = joystickTable.get("POVs", new int[0]);
      DriverStationSim.setJoystickPOVsAvailable(id, povValues.length);
      for (int i = 0; i < povValues.length; i++) {
        DriverStationDataJNI.setJoystickPOV(id, i, (byte) povValues[i]);
      }

      float[] axisValues = joystickTable.get("AxisValues", new float[0]);
      DriverStationSim.setJoystickAxesAvailable(id, axisValues.length);
      for (int i = 0; i < axisValues.length; i++) {
        DriverStationSim.setJoystickAxis(id, i, axisValues[i]);
      }

      int touchpadCount = joystickTable.get("TouchpadCount", 0);
      int[] fingerCounts = new int[touchpadCount];
      for (int t = 0; t < touchpadCount; t++) {
        fingerCounts[t] = joystickTable.get("Touchpad/" + t + "/FingerCount", 0);
      }
      DriverStationDataJNI.setTouchpadCounts(id, touchpadCount, fingerCounts);
      for (int t = 0; t < touchpadCount; t++) {
        int fingerCount = fingerCounts[t];
        for (int f = 0; f < fingerCount; f++) {
          boolean down = joystickTable.get("Touchpad/" + t + "/Finger/" + f + "/Down", false);
          float x = joystickTable.get("Touchpad/" + t + "/Finger/" + f + "/X", 0.0f);
          float y = joystickTable.get("Touchpad/" + t + "/Finger/" + f + "/Y", 0.0f);
          DriverStationDataJNI.setTouchpadFinger(id, t, f, down, x, y);
        }
      }
    }

    if (dsAttached) {
      // https://wpilib.slack.com/archives/C05ATJ3A2TC/p1730995258684039
      DriverStationSim.notifyNewData();
    }
  }
}
