// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj;

import org.littletonrobotics.junction.inputs.LoggedDriverStation;

import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.hal.ControlWord;
import edu.wpi.first.hal.DriverStationJNI;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.MatchInfoData;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.util.datalog.BooleanArrayLogEntry;
import edu.wpi.first.util.datalog.BooleanLogEntry;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.FloatArrayLogEntry;
import edu.wpi.first.util.datalog.IntegerArrayLogEntry;
import edu.wpi.first.util.EventVector;
import edu.wpi.first.util.WPIUtilJNI;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provide access to the network communication data to / from the Driver
 * Station. Patched by AdvantageKit to support logging.
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength", "PMD.ExcessivePublicCount", "PMD.GodClass",
    "PMD.TooManyFields" })
public final class DriverStation {
  /** Number of Joystick Ports. */
  public static final int kJoystickPorts = 6;
  /** The robot alliance that the robot is a part of. */
  public enum Alliance {
    Red, Blue, Invalid
  }

  public enum MatchType {
    None, Practice, Qualification, Elimination
  }
  private static final double JOYSTICK_UNPLUGGED_MESSAGE_INTERVAL = 1.0;
  private static double m_nextMessageTime;

  private static LoggedDriverStation logDS = LoggedDriverStation.getInstance();

  // Joystick button rising/falling edge flags
  private static int[] m_lastJoystickButtonsPressDetect = new int[kJoystickPorts];
  private static int[] m_lastJoystickButtonsReleaseDetect = new int[kJoystickPorts];


  private static boolean m_silenceJoystickWarning;

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
   * @param printTrace If true, append stack trace to error string
   */
  public static void reportError(String error, boolean printTrace) {
    reportErrorImpl(true, 1, error, printTrace);
  }

  /**
   * Report error to Driver Station. Appends provided stack trace to error
   * message.
   *
   * @param stackTrace The stack trace to append
   */
  public static void reportError(String error, StackTraceElement[] stackTrace) {
    reportErrorImpl(true, 1, error, stackTrace);
  }

  /**
   * Report warning to Driver Station. Optionally appends Stack trace to warning
   * message.
   *
   * @param printTrace If true, append stack trace to warning string
   */
  public static void reportWarning(String error, boolean printTrace) {
    reportErrorImpl(false, 1, error, printTrace);
  }

