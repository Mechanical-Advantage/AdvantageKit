// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.junction.LogTable;

/**
 * A set of values which can be logged and replayed (for example, the hardware inputs for a
 * subsystem). Data is stored in LogTable objects.
 */
public interface LoggableInputs {
  /**
   * Updates a LogTable with the data to log.
   *
   * @param table The table to which data should be written.
   */
  public void toLog(LogTable table);

  /**
   * Updates data based on a LogTable.
   *
   * @param table The table from which data should be read.
   */
  public void fromLog(LogTable table);
}
