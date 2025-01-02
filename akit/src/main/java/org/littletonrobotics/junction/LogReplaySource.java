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
 * Provides a stream of log entries to be fed back to the robot code during
 * simulation.
 */
public interface LogReplaySource {

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
   * Called every loop cycle to get the next set of data.
   * 
   * @param table A reference to the current data table, to be updated with new
   *              data (including a timestamp).
   * @return A boolean indicating whether the replay should continue.
   */
  public boolean updateTable(LogTable table);
}