  /**
   * Report warning to Driver Station. Appends provided stack trace to warning
   * message.
   *
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
    DriverStationJNI.sendError(
        isError, code, false, error, locString, traceString.toString(), true);
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

    if (button <= logDS.getJoystickData(stick).buttonCount) {
      return (logDS.getJoystickData(stick).buttonValues & (1 << (button - 1))) != 0;
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

    if (button <= logDS.getJoystickData(stick).buttonCount) {
      if ((logDS.getJoystickData(stick).buttonValues & (1 << (button - 1))) != 0) { // Currently pressed
        if ((~m_lastJoystickButtonsPressDetect[stick] & (1 << (button - 1))) != 0) { // Released last time
          m_lastJoystickButtonsPressDetect[stick] |= (1 << (button - 1));
          return true;
        } else { // Also pressed last time
          return false;
        }
      } else {
        m_lastJoystickButtonsPressDetect[stick] &= ~(1 << (button - 1)); // Currently released, reset flag
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

    if (button <= logDS.getJoystickData(stick).buttonCount) {
      if ((logDS.getJoystickData(stick).buttonValues & (1 << (button - 1))) != 0) {
        m_lastJoystickButtonsReleaseDetect[stick] |= (1 << (button - 1)); // Currently pressed, reset flag
        return false;
      } else { // Currently released
        if ((m_lastJoystickButtonsReleaseDetect[stick] & (1 << (button - 1))) != 0) { // Pressed last time
          m_lastJoystickButtonsReleaseDetect[stick] &= ~(1 << (button - 1));
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

    if (axis < logDS.getJoystickData(stick).axisValues.length) {
      return logDS.getJoystickData(stick).axisValues[axis];
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

    if (pov < logDS.getJoystickData(stick).povs.length) {
      return logDS.getJoystickData(stick).povs[pov];
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

    return logDS.getJoystickData(stick).buttonValues;
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

    return logDS.getJoystickData(stick).axisValues.length;
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

    return logDS.getJoystickData(stick).povs.length;
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

    return logDS.getJoystickData(stick).buttonCount;
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

    return logDS.getJoystickData(stick).xbox;
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

    return logDS.getJoystickData(stick).type;
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

    return logDS.getJoystickData(stick).name;
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

    return logDS.getJoystickData(stick).axisTypes[axis];
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
    return logDS.getDSData().enabled && logDS.getDSData().dsAttached;
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
    return logDS.getDSData().emergencyStop;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in autonomous mode.
   *
   * @return True if autonomous mode should be enabled, false otherwise.
   */
  public static boolean isAutonomous() {
    return logDS.getDSData().autonomous;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in autonomous mode and enabled.
   *
   * @return True if autonomous should be set and the robot should be enabled.
   */
  public static boolean isAutonomousEnabled() {
    return logDS.getDSData().autonomous && logDS.getDSData().enabled;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in
   * operator-controlled mode.
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
    return !logDS.getDSData().autonomous && !logDS.getDSData().test && logDS.getDSData().enabled;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in test mode.
   *
   * @return True if test mode should be enabled, false otherwise.
   */
  public static boolean isTest() {
    return logDS.getDSData().test;
  }

  /**
   * Gets a value indicating whether the Driver Station is attached.
   *
   * @return True if Driver Station is attached, false otherwise.
   */
  public static boolean isDSAttached() {
    return logDS.getDSData().dsAttached;
  }

  /**
   * Gets if the driver station attached to a Field Management System.
   *
   * @return true if the robot is competing on a field being controlled by a Field
   *         Management System
   */
  public static boolean isFMSAttached() {
    return logDS.getDSData().fmsAttached;
  }

  /**
   * Get the game specific message.
   *
   * @return the game specific message
   */
  public static String getGameSpecificMessage() {
    return logDS.getDSData().gameSpecificMessage;
  }

  /**
   * Get the event name.
   *
   * @return the event name
   */
  public static String getEventName() {
    return logDS.getDSData().eventName;
  }

  /**
   * Get the match type.
   *
   * @return the match type
   */
  public static MatchType getMatchType() {
    int matchType = logDS.getDSData().matchType;
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
    return logDS.getDSData().matchNumber;
  }

  /**
   * Get the replay number.
   *
   * @return the replay number
   */
  public static int getReplayNumber() {
    return logDS.getDSData().replayNumber;
  }

  /**
   * Get the current alliance from the FMS.
   *
   * @return the current alliance
   */
  public static Alliance getAlliance() {
    switch (logDS.getDSData().allianceStation) {
      case 0:
      case 1:
      case 2:
        return Alliance.Red;

      case 3:
      case 4:
      case 5:
        return Alliance.Blue;

      default:
        return Alliance.Invalid;
    }
  }

  /**
   * Gets the location of the team's driver station controls.
   *
   * @return the location of the team's driver station controls: 1, 2, or 3
   */
  public static int getLocation() {
    switch (logDS.getDSData().allianceStation) {
      case 0:
      case 3:
        return 1;

      case 1:
      case 4:
        return 2;

      case 2:
      case 5:
        return 3;

      default:
        return 0;
    }
  }

  /**
   * Return the approximate match time. The FMS does not send an official match
   * time to the robots, but does send an approximate match time. The value will
   * count down the time remaining in the current period (auto or teleop).
   * Warning: This is not an official time (so it cannot be used to dispute ref
   * calls or guarantee that a function will trigger before the match ends) The
   * Practice Match function of the DS approximates the behavior seen on the
   * field.
   *
   * @return Time remaining in current match period (auto or teleop) in seconds
   */
  public static double getMatchTime() {
    return logDS.getDSData().matchTime;
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
   * @param word Word to refresh.
   */
  public static void refreshControlWordFromCache(ControlWord word) {}

    /**
   * Copy data from the DS task for the user. If no new data exists, it will just be returned,
   * otherwise the data will be copied from the DS polling loop.  This method has been patched
   * by AdvantageKit and is nonfunctional.
   */
  public static void refreshData() {}

  public static void provideRefreshedDataEventHandle(int handle) {}

  public static void removeRefreshedDataEventHandle(int handle) {}

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
   * Starts logging DriverStation data to data log. Repeated calls are ignored. This method
   * has been patched by AdvantageKit and is nonfunctional.
   *
   * @param log data log
   * @param logJoysticks if true, log joystick data
   */
  @SuppressWarnings("PMD.NonThreadSafeSingleton")
  public static void startDataLog(DataLog log, boolean logJoysticks) {
  }

  /**
   * Starts logging DriverStation data to data log, including joystick data. Repeated calls are
   * ignored. This method has been patched by AdvantageKit and is nonfunctional.
   *
   * @param log data log
   */
  public static void startDataLog(DataLog log) {
  }
}
