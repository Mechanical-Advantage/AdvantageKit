// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

/** Receives entries from the logging system during real operation or simulation. */
public interface LogDataReceiver {
  /** Data receivers may optionally log the current timestamp using this key. */
  public static final String timestampKey = "/Timestamp";

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
   * Called every loop cycle when a new table is complete. This data can be processed immediately or
   * queued for later.
   *
   * @param table A copy of the data to save.
   * @throws InterruptedException If interrupted while processing.
   */
  public void putTable(LogTable table) throws InterruptedException;
}
