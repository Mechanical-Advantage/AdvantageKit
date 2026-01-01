// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.autolog;

import static edu.wpi.first.units.Units.Rotations;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.MutAngle;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.LogTable;

/*
 * Units tests for testing storing/retrieving entries in a log table.
 */
public class LogTableTest {
  @Test
  public void TestMeasure() {
    LogTable table = new LogTable(0);

    Angle angle = Rotations.of(0.1);
    table.put("MutableAngle", angle);

    Angle defaultAngle = Rotations.of(0.2);
    Angle restoredAngle = table.get("MutableAngle", defaultAngle);
    assertTrue(restoredAngle.isEquivalent(angle));
  }

  @Test
  public void TestMutableMeasure() {
    LogTable table = new LogTable(0);

    MutAngle mutAngle = Rotations.mutable(0.1);
    table.put("Angle", mutAngle);

    MutAngle defaultMutAngle = Rotations.mutable(0.2);
    MutAngle restoredMutAngle = table.get("Angle", defaultMutAngle);
    assertTrue(restoredMutAngle.isEquivalent(mutAngle));
  }
}
