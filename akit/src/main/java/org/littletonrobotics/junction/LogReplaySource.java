// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

/** Provides a stream of log entries to be fed back to the robot code during simulation. */
public interface LogReplaySource {

  /**
   * Called before the logging system begins reporting data. This should be used to connect to
   * files, find network devices, start threads, etc.
   */
  public default void start() {}

  /**
   * Called when the code shuts down cleanly. Note that this will NOT be called when the robot is
   * powered off.
   */
  public default void end() {}

  /**
   * Called every loop cycle to get the next set of data.
   *
   * @param table A reference to the current data table, to be updated with new data (including a
   *     timestamp).
   * @return A boolean indicating whether the replay should continue.
   */
  public boolean updateTable(LogTable table);
}
