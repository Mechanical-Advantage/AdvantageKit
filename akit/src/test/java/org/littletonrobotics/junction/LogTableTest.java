// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.wpilib.units.Units.Rotations;

import org.junit.jupiter.api.Test;
import org.wpilib.units.measure.Angle;

/*
 * Units tests for testing storing/retrieving entries in a log table.
 */
public class LogTableTest {
  private static record TestRecord(int x, double y) {}

  @Test
  public void TestMeasureExplicit() {
    LogTable table = new LogTable(0);

    // 1. Test putMeasure and getMeasure explicitly
    Angle angle = Rotations.of(0.15);
    table.putMeasure("AngleExplicit", angle);

    Angle defaultAngle = Rotations.of(0.2);
    Angle restoredAngle = table.getMeasure("AngleExplicit", defaultAngle);
    assertTrue(restoredAngle.isEquivalent(angle));

    // Verify stored type and value
    assertEquals(angle.baseUnitMagnitude(), table.get("AngleExplicit").getDouble());
    assertEquals(angle.baseUnit().name(), table.get("AngleExplicit").unitStr);
  }

  @Test
  public void TestMeasureRecordPassthrough() {
    LogTable table = new LogTable(0);

    // 2. Test put and get delegation on Angle record-measures
    Angle angle = Rotations.of(0.1);
    table.put("AngleRecord", angle); // Calls put(String, R extends Record)

    Angle defaultAngle = Rotations.of(0.2);
    Angle restoredAngle =
        table.get("AngleRecord", defaultAngle); // Calls get(String, R extends Record)
    assertTrue(restoredAngle.isEquivalent(angle));

    // Verify that the delegation redirected it to a measure instead of a struct
    assertEquals(angle.baseUnitMagnitude(), table.get("AngleRecord").getDouble());
    assertEquals(angle.baseUnit().name(), table.get("AngleRecord").unitStr);
    assertTrue(table.get("AngleRecord").customTypeStr == null);
  }

  @Test
  public void TestStandardRecord() {
    LogTable table = new LogTable(0);

    // 3. Test standard records (not measures)
    TestRecord rec = new TestRecord(42, 3.14);
    table.put("NormalRecord", rec);

    TestRecord defaultRec = new TestRecord(0, 0.0);
    TestRecord restoredRec = table.get("NormalRecord", defaultRec);
    assertEquals(rec, restoredRec);

    // Verify that it is stored as a struct
    assertTrue(table.get("NormalRecord").customTypeStr.startsWith("struct:"));
  }
}
