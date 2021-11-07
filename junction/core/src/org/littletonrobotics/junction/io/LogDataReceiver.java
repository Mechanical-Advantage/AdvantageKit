// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.littletonrobotics.junction.io;

import org.littletonrobotics.junction.LogTable;

/**
 * Receives entries from the logging system during real operation or simulation.
 */
public interface LogDataReceiver {

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
   * Called every loop cycle when a new entry is complete. This data can be
   * processed immediately or queued for later.
   */
  public void putEntry(LogTable entry);
}
