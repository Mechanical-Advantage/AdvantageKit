// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj;

import org.littletonrobotics.junction.inputs.LoggedDriverStation;

import edu.wpi.first.hal.HAL;

/**
 * Provide access to the network communication data to / from the Driver
 * Station.
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength", "PMD.ExcessivePublicCount", "PMD.GodClass",
    "PMD.TooManyFields" })
public class DriverStation {
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
  private double m_nextMessageTime;

  private static class DriverStationTask implements Runnable {
    private final DriverStation m_ds;

    DriverStationTask(DriverStation ds) {
      m_ds = ds;
    }

    @Override
    public void run() {
      m_ds.run();
    }
  } /* DriverStationTask */

  private static DriverStation instance = new DriverStation();
  private static LoggedDriverStation logDS = LoggedDriverStation.getInstance();

  // Joystick button rising/falling edge flags
  private int[] m_lastJoystickButtonsPressDetect = new int[kJoystickPorts];
  private int[] m_lastJoystickButtonsReleaseDetect = new int[kJoystickPorts];

  // Internal Driver Station thread
  @SuppressWarnings("PMD.SingularField")
  private final Thread m_thread;

  private volatile boolean m_threadKeepAlive = true;

  private boolean m_silenceJoystickWarning;

  // Robot state status variables
  private boolean m_userInDisabled;
  private boolean m_userInAutonomous;
  private boolean m_userInTeleop;
  private boolean m_userInTest;

  /**
   * Gets an instance of the DriverStation.
   *
   * @return The DriverStation.
   */
  public static DriverStation getInstance() {
    return DriverStation.instance;
  }

  /**
   * DriverStation constructor.
   *
   * <p>
   * The single DriverStation instance is created statically with the instance
   * static member variable.
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private DriverStation() {
    HAL.initialize(500, 0);

    m_thread = new Thread(new DriverStationTask(this), "FRCDriverStation");
    m_thread.setPriority((Thread.NORM_PRIORITY + Thread.MAX_PRIORITY) / 2);

    m_thread.start();
  }

  /** Kill the thread. */
  public void release() {
    m_threadKeepAlive = false;
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
    HAL.sendError(isError, code, false, error, locString, traceString.toString(), true);
  }

  /**
   * The state of one joystick button. Button indexes begin at 1.
   *
   * @param stick  The joystick to read.
   * @param button The button index, beginning at 1.
   * @return The state of the joystick button.
   */
  public boolean getStickButton(final int stick, final int button) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-3");
    }
    if (button <= 0) {
      reportJoystickUnpluggedError("Button indexes begin at 1 in WPILib for C++ and Java\n");
      return false;
    }

    if (button <= logDS.getJoystickData(stick).buttonCount) {
      return (logDS.getJoystickData(stick).buttonValues & 1 << (button - 1)) != 0;
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
  public boolean getStickButtonPressed(final int stick, final int button) {
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
  public boolean getStickButtonReleased(final int stick, final int button) {
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
  public double getStickAxis(int stick, int axis) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }
    if (axis < 0 || axis >= HAL.kMaxJoystickAxes) {
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
  public int getStickPOV(int stick, int pov) {
    if (stick < 0 || stick >= kJoystickPorts) {
      throw new IllegalArgumentException("Joystick index is out of range, should be 0-5");
    }
    if (pov < 0 || pov >= HAL.kMaxJoystickPOVs) {
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
  public int getStickButtons(final int stick) {
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
  public int getStickAxisCount(int stick) {
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
  public int getStickPOVCount(int stick) {
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
  public int getStickButtonCount(int stick) {
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
  public boolean getJoystickIsXbox(int stick) {
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
  public int getJoystickType(int stick) {
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
  public String getJoystickName(int stick) {
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
  public int getJoystickAxisType(int stick, int axis) {
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
  public boolean isJoystickConnected(int stick) {
    return getStickAxisCount(stick) > 0 || getStickButtonCount(stick) > 0 || getStickPOVCount(stick) > 0;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * enabled.
   *
   * @return True if the robot is enabled, false otherwise.
   */
  public boolean isEnabled() {
    return logDS.getDSData().enabled && logDS.getDSData().dsAttached;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * disabled.
   *
   * @return True if the robot should be disabled, false otherwise.
   */
  public boolean isDisabled() {
    return !isEnabled();
  }

  /**
   * Gets a value indicating whether the Robot is e-stopped.
   *
   * @return True if the robot is e-stopped, false otherwise.
   */
  public boolean isEStopped() {
    return logDS.getDSData().emergencyStop;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in autonomous mode.
   *
   * @return True if autonomous mode should be enabled, false otherwise.
   */
  public boolean isAutonomous() {
    return logDS.getDSData().autonomous;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in autonomous mode and enabled.
   *
   * @return True if autonomous should be set and the robot should be enabled.
   */
  public boolean isAutonomousEnabled() {
    return logDS.getDSData().autonomous && logDS.getDSData().enabled;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in operator-controlled mode.
   *
   * @return True if operator-controlled mode should be enabled, false otherwise.
   */
  public boolean isOperatorControl() {
    return !(isAutonomous() || isTest());
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in operator-controller mode and enabled.
   *
   * @return True if operator-controlled mode should be set and the robot should
   *         be enabled.
   */
  public boolean isOperatorControlEnabled() {
    return !logDS.getDSData().autonomous && !logDS.getDSData().test && !logDS.getDSData().enabled;
  }

  /**
   * Gets a value indicating whether the Driver Station requires the robot to be
   * running in test mode.
   *
   * @return True if test mode should be enabled, false otherwise.
   */
  public boolean isTest() {
    return logDS.getDSData().test;
  }

  /**
   * Gets a value indicating whether the Driver Station is attached.
   *
   * @return True if Driver Station is attached, false otherwise.
   */
  public boolean isDSAttached() {
    return logDS.getDSData().dsAttached;
  }

  /**
   * Gets if a new control packet from the driver station arrived since the last
   * time this function was called.
   *
   * @return True if the control data has been updated since the last call.
   */
  public boolean isNewControlData() {
    return true;
  }

  /**
   * Gets if the driver station attached to a Field Management System.
   *
   * @return true if the robot is competing on a field being controlled by a Field
   *         Management System
   */
  public boolean isFMSAttached() {
    return logDS.getDSData().fmsAttached;
  }

  /**
   * Get the game specific message.
   *
   * @return the game specific message
   */
  public String getGameSpecificMessage() {
    return logDS.getDSData().gameSpecificMessage;
  }

  /**
   * Get the event name.
   *
   * @return the event name
   */
  public String getEventName() {
    return logDS.getDSData().eventName;
  }

  /**
   * Get the match type.
   *
   * @return the match type
   */
  public MatchType getMatchType() {
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
  public int getMatchNumber() {
    return logDS.getDSData().matchNumber;
  }

  /**
   * Get the replay number.
   *
   * @return the replay number
   */
  public int getReplayNumber() {
    return logDS.getDSData().replayNumber;
  }

  /**
   * Get the current alliance from the FMS.
   *
   * @return the current alliance
   */
  public Alliance getAlliance() {
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
  public int getLocation() {
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
   * Wait for new data from the driver station.
   *
   * <p>
   * Checks if new control data has arrived since the last waitForData call on the
   * current thread. If new data has not arrived, returns immediately.
   */
  public void waitForData() {
    waitForData(0);
  }

  /**
   * Wait for new data or for timeout, which ever comes first. If timeout is 0,
   * wait for new data only.
   *
   * <p>
   * Checks if new control data has arrived since the last waitForData call on the
   * current thread. If new data has not arrived, returns immediately.
   *
   * @param timeout The maximum time in seconds to wait.
   * @return true if there is new data, otherwise false
   */
  public boolean waitForData(double timeout) {
    return true;
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
  public double getMatchTime() {
    return logDS.getDSData().matchTime;
  }

  /**
   * Only to be used to tell the Driver Station what code you claim to be
   * executing for diagnostic purposes only.
   *
   * @param entering If true, starting disabled code; if false, leaving disabled
   *                 code
   */
  @SuppressWarnings("MethodName")
  public void InDisabled(boolean entering) {
    m_userInDisabled = entering;
  }

  /**
   * Only to be used to tell the Driver Station what code you claim to be
   * executing for diagnostic purposes only.
   *
   * @param entering If true, starting autonomous code; if false, leaving
   *                 autonomous code
   */
  @SuppressWarnings("MethodName")
  public void InAutonomous(boolean entering) {
    m_userInAutonomous = entering;
  }

  /**
   * Only to be used to tell the Driver Station what code you claim to be
   * executing for diagnostic purposes only.
   *
   * @param entering If true, starting teleop code; if false, leaving teleop code
   */
  @SuppressWarnings("MethodName")
  public void InOperatorControl(boolean entering) {
    m_userInTeleop = entering;
  }

  /**
   * Only to be used to tell the Driver Station what code you claim to be
   * executing for diagnostic purposes only.
   *
   * @param entering If true, starting test code; if false, leaving test code
   */
  @SuppressWarnings("MethodName")
  public void InTest(boolean entering) {
    m_userInTest = entering;
  }

  /** Forces waitForData() to return immediately. */
  public void wakeupWaitForData() {
    return;
  }

  /**
   * Allows the user to specify whether they want joystick connection warnings to
   * be printed to the console. This setting is ignored when the FMS is connected
   * -- warnings will always be on in that scenario.
   *
   * @param silence Whether warning messages should be silenced.
   */
  public void silenceJoystickConnectionWarning(boolean silence) {
    m_silenceJoystickWarning = silence;
  }

  /**
   * Returns whether joystick connection warnings are silenced. This will always
   * return false when connected to the FMS.
   *
   * @return Whether joystick connection warnings are silenced.
   */
  public boolean isJoystickConnectionWarningSilenced() {
    return !isFMSAttached() && m_silenceJoystickWarning;
  }

  /**
   * Reports errors related to unplugged joysticks Throttles the errors so that
   * they don't overwhelm the DS.
   */
  private void reportJoystickUnpluggedError(String message) {
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
  private void reportJoystickUnpluggedWarning(String message) {
    if (isFMSAttached() || !m_silenceJoystickWarning) {
      double currentTime = Timer.getFPGATimestamp();
      if (currentTime > m_nextMessageTime) {
        reportWarning(message, false);
        m_nextMessageTime = currentTime + JOYSTICK_UNPLUGGED_MESSAGE_INTERVAL;
      }
    }
  }

  /** Provides the service routine for the DS polling m_thread. */
  private void run() {
    int safetyCounter = 0;
    while (m_threadKeepAlive) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        return;
      }

      if (isDisabled()) {
        safetyCounter = 0;
      }

      safetyCounter++;
      if (safetyCounter >= 4) {
        MotorSafety.checkMotors();
        safetyCounter = 0;
      }
      if (m_userInDisabled) {
        HAL.observeUserProgramDisabled();
      }
      if (m_userInAutonomous) {
        HAL.observeUserProgramAutonomous();
      }
      if (m_userInTeleop) {
        HAL.observeUserProgramTeleop();
      }
      if (m_userInTest) {
        HAL.observeUserProgramTest();
      }
    }
  }

}
