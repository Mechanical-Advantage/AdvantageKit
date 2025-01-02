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
 * Manages a string value published to the "SmartDashboard" table of NT.
 * 
 * @deprecated Use {@link LoggedNetworkString} with a "/SmartDashboard" prefix
 *             (e.g. "/SmartDashboard/...")
 */
@Deprecated
public class LoggedDashboardString extends LoggedNetworkString {
  /**
   * Creates a new LoggedDashboardString, for handling a string input sent to the
   * "SmartDashboard" table of NetworkTables.
   *
   * @param key The key for the number, published to the "SmartDashboard" table of
   *            NT or
   *            "/DashboardInputs/{key}" when logged.
   */
  public LoggedDashboardString(String key) {
    super("/SmartDashboard/" + key);
  }

  /**
   * Creates a new LoggedDashboardString, for handling a string input sent to the
   * "SmartDashboard" table of NetworkTables.
   *
   * @param key          The key for the number, published to the "SmartDashboard"
   *                     table of NT or "/DashboardInputs/{key}" when logged.
   * @param defaultValue The default value if no value in NT is found.
   */
  public LoggedDashboardString(String key, String defaultValue) {
    super("/SmartDashboard/" + key, defaultValue);
  }
}
