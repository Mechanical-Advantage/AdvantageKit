// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

// Copyright 2021-2024 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package edu.wpi.first.wpilibj;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.littletonrobotics.junction.inputs.LoggedDriverStation;

import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.hal.ControlWord;
import edu.wpi.first.hal.DriverStationJNI;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.util.EventVector;
import edu.wpi.first.util.datalog.DataLog;

/**
 * Provide access to the network communication data to / from the Driver
 * Station. Patched by AdvantageKit to support logging.
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength", "PMD.ExcessivePublicCount", "PMD.GodClass",
    "PMD.TooManyFields" })
public class DriverStation {
  /** Number of Joystick Ports. */
  public static final int kJoystickPorts = 6;

  /** The robot alliance that the robot is a part of. */
  public enum Alliance {
    Red, Blue
  }

  public enum MatchType {
    None, Practice, Qualification, Elimination
  }

  private static final double JOYSTICK_UNPLUGGED_MESSAGE_INTERVAL = 1.0;
  private static double m_nextMessageTime;

  private static EventVector m_refreshEvents = new EventVector();

  // Joystick button rising/falling edge flags
  private static int[] m_lastJoystickButtonsPressed = new int[kJoystickPorts];
  private static int[] m_lastJoystickButtonsReleased = new int[kJoystickPorts];

  private static boolean m_silenceJoystickWarning;

  // Robot state status variables
  private static boolean m_userInDisabled;
  private static boolean m_userInAutonomous;
  private static boolean m_userInTeleop;
  private static boolean m_userInTest;

  /**
   * DriverStation constructor.
   *
   * <p>
   * The single DriverStation instance is created statically with the instance
   * static member variable.
   */
  private DriverStation() {
  }

  static {
    HAL.initialize(500, 0);
  }

  /**
   * Report error to Driver Station. Optionally appends Stack trace to error
   * message.
   *
   * @param error      The error to report.
   * @param printTrace If true, append stack trace to error string
   */
  public static void reportError(String error, boolean printTrace) {
    reportErrorImpl(true, 1, error, printTrace);
  }

  /**
   * Report error to Driver Station. Appends provided stack trace to error
   * message.
   *
   * @param error      The error to report.
   * @param stackTrace The stack trace to append
   */
  public static void reportError(String error, StackTraceElement[] stackTrace) {
    reportErrorImpl(true, 1, error, stackTrace);
  }

  /**
   * Report warning to Driver Station. Optionally appends Stack trace to warning
   * message.
   *
   * @param warning    The warning to report.
   * @param printTrace If true, append stack trace to warning string
   */
  public static void reportWarning(String error, boolean printTrace) {
    reportErrorImpl(false, 1, error, printTrace);
  }

  /**
   * Report warning to Driver Station. Appends provided stack trace to warning
   * message.
   *
   * @param warning    The warning to report.
   * @param stackTrace The stack trace to append
   */
  public static void reportWarning(String error, StackTraceElement[] stackTrace) {
    reportErrorImpl(false, 1, error, stackTrace);
  }

  private static void reportErrorImpl(boolean isError, int code, String error, boolean printTrace) {
    reportErrorImpl(isError, code, error, printTrace, Thread.currentThread().getStackTrace(), 3);
  }

  private static void reportErrorImpl(boolean isError, int code, String error, StackTraceElement[] stackTrace) {
    reportErrorImpl(isError, code, error, true, stackTrace, 0);
  }

  private static void reportErrorImpl(boolean isError, int code, String error, boolean printTrace,
      StackTraceElement[] stackTrace, int stackTraceFirst) {
    String locString;
    if (stackTrace.length >= stackTraceFirst + 1) {
      locString = stackTrace[stackTraceFirst].toString();
    } else {
      locString = "";
    }
    StringBuilder traceString = new StringBuilder();
    if (printTrace) {
      boolean haveLoc = false;
      for (int i = stackTraceFirst; i < stackTrace.length; i++) {
        String loc = stackTrace[i].toString();
        traceString.append("\tat ").append(loc).append('\n');
        // get first user function
        if (!haveLoc && !loc.startsWith("edu.wpi.first")) {
          locString = loc;
          haveLoc = true;
        }
      }
    }
    DriverStationJNI.sendError(isError, code, false, error, locString, traceString.toString(), true);
  }

  /**
   * The state of one joystick button. Button indexes begin at 1.
   *
   * @param stick  The joystick to read.
   * @param button The button index, beginning at 1.
   * @return The state of the joystick button.
   */
  public static boolean getStickButton(final int stick, final int button) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-3");
    }
    if (button <= 0) {
      reportJoystickUnpluggedError("Button indexes begin at 1 in WPILib for C++ and Java\n");
      return false;
    }

    if (button <= LoggedDriverStation.getJoystickData(stick).buttonCount) {
      return (LoggedDriverStation.getJoystickData(stick).buttonValues & (1 << (button - 1))) != 0;
    }

    reportJoystickUnpluggedWarning(
        "Joystick Button " + button + " on port " + stick + " not available, check if controller is plugged in");
    return false;
  }

  /**
   * Whether one joystick button was pressed since the last check. Button indexes
   * begin at 1.
   *
   * @param stick  The joystick to read.
   * @param button The button index, beginning at 1.
   * @return Whether the joystick button was pressed since the last check.
   */
  public static boolean getStickButtonPressed(final int stick, final int button) {
    if (button <= 0) {
      reportJoystickUnpluggedError("Button indexes begin at 1 in WPILib for C++ and Java\n");
      return false;
    }
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-3");
    }

    if (button <= LoggedDriverStation.getJoystickData(stick).buttonCount) {
      if ((LoggedDriverStation.getJoystickData(stick).buttonValues & (1 << (button - 1))) != 0) { // Currently pressed
        if ((~m_lastJoystickButtonsPressed[stick] & (1 << (button - 1))) != 0) { // Released last time
          m_lastJoystickButtonsPressed[stick] |= (1 << (button - 1));
          return true;
        } else { // Also pressed last time
          return false;
        }
      } else {
        m_lastJoystickButtonsPressed[stick] &= ~(1 << (button - 1)); // Currently released, reset flag
        return false;
      }
    }

    reportJoystickUnpluggedWarning(
        "Joystick Button " + button + " on port " + stick + " not available, check if controller is plugged in");
    return false;
  }

  /**
   * Whether one joystick button was released since the last check. Button indexes
   * begin at 1.
   *
   * @param stick  The joystick to read.
   * @param button The button index, beginning at 1.
   * @return Whether the joystick button was released since the last check.
   */
  public static boolean getStickButtonReleased(final int stick, final int button) {
    if (button <= 0) {
      reportJoystickUnpluggedError("Button indexes begin at 1 in WPILib for C++ and Java\n");
      return false;
    }
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-3");
    }

    if (button <= LoggedDriverStation.getJoystickData(stick).buttonCount) {
      if ((LoggedDriverStation.getJoystickData(stick).buttonValues & (1 << (button - 1))) != 0) {
        m_lastJoystickButtonsReleased[stick] |= (1 << (button - 1)); // Currently pressed, reset flag
        return false;
      } else { // Currently released
        if ((m_lastJoystickButtonsReleased[stick] & (1 << (button - 1))) != 0) { // Pressed last time
          m_lastJoystickButtonsReleased[stick] &= ~(1 << (button - 1));
          return true;
        } else { // Also released last time
          return false;
        }
      }
    }

    reportJoystickUnpluggedWarning(
        "Joystick Button " + button + " on port " + stick + " not available, check if controller is plugged in");
    return false;
  }

  /**
   * Get the value of the axis on a joystick. This depends on the mapping of the
   * joystick connected to the specified port.
   *
   * @param stick The joystick to read.
   * @param axis  The analog axis value to read from the joystick.
   * @return The value of the axis on the joystick.
   */
  public static double getStickAxis(int stick, int axis) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }
    if (axis < 0 || axis >= DriverStationJNI.kMaxJoystickAxes) {
      throw new IllegalArgumentException("Joystick axis is out of range");
    }

    if (axis < LoggedDriverStation.getJoystickData(stick).axisValues.length) {
      return LoggedDriverStation.getJoystickData(stick).axisValues[axis];
    }

    reportJoystickUnpluggedWarning(
        "Joystick axis " + axis + " on port " + stick + " not available, check if controller is plugged in");
    return 0.0;
  }

  /**
   * Get the state of a POV on the joystick.
   *
   * @return the angle of the POV in degrees, or -1 if the POV is not pressed.
   */
  public static int getStickPOV(int stick, int pov) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }
    if (pov < 0 || pov >= DriverStationJNI.kMaxJoystickPOVs) {
      throw new IllegalArgumentException("Joystick POV is out of range");
    }

    if (pov < LoggedDriverStation.getJoystickData(stick).povs.length) {
      return LoggedDriverStation.getJoystickData(stick).povs[pov];
    }

    reportJoystickUnpluggedWarning(
        "Joystick POV " + pov + " on port " + stick + " not available, check if controller is plugged in");
    return -1;
  }

  /**
   * The state of the buttons on the joystick.
   *
   * @param stick The joystick to read.
   * @return The state of the buttons on the joystick.
   */
  public static int getStickButtons(final int stick) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-3");
    }

    return LoggedDriverStation.getJoystickData(stick).buttonValues;
  }

  /**
   * Returns the number of axes on a given joystick port.
   *
   * @param stick The joystick port number
   * @return The number of axes on the indicated joystick
   */
  public static int getStickAxisCount(int stick) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }

    return LoggedDriverStation.getJoystickData(stick).axisValues.length;
  }

  /**
   * Returns the number of POVs on a given joystick port.
   *
   * @param stick The joystick port number
   * @return The number of POVs on the indicated joystick
   */
  public static int getStickPOVCount(int stick) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }

    return LoggedDriverStation.getJoystickData(stick).povs.length;
  }

  /**
   * Gets the number of buttons on a joystick.
   *
   * @param stick The joystick port number
   * @return The number of buttons on the indicated joystick
   */
  public static int getStickButtonCount(int stick) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }

    return LoggedDriverStation.getJoystickData(stick).buttonCount;
  }

  /**
   * Gets the value of isXbox on a joystick.
   *
   * @param stick The joystick port number
   * @return A boolean that returns the value of isXbox
   */
  public static boolean getJoystickIsXbox(int stick) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }

    return LoggedDriverStation.getJoystickData(stick).xbox;
  }

  /**
   * Gets the value of type on a joystick.
   *
   * @param stick The joystick port number
   * @return The value of type
   */
  public static int getJoystickType(int stick) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }

    return LoggedDriverStation.getJoystickData(stick).type;
  }

  /**
   * Gets the name of the joystick at a port.
   *
   * @param stick The joystick port number
   * @return The value of name
   */
  public static String getJoystickName(int stick) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }

    return LoggedDriverStation.getJoystickData(stick).name;
  }

  /**
   * Returns the types of Axes on a given joystick port.
   *
   * @param stick The joystick port number
   * @param axis  The target axis
   * @return What type of axis the axis is reporting to be
   */
  public static int getJoystickAxisType(int stick, int axis) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }

    return LoggedDriverStation.getJoystickData(stick).axisTypes[axis];
  }

  /**
   * Returns if a joystick is connected to the Driver Station.
   *
   * <p>
   * This makes a best effort guess by looking at the reported number of axis,
   * buttons, and POVs attached.
   *
   * @param stick The joystick port number
   * @return true if a joystick is connected
   */
  public static boolean isJoystickConnected(int stick) {
    return getStickAxisCount(stick) > 0 || getStickButtonCount(stick) > 0 || getStickPOVCount(stick) > 0;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * enabled.
   *
   * @return True if the robot is enabled, false otherwise.
   */
  public static boolean isEnabled() {
    return LoggedDriverStation.getDSData().enabled && LoggedDriverStation.getDSData().dsAttached;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * disabled.
   *
   * @return True if the robot should be disabled, false otherwise.
   */
  public static boolean isDisabled() {
    return !isEnabled();
  }

  /**
   * Gets a value indicating whether the Robot is e-stopped.
   *
   * @return True if the robot is e-stopped, false otherwise.
   */
  public static boolean isEStopped() {
    return LoggedDriverStation.getDSData().emergencyStop;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in autonomous mode.
   *
   * @return True if autonomous mode should be enabled, false otherwise.
   */
  public static boolean isAutonomous() {
    return LoggedDriverStation.getDSData().autonomous;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in autonomous mode and enabled.
   *
   * @return True if autonomous should be set and the robot should be enabled.
   */
  public static boolean isAutonomousEnabled() {
    return LoggedDriverStation.getDSData().autonomous && LoggedDriverStation.getDSData().enabled;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in operator-controlled mode.
   *
   * @return True if operator-controlled mode should be enabled, false otherwise.
   */
  public static boolean isTeleop() {
    return !(isAutonomous() || isTest());
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in operator-controller mode and enabled.
   *
   * @return True if operator-controlled mode should be set and the robot should
   *         be enabled.
   */
  public static boolean isTeleopEnabled() {
    return !LoggedDriverStation.getDSData().autonomous && !LoggedDriverStation.getDSData().test
        && LoggedDriverStation.getDSData().enabled;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in test mode.
   *
   * @return True if test mode should be enabled, false otherwise.
   */
  public static boolean isTest() {
    return LoggedDriverStation.getDSData().test;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in Test
   * mode and enabled.
   *
   * @return True if test mode should be set and the robot should be enabled.
   */
  public static boolean isTestEnabled() {
    return LoggedDriverStation.getDSData().test && LoggedDriverStation.getDSData().enabled;
  }

  /**
   * Gets a value indicating whether the Driver Station is attached.
   *
   * @return True if Driver Station is attached, false otherwise.
   */
  public static boolean isDSAttached() {
    return LoggedDriverStation.getDSData().dsAttached;
  }

  /**
   * Gets if a new control packet from the driver station arrived since the last
   * time this function was called.
   *
   * @return True if the control data has been updated since the last call.
   */
  public static boolean isNewControlData() {
    return true;
  }

  /**
   * Gets if the driver station attached to a Field Management System.
   *
   * @return true if the robot is competing on a field being controlled by a Field
   *         Management System
   */
  public static boolean isFMSAttached() {
    return LoggedDriverStation.getDSData().fmsAttached;
  }

  /**
   * Get the game specific message.
   *
   * @return the game specific message
   */
  public static String getGameSpecificMessage() {
    return LoggedDriverStation.getDSData().gameSpecificMessage;
  }

  /**
   * Get the event name.
   *
   * @return the event name
   */
  public static String getEventName() {
    return LoggedDriverStation.getDSData().eventName;
  }

  /**
   * Get the match type.
   *
   * @return the match type
   */
  public static MatchType getMatchType() {
    int matchType = LoggedDriverStation.getDSData().matchType;
    switch (matchType) {
      case 1:
        return MatchType.Practice;
      case 2:
        return MatchType.Qualification;
      case 3:
        return MatchType.Elimination;
      default:
        return MatchType.None;
    }
  }

  /**
   * Get the match number.
   *
   * @return the match number
   */
  public static int getMatchNumber() {
    return LoggedDriverStation.getDSData().matchNumber;
  }

  /**
   * Get the replay number.
   *
   * @return the replay number
   */
  public static int getReplayNumber() {
    return LoggedDriverStation.getDSData().replayNumber;
  }

  private static Map<AllianceStationID, Optional<Alliance>> m_allianceMap = Map.of(
      AllianceStationID.Unknown, Optional.empty(),
      AllianceStationID.Red1, Optional.of(Alliance.Red),
      AllianceStationID.Red2, Optional.of(Alliance.Red),
      AllianceStationID.Red3, Optional.of(Alliance.Red),
      AllianceStationID.Blue1, Optional.of(Alliance.Blue),
      AllianceStationID.Blue2, Optional.of(Alliance.Blue),
      AllianceStationID.Blue3, Optional.of(Alliance.Blue));

  private static Map<AllianceStationID, OptionalInt> m_stationMap = Map.of(
      AllianceStationID.Unknown, OptionalInt.empty(),
      AllianceStationID.Red1, OptionalInt.of(1),
      AllianceStationID.Red2, OptionalInt.of(2),
      AllianceStationID.Red3, OptionalInt.of(3),
      AllianceStationID.Blue1, OptionalInt.of(1),
      AllianceStationID.Blue2, OptionalInt.of(2),
      AllianceStationID.Blue3, OptionalInt.of(3));

  /**
   * Get the current alliance from the FMS.
   *
   * <p>
   * If the FMS is not connected, it is set from the team alliance setting on the
   * driver station.
   *
   * @return the current alliance
   */
  public static Optional<Alliance> getAlliance() {
    AllianceStationID allianceStationID = getRawAllianceStation();
    if (allianceStationID == null) {
      allianceStationID = AllianceStationID.Unknown;
    }

    return m_allianceMap.get(allianceStationID);
  }

  /**
   * Gets the location of the team's driver station controls from the FMS.
   *
   * <p>
   * If the FMS is not connected, it is set from the team alliance setting on the
   * driver station.
   *
   * @return the location of the team's driver station controls: 1, 2, or 3
   */
  public static OptionalInt getLocation() {
    AllianceStationID allianceStationID = getRawAllianceStation();
    if (allianceStationID == null) {
      allianceStationID = AllianceStationID.Unknown;
    }

    return m_stationMap.get(allianceStationID);
  }

  /**
   * Gets the raw alliance station of the teams driver station.
   *
   * <p>
   * This returns the raw low level value. Prefer getLocation or getAlliance
   * unless necessary for performance.
   *
   * @return The raw alliance station id.
   */
  public static AllianceStationID getRawAllianceStation() {
    switch (LoggedDriverStation.getDSData().allianceStation) {
      case DriverStationJNI.kUnknownAllianceStation:
        return AllianceStationID.Unknown;
      case DriverStationJNI.kRed1AllianceStation:
        return AllianceStationID.Red1;
      case DriverStationJNI.kRed2AllianceStation:
        return AllianceStationID.Red2;
      case DriverStationJNI.kRed3AllianceStation:
        return AllianceStationID.Red3;
      case DriverStationJNI.kBlue1AllianceStation:
        return AllianceStationID.Blue1;
      case DriverStationJNI.kBlue2AllianceStation:
        return AllianceStationID.Blue2;
      case DriverStationJNI.kBlue3AllianceStation:
        return AllianceStationID.Blue3;
      default:
        return null;
    }
  }

  /**
   * Wait for a DS connection. This method has been patched by AdvantageKit and is
   * nonfunctional (the return value is always false).
   *
   * @param timeoutSeconds timeout in seconds. 0 for infinite.
   * @return true if connected, false if timeout
   */
  public static boolean waitForDsConnection(double timeoutSeconds) {
    return false;
  }

  /**
   * Return the approximate match time. The FMS does not send an official match
   * time to the robots, but does send an approximate match time. The value will
   * count down the time remaining in the current period (auto or teleop).
   * Warning: This is not an official time (so it cannot be used to dispute ref
   * calls or guarantee that a function will trigger before the match ends).
   *
   * <p>
   * When connected to the real field, this number only changes in full integer
   * increments, and always counts down.
   *
   * <p>
   * When the DS is in practice mode, this number is a floating point number, and
   * counts down.
   *
   * <p>
   * When the DS is in teleop or autonomous mode, this number is a floating point
   * number, and counts up.
   *
   * <p>
   * Simulation matches DS behavior without an FMS connected.
   *
   * @return Time remaining in current match period (auto or teleop) in seconds
   */
  public static double getMatchTime() {
    return LoggedDriverStation.getDSData().matchTime;
  }

  /**
   * Allows the user to specify whether they want joystick connection warnings to
   * be printed to the console. This setting is ignored when the FMS is connected
   * -- warnings will always be on in that scenario.
   *
   * @param silence Whether warning messages should be silenced.
   */
  public static void silenceJoystickConnectionWarning(boolean silence) {
    m_silenceJoystickWarning = silence;
  }

  /**
   * Returns whether joystick connection warnings are silenced. This will always
   * return false when connected to the FMS.
   *
   * @return Whether joystick connection warnings are silenced.
   */
  public static boolean isJoystickConnectionWarningSilenced() {
    return !isFMSAttached() && m_silenceJoystickWarning;
  }

  /**
   * Refresh the passed in control word to contain the current control word cache.
   * This method has been patched by AdvantageKit and is nonfunctional.
   *
   * @param word Word to update.
   */
  public static void refreshControlWordFromCache(ControlWord word) {
  }

  /**
   * Copy data from the DS task for the user. If no new data exists, it will just
   * be returned, otherwise the data will be copied from the DS polling loop. This
   * method has been patched by AdvantageKit and is nonfunctional, except for
   * triggering refresh events.
   */
  public static void refreshData() {
    m_refreshEvents.wakeup();
  }

  public static void provideRefreshedDataEventHandle(int handle) {
    m_refreshEvents.add(handle);
  }

  public static void removeRefreshedDataEventHandle(int handle) {
    m_refreshEvents.remove(handle);
  }

  /**
   * Reports errors related to unplugged joysticks Throttles the errors so that
   * they don't overwhelm the DS.
   */
  private static void reportJoystickUnpluggedError(String message) {
    double currentTime = Timer.getFPGATimestamp();
    if (currentTime > m_nextMessageTime) {
      reportError(message, false);
      m_nextMessageTime = currentTime + JOYSTICK_UNPLUGGED_MESSAGE_INTERVAL;
    }
  }

  /**
   * Reports errors related to unplugged joysticks Throttles the errors so that
   * they don't overwhelm the DS.
   */
  private static void reportJoystickUnpluggedWarning(String message) {
    if (isFMSAttached() || !m_silenceJoystickWarning) {
      double currentTime = Timer.getFPGATimestamp();
      if (currentTime > m_nextMessageTime) {
        reportWarning(message, false);
        m_nextMessageTime = currentTime + JOYSTICK_UNPLUGGED_MESSAGE_INTERVAL;
      }
    }
  }

  /**
   * Starts logging DriverStation data to data log. Repeated calls are ignored.
   * This method has been patched by AdvantageKit and is nonfunctional.
   *
   * @param log          data log
   * @param logJoysticks if true, log joystick data
   */
  @SuppressWarnings("PMD.NonThreadSafeSingleton")
  public static void startDataLog(DataLog log, boolean logJoysticks) {
  }

  /**
   * Starts logging DriverStation data to data log, including joystick data.
   * Repeated calls are ignored. This method has been patched by AdvantageKit and
   * is nonfunctional.
   *
   * @param log data log
   */
  public static void startDataLog(DataLog log) {
  }
}
