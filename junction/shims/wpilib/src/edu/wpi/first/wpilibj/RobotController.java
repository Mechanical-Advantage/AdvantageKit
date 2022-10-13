// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj;

import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggedSystemStats;

import edu.wpi.first.hal.HALUtil;
import edu.wpi.first.hal.PowerJNI;
import edu.wpi.first.hal.can.CANStatus;

/**
 * Contains functions for roboRIO functionality. Patched by AdvantageKit to
 * support logging. Patched by AdvantageKit to support logging.
 */
public final class RobotController {
  private RobotController() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Return the FPGA Version number. For now, expect this to be the current year.
   *
   * @return FPGA Version number.
   */
  @SuppressWarnings("AbbreviationAsWordInName")
  public static int getFPGAVersion() {
    return HALUtil.getFPGAVersion();
  }

  /**
   * Return the FPGA Revision number. The format of the revision is 3 numbers. The
   * 12 most significant bits are the Major Revision. the next 8 bits are the
   * Minor Revision. The 12 least significant bits are the Build Number.
   *
   * @return FPGA Revision number.
   */
  @SuppressWarnings("AbbreviationAsWordInName")
  public static long getFPGARevision() {
    return (long) HALUtil.getFPGARevision();
  }

  /**
   * Read the microsecond timer from the FPGA.
   * 
   * Patched by AdvantageKit to read the syncronized timestamp. To access the real
   * FPGA time for performance analysis, call
   * {@code Logger.getInstance().getRealTimestamp()} instead.
   *
   * @return The current time in microseconds according to the FPGA.
   */
  public static long getFPGATime() {
    return (long) (Logger.getInstance().getTimestamp() * 1000000.0);
  }

  /**
   * Get the state of the "USER" button on the roboRIO.
   *
   * @return true if the button is currently pressed down
   */
  public static boolean getUserButton() {
    return HALUtil.getFPGAButton();
  }

  /**
   * Read the battery voltage.
   *
   * @return The battery voltage in Volts.
   */
  public static double getBatteryVoltage() {
    return LoggedSystemStats.getInstance().getInputs().voltageVin;
  }

  /**
   * Gets a value indicating whether the FPGA outputs are enabled. The outputs may
   * be disabled if the robot is disabled or e-stopped, the watchdog has expired,
   * or if the roboRIO browns out.
   *
   * @return True if the FPGA outputs are enabled.
   */
  public static boolean isSysActive() {
    return LoggedSystemStats.getInstance().getInputs().systemActive;
  }

  /**
   * Check if the system is browned out.
   *
   * @return True if the system is browned out
   */
  public static boolean isBrownedOut() {
    return LoggedSystemStats.getInstance().getInputs().brownedOut;
  }

  /**
   * Get the input voltage to the robot controller.
   *
   * @return The controller input voltage value in Volts
   */
  public static double getInputVoltage() {
    return LoggedSystemStats.getInstance().getInputs().voltageVin;
  }

  /**
   * Get the input current to the robot controller.
   *
   * @return The controller input current value in Amps
   */
  public static double getInputCurrent() {
    return LoggedSystemStats.getInstance().getInputs().currentVin;
  }

  /**
   * Get the voltage of the 3.3V rail.
   *
   * @return The controller 3.3V rail voltage value in Volts
   */
  public static double getVoltage3V3() {
    return LoggedSystemStats.getInstance().getInputs().userVoltage3v3;
  }

  /**
   * Get the current output of the 3.3V rail.
   *
   * @return The controller 3.3V rail output current value in Amps
   */
  public static double getCurrent3V3() {
    return LoggedSystemStats.getInstance().getInputs().userCurrent3v3;
  }

  /**
   * Get the enabled state of the 3.3V rail. The rail may be disabled due to a
   * controller brownout, a short circuit on the rail, or controller over-voltage.
   *
   * @return The controller 3.3V rail enabled value
   */
  public static boolean getEnabled3V3() {
    return LoggedSystemStats.getInstance().getInputs().userActive3v3;
  }

  /**
   * Get the count of the total current faults on the 3.3V rail since the
   * controller has booted.
   *
   * @return The number of faults
   */
  public static int getFaultCount3V3() {
    return LoggedSystemStats.getInstance().getInputs().userCurrentFaults3v3;
  }

  /**
   * Get the voltage of the 5V rail.
   *
   * @return The controller 5V rail voltage value in Volts
   */
  public static double getVoltage5V() {
    return LoggedSystemStats.getInstance().getInputs().userVoltage5v;
  }

  /**
   * Get the current output of the 5V rail.
   *
   * @return The controller 5V rail output current value in Amps
   */
  public static double getCurrent5V() {
    return LoggedSystemStats.getInstance().getInputs().userCurrent5v;
  }

  /**
   * Get the enabled state of the 5V rail. The rail may be disabled due to a
   * controller brownout, a short circuit on the rail, or controller over-voltage.
   *
   * @return The controller 5V rail enabled value
   */
  public static boolean getEnabled5V() {
    return LoggedSystemStats.getInstance().getInputs().userActive5v;
  }

  /**
   * Get the count of the total current faults on the 5V rail since the controller
   * has booted.
   *
   * @return The number of faults
   */
  public static int getFaultCount5V() {
    return LoggedSystemStats.getInstance().getInputs().userCurrentFaults5v;
  }

  /**
   * Get the voltage of the 6V rail.
   *
   * @return The controller 6V rail voltage value in Volts
   */
  public static double getVoltage6V() {
    return LoggedSystemStats.getInstance().getInputs().userVoltage6v;
  }

  /**
   * Get the current output of the 6V rail.
   *
   * @return The controller 6V rail output current value in Amps
   */
  public static double getCurrent6V() {
    return LoggedSystemStats.getInstance().getInputs().userCurrent6v;
  }

  /**
   * Get the enabled state of the 6V rail. The rail may be disabled due to a
   * controller brownout, a short circuit on the rail, or controller over-voltage.
   *
   * @return The controller 6V rail enabled value
   */
  public static boolean getEnabled6V() {
    return LoggedSystemStats.getInstance().getInputs().userActive6v;
  }

  /**
   * Get the count of the total current faults on the 6V rail since the controller
   * has booted.
   *
   * @return The number of faults
   */
  public static int getFaultCount6V() {
    return LoggedSystemStats.getInstance().getInputs().userCurrentFaults6v;
  }

  /**
   * Get the current brownout voltage setting.
   *
   * @return The brownout voltage
   */
  public static double getBrownoutVoltage() {
    return PowerJNI.getBrownoutVoltage();
  }

  /**
   * Set the voltage the roboRIO will brownout and disable all outputs.
   *
   * <p>
   * Note that this only does anything on the roboRIO 2. On the roboRIO it is a
   * no-op.
   *
   * @param brownoutVoltage The brownout voltage
   */
  public static void setBrownoutVoltage(double brownoutVoltage) {
    PowerJNI.setBrownoutVoltage(brownoutVoltage);
  }

  /**
   * Get the current status of the CAN bus.
   *
   * @return The status of the CAN bus
   */
  public static CANStatus getCANStatus() {
    return LoggedSystemStats.getInstance().getInputs().canStatus;
  }
}
