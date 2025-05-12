// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.networktables;

/**
 * Manages a boolean value published to the "SmartDashboard" table of NT.
 *
 * @deprecated Use {@link LoggedNetworkBoolean} with a "/SmartDashboard" prefix (e.g.
 *     "/SmartDashboard/...")
 */
@Deprecated
public class LoggedDashboardBoolean extends LoggedNetworkBoolean {
  /**
   * Creates a new LoggedDashboardBoolean, for handling a boolean input sent to the "SmartDashboard"
   * table of NetworkTables.
   *
   * @param key The key for the number, published to the "SmartDashboard" table of NT or
   *     "/DashboardInputs/{key}" when logged.
   */
  public LoggedDashboardBoolean(String key) {
    super("/SmartDashboard/" + key);
  }

  /**
   * Creates a new LoggedDashboardBoolean, for handling a boolean input sent to the "SmartDashboard"
   * table of NetworkTables.
   *
   * @param key The key for the number, published to the "SmartDashboard" table of NT or
   *     "/DashboardInputs/{key}" when logged.
   * @param defaultValue The default value if no value in NT is found.
   */
  public LoggedDashboardBoolean(String key, boolean defaultValue) {
    super("/SmartDashboard/" + key, defaultValue);
  }
}
