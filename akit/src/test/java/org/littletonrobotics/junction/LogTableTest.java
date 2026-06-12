// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.autolog;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.LogTable;
import org.wpilib.units.Units.Rotations;
import org.wpilib.units.measure.Angle;

/*
 * Units tests for testing storing/retrieving entries in a log table.
 */
public class LogTableTest {
  @Test
  public void TestMeasure() {
    LogTable table = new LogTable(0);

    Angle angle = Rotations.of(0.1);
    table.put("Angle", angle);

    Angle defaultAngle = Rotations.of(0.2);
    Angle restoredAngle = table.get("Angle", defaultAngle);
    assertTrue(restoredAngle.isEquivalent(angle));
  }
}
