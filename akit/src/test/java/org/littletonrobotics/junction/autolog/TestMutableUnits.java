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
 * Units tests for testing storing/retrieving mutable units
 * in a log table.
 */
public class TestMutableUnits {
  @Test
  public void TestMutAngle() {
    LogTable table = new LogTable(0);

    MutAngle mutAngle = Rotations.mutable(0.1);
    MutAngle defaultMutAngle = Rotations.mutable(0.2);
    table.put("AMutableAngle", mutAngle);
    MutAngle restoredMutAngle = table.get("AMutableAngle", defaultMutAngle);
    assertTrue(restoredMutAngle.isEquivalent(mutAngle));
  }

  // remaining tests courtesy of Gemini 2.5pro
  @Test
  public void TestMutAngularAcceleration() {
    LogTable table = new LogTable(0);
    MutAngularAcceleration mut = RadiansPerSecondPerSecond.mutable(0.1);
    MutAngularAcceleration defaultMut = RadiansPerSecondPerSecond.mutable(0.2);
    table.put("AMutAngularAcceleration", mut);
    MutAngularAcceleration restoredMut = table.get("AMutAngularAcceleration", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutAngularVelocity() {
    LogTable table = new LogTable(0);
    MutAngularVelocity mut = RadiansPerSecond.mutable(0.1);
    MutAngularVelocity defaultMut = RadiansPerSecond.mutable(0.2);
    table.put("AMutAngularVelocity", mut);
    MutAngularVelocity restoredMut = table.get("AMutAngularVelocity", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutCurrent() {
    LogTable table = new LogTable(0);
    MutCurrent mut = Amps.mutable(0.1);
    MutCurrent defaultMut = Amps.mutable(0.2);
    table.put("AMutCurrent", mut);
    MutCurrent restoredMut = table.get("AMutCurrent", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutDimensionless() {
    LogTable table = new LogTable(0);
    MutDimensionless mut = Value.mutable(0.1);
    MutDimensionless defaultMut = Value.mutable(0.2);
    table.put("AMutDimensionless", mut);
    MutDimensionless restoredMut = table.get("AMutDimensionless", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutDistance() {
    LogTable table = new LogTable(0);
    MutDistance mut = Meters.mutable(0.1);
    MutDistance defaultMut = Meters.mutable(0.2);
    table.put("AMutDistance", mut);
    MutDistance restoredMut = table.get("AMutDistance", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutEnergy() {
    LogTable table = new LogTable(0);
    MutEnergy mut = Joules.mutable(0.1);
    MutEnergy defaultMut = Joules.mutable(0.2);
    table.put("AMutEnergy", mut);
    MutEnergy restoredMut = table.get("AMutEnergy", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutForce() {
    LogTable table = new LogTable(0);
    MutForce mut = Newtons.mutable(0.1);
    MutForce defaultMut = Newtons.mutable(0.2);
    table.put("AMutForce", mut);
    MutForce restoredMut = table.get("AMutForce", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutFrequency() {
    LogTable table = new LogTable(0);
    MutFrequency mut = Hertz.mutable(0.1);
    MutFrequency defaultMut = Hertz.mutable(0.2);
    table.put("AMutFrequency", mut);
    MutFrequency restoredMut = table.get("AMutFrequency", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutLinearAcceleration() {
    LogTable table = new LogTable(0);
    MutLinearAcceleration mut = MetersPerSecondPerSecond.mutable(0.1);
    MutLinearAcceleration defaultMut = MetersPerSecondPerSecond.mutable(0.2);
    table.put("AMutLinearAcceleration", mut);
    MutLinearAcceleration restoredMut = table.get("AMutLinearAcceleration", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutLinearVelocity() {
    LogTable table = new LogTable(0);
    MutLinearVelocity mut = MetersPerSecond.mutable(0.1);
    MutLinearVelocity defaultMut = MetersPerSecond.mutable(0.2);
    table.put("AMutLinearVelocity", mut);
    MutLinearVelocity restoredMut = table.get("AMutLinearVelocity", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutMass() {
    LogTable table = new LogTable(0);
    MutMass mut = Kilograms.mutable(0.1);
    MutMass defaultMut = Kilograms.mutable(0.2);
    table.put("AMutMass", mut);
    MutMass restoredMut = table.get("AMutMass", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutMomentOfInertia() {
    LogTable table = new LogTable(0);
    MutMomentOfInertia mut = KilogramSquareMeters.mutable(0.1);
    MutMomentOfInertia defaultMut = KilogramSquareMeters.mutable(0.2);
    table.put("AMutMomentOfInertia", mut);
    MutMomentOfInertia restoredMut = table.get("AMutMomentOfInertia", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutPower() {
    LogTable table = new LogTable(0);
    MutPower mut = Watts.mutable(0.1);
    MutPower defaultMut = Watts.mutable(0.2);
    table.put("AMutPower", mut);
    MutPower restoredMut = table.get("AMutPower", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutResistance() {
    LogTable table = new LogTable(0);
    MutResistance mut = Ohms.mutable(0.1);
    MutResistance defaultMut = Ohms.mutable(0.2);
    table.put("AMutResistance", mut);
    MutResistance restoredMut = table.get("AMutResistance", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutTemperature() {
    LogTable table = new LogTable(0);
    MutTemperature mut = Celsius.mutable(0.1);
    MutTemperature defaultMut = Celsius.mutable(0.2);
    table.put("AMutTemperature", mut);
    MutTemperature restoredMut = table.get("AMutTemperature", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutTime() {
    LogTable table = new LogTable(0);
    MutTime mut = Seconds.mutable(0.1);
    MutTime defaultMut = Seconds.mutable(0.2);
    table.put("AMutTime", mut);
    MutTime restoredMut = table.get("AMutTime", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutTorque() {
    LogTable table = new LogTable(0);
    MutTorque mut = NewtonMeters.mutable(0.1);
    MutTorque defaultMut = NewtonMeters.mutable(0.2);
    table.put("AMutTorque", mut);
    MutTorque restoredMut = table.get("AMutTorque", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }

  @Test
  public void TestMutVoltage() {
    LogTable table = new LogTable(0);
    MutVoltage mut = Volts.mutable(0.1);
    MutVoltage defaultMut = Volts.mutable(0.2);
    table.put("AMutVoltage", mut);
    MutVoltage restoredMut = table.get("AMutVoltage", defaultMut);
    assertTrue(restoredMut.isEquivalent(mut));
  }
}
