// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

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
