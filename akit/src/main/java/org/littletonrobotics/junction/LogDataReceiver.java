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

package org.littletonrobotics.junction;

/**
 * Receives entries from the logging system during real operation or simulation.
 */
public interface LogDataReceiver {
  /** Data receivers may optionally log the current timestamp using this key. */
  public static final String timestampKey = "/Timestamp";

  /**
   * Called before the logging system begins reporting data. This should be used
   * to connect to files, find network devices, start threads, etc.
   */
  public default void start() {
  };

  /**
   * Called when the code shuts down cleanly. Note that this will NOT be called
   * when the robot is powered off.
   */
  public default void end() {
  };

  /**
   * Called every loop cycle when a new table is complete. This data can be
   * processed immediately or queued for later.
   * 
   * @param table A copy of the data to save.
   */
  public void putTable(LogTable table) throws InterruptedException;
}
