package org.littletonrobotics.junction.autolog;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Celsius;
import static edu.wpi.first.units.Units.Hertz;
import static edu.wpi.first.units.Units.Joules;
import static edu.wpi.first.units.Units.Kilograms;
import static edu.wpi.first.units.Units.KilogramSquareMeters;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.MetersPerSecondPerSecond;
import static edu.wpi.first.units.Units.NewtonMeters;
import static edu.wpi.first.units.Units.Newtons;
import static edu.wpi.first.units.Units.Ohms;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.RadiansPerSecondPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Seconds;
import static edu.wpi.first.units.Units.Value;
import static edu.wpi.first.units.Units.Volts;
import static edu.wpi.first.units.Units.Watts;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.MutAngle;
import edu.wpi.first.units.measure.MutAngularAcceleration;
import edu.wpi.first.units.measure.MutAngularVelocity;
import edu.wpi.first.units.measure.MutCurrent;
import edu.wpi.first.units.measure.MutDimensionless;
import edu.wpi.first.units.measure.MutDistance;
import edu.wpi.first.units.measure.MutEnergy;
import edu.wpi.first.units.measure.MutForce;
import edu.wpi.first.units.measure.MutFrequency;
import edu.wpi.first.units.measure.MutLinearAcceleration;
import edu.wpi.first.units.measure.MutLinearVelocity;
import edu.wpi.first.units.measure.MutMass;
import edu.wpi.first.units.measure.MutMomentOfInertia;
import edu.wpi.first.units.measure.MutPower;
import edu.wpi.first.units.measure.MutResistance;
import edu.wpi.first.units.measure.MutTemperature;
import edu.wpi.first.units.measure.MutTime;
import edu.wpi.first.units.measure.MutTorque;
import edu.wpi.first.units.measure.MutVoltage;
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