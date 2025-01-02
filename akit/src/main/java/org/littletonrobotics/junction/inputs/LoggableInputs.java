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

package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.junction.LogTable;

/**
 * A set of values which can be logged and replayed (for example, the hardware
 * inputs for a subsystem). Data is stored in LogTable objects.
 */
public interface LoggableInputs {
  /**
   * Updates a LogTable with the data to log.
   */
  public void toLog(LogTable table);

  /**
   * Updates data based on a LogTable.
   */
  public void fromLog(LogTable table);
}
