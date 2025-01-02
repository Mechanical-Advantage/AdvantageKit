// Copyright 2021-2025 FRC 6328
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

package org.littletonrobotics.junction.networktables;

/**
 * Manages a number value published to the "SmartDashboard" table of NT.
 * 
 * @deprecated Use {@link LoggedNetworkNumber} with a "/SmartDashboard" prefix
 *             (e.g. "/SmartDashboard/...")
 */
@Deprecated
public class LoggedDashboardNumber extends LoggedNetworkNumber {

  /**
   * Creates a new LoggedDashboardNumber, for handling a number input sent to the
   * "SmartDashboard" table of NetworkTables.
   *
   * @param key The key for the number, published to the "SmartDashboard" table of
   *            NT or "/DashboardInputs/{key}" when logged.
   */
  public LoggedDashboardNumber(String key) {
    super("/SmartDashboard/" + key);
  }

  /**
   * Creates a new LoggedDashboardNumber, for handling a number input sent to the
   * "SmartDashboard" table of NetworkTables.
   *
   * @param key          The key for the number, published to the "SmartDashboard"
   *                     table of NT or "/DashboardInputs/{key}" when logged.
   * @param defaultValue The default value if no value in NT is found.
   */
  public LoggedDashboardNumber(String key, double defaultValue) {
    super("/SmartDashboard/" + key, defaultValue);
  }
}
