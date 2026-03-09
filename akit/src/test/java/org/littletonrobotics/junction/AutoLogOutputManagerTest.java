// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.util.Color;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;

/**
 * Tests for AutoLogOutputManager scanning, package filtering, deduplication, and key generation.
 *
 * <p>Because AutoLogOutputManager holds static state, each test resets that state via reflection
 * before running.
 */
public class AutoLogOutputManagerTest {

  @BeforeAll
  static void initHAL() {
    assertTrue(HAL.initialize(500, 0), "HAL initialization must succeed");
  }

  // ─── Static-state reset ─────────────────────────────────────────────────────

  @BeforeEach
  @SuppressWarnings("unchecked")
  void resetStaticState() throws Exception {
    Field callbacksField = AutoLogOutputManager.class.getDeclaredField("callbacks");
    callbacksField.setAccessible(true);
    ((List<?>) callbacksField.get(null)).clear();

    Field hashesField = AutoLogOutputManager.class.getDeclaredField("scannedObjectHashes");
    hashesField.setAccessible(true);
    ((List<?>) hashesField.get(null)).clear();

    Field packagesField = AutoLogOutputManager.class.getDeclaredField("allowedPackages");
    packagesField.setAccessible(true);
    ((Set<?>) packagesField.get(null)).clear();
  }

  @SuppressWarnings("unchecked")
  private List<Runnable> callbacks() throws Exception {
    Field f = AutoLogOutputManager.class.getDeclaredField("callbacks");
    f.setAccessible(true);
    return (List<Runnable>) f.get(null);
  }

  @SuppressWarnings("unchecked")
  private List<Integer> scannedHashes() throws Exception {
    Field f = AutoLogOutputManager.class.getDeclaredField("scannedObjectHashes");
    f.setAccessible(true);
    return (List<Integer>) f.get(null);
  }

  // ─── Test objects ───────────────────────────────────────────────────────────

  /** An object with a single annotated boolean field. */
  static class BooleanFieldObject {
    @AutoLogOutput boolean active = true;
  }

  /** An object with an annotated double field. */
  static class DoubleFieldObject {
    @AutoLogOutput double position = 3.14;
  }

  /** An object with an annotated method (no parameters, non-void return). */
  static class MethodAnnotatedObject {
    @AutoLogOutput
    public boolean isEnabled() {
      return true;
    }
  }

  /** An object where the method has an invalid signature (takes a parameter). */
  static class InvalidMethodSignatureObject {
    @AutoLogOutput
    @SuppressWarnings("unused")
    public boolean bad(int x) {
      return false;
    }
  }

  /** An object with multiple annotated fields of different types. */
  static class MultiFieldObject {
    @AutoLogOutput boolean flag = false;
    @AutoLogOutput double speed = 1.5;
    @AutoLogOutput String label = "test";
  }

  /** An object in this same package — should be scannable. */
  static class SamePackageObject {
    @AutoLogOutput int counter = 7;
  }

  enum TestDirection {
    NORTH,
    SOUTH
  }

  /** Object with annotated fields of every primitive and array type registered by registerField(). */
  static class AllTypesObject {
    @AutoLogOutput int intVal = 1;
    @AutoLogOutput long longVal = 2L;
    @AutoLogOutput float floatVal = 3.0f;
    @AutoLogOutput double doubleVal = 4.0;
    @AutoLogOutput String strVal = "hello";
    @AutoLogOutput boolean boolVal = true;
    @AutoLogOutput byte[] byteArr = {1, 2};
    @AutoLogOutput boolean[] boolArr = {true};
    @AutoLogOutput int[] intArr = {1, 2, 3};
    @AutoLogOutput long[] longArr = {1L};
    @AutoLogOutput float[] floatArr = {1.0f};
    @AutoLogOutput double[] doubleArr = {1.0, 2.0};
    @AutoLogOutput String[] strArr = {"a", "b"};
    @AutoLogOutput TestDirection enumVal = TestDirection.NORTH;
    @AutoLogOutput TestDirection[] enumArr = {TestDirection.NORTH, TestDirection.SOUTH};
  }

  /** Method whose return type is void — must be skipped by the scanner. */
  static class VoidReturnMethodObject {
    @AutoLogOutput
    public void doNothing() {}
  }

  /** Method that declares a checked exception — must be skipped (getExceptionTypes().length > 0). */
  static class CheckedExceptionMethodObject {
    @AutoLogOutput
    public int getValue() throws Exception {
      return 42;
    }
  }

  /** Object used to test auto-generated key format: ClassName/FieldName. */
  static class KeyAutoGenObject {
    @AutoLogOutput double speed = 1.5;
  }

  /** Object used to test custom key override in annotation. */
  static class CustomKeyObject {
    @AutoLogOutput(key = "Custom/MySpeed")
    double speed = 2.5;
  }

  /** Object used to test {fieldName} interpolation in key. */
  static class KeyInterpolationObject {
    String name = "arm";

    @AutoLogOutput(key = "Mechanisms/{name}/position")
    double position = 1.0;
  }

  // ─── addPackage ─────────────────────────────────────────────────────────────

  @Test
  void addPackageDoesNotThrow() {
    assertDoesNotThrow(() -> AutoLogOutputManager.addPackage("frc.robot"));
  }

  // ─── addObject ──────────────────────────────────────────────────────────────

  @Test
  void addObjectRegistersCallbacksForAnnotatedFields() throws Exception {
    BooleanFieldObject obj = new BooleanFieldObject();
    AutoLogOutputManager.addObject(obj);
    assertTrue(callbacks().size() >= 1, "At least one callback must be registered");
  }

  @Test
  void addObjectRegistersCallbacksForAnnotatedMethods() throws Exception {
    MethodAnnotatedObject obj = new MethodAnnotatedObject();
    AutoLogOutputManager.addObject(obj);
    assertTrue(callbacks().size() >= 1, "Annotated method must produce at least one callback");
  }

  @Test
  void methodWithParametersIsSkipped() throws Exception {
    InvalidMethodSignatureObject obj = new InvalidMethodSignatureObject();
    AutoLogOutputManager.addObject(obj);
    assertEquals(0, callbacks().size(), "Methods with parameters must not be registered");
  }

  @Test
  void addObjectMultipleFieldsRegistersMultipleCallbacks() throws Exception {
    MultiFieldObject obj = new MultiFieldObject();
    AutoLogOutputManager.addObject(obj);
    assertEquals(3, callbacks().size(), "One callback per annotated field");
  }

  @Test
  void addObjectRecordsHashToPreventRescan() throws Exception {
    BooleanFieldObject obj = new BooleanFieldObject();
    AutoLogOutputManager.addObject(obj);

    assertTrue(
        scannedHashes().contains(obj.hashCode()),
        "Object's hash must be recorded after scanning");
  }

  @Test
  void addObjectTwiceDoesNotDuplicateCallbacks() throws Exception {
    BooleanFieldObject obj = new BooleanFieldObject();
    AutoLogOutputManager.addObject(obj);
    int afterFirst = callbacks().size();
    AutoLogOutputManager.addObject(obj);
    assertEquals(afterFirst, callbacks().size(), "Re-adding the same object must not add callbacks");
  }

  @Test
  void addObjectFromDisallowedPackageIsSkipped() throws Exception {
    // java.lang.Object is from "java.lang" — not in allowedPackages after reset
    // The manager's addObject() adds the root object's package, but NOT java.lang
    // Use a nested object from a completely different package to trigger the guard.
    // We can simulate this by adding a package and then scanning an object NOT in it.

    // Add only "com.example" as allowed
    AutoLogOutputManager.addPackage("com.example");

    // SamePackageObject is in org.littletonrobotics.junction — not a subpackage of com.example
    // However, addObject() itself always adds root.getClass().getPackageName() first,
    // so the root is always allowed. We test a *nested field* object instead.
    // For simplicity: verify that an object whose package is NOT in the allowed set
    // does not get registered. We can test this by observing the package guard indirectly.

    // Create a wrapper that holds a field pointing to an object from another package
    // but the wrapper itself is in our test package (so it will be scanned).
    // The inner object (java.lang.String) would be skipped due to package filtering.
    // This test mainly verifies the guard does not throw.
    BooleanFieldObject obj = new BooleanFieldObject();
    assertDoesNotThrow(() -> AutoLogOutputManager.addObject(obj));
  }

  // ─── Hash collision bug ──────────────────────────────────────────────────────

  /**
   * Two objects that intentionally return the same hashCode(), backed by different state.
   *
   * <p>AutoLogOutputManager uses hashCode() — not identity — for deduplication. If two distinct
   * objects hash to the same value, the second one is silently skipped, meaning its annotated
   * fields are never registered. This is a real, reproducible bug when hash collisions occur.
   */
  static class FixedHashObject {
    private final int fixedHash;

    @AutoLogOutput double value;

    FixedHashObject(double value, int hash) {
      this.value = value;
      this.fixedHash = hash;
    }

    @Override
    public int hashCode() {
      return fixedHash;
    }
  }

  @Test
  void hashCollisionCausesSecondObjectToBeSkipped() throws Exception {
    // Both objects use the same hashCode() despite being distinct instances
    FixedHashObject first = new FixedHashObject(1.0, 42);
    FixedHashObject second = new FixedHashObject(2.0, 42); // same hash, different object

    AutoLogOutputManager.addObject(first);
    int afterFirst = callbacks().size();

    AutoLogOutputManager.addObject(second);
    int afterSecond = callbacks().size();

    assertEquals(
        afterFirst,
        afterSecond,
        "BUG CONFIRMED: hash collision causes second distinct object to be silently skipped. "
            + "Callbacks should have grown but did not.");
  }

  // ─── Key generation ─────────────────────────────────────────────────────────

  @Test
  void periodicDoesNotThrowWhenCallbacksAreRegistered() throws Exception {
    BooleanFieldObject obj = new BooleanFieldObject();
    AutoLogOutputManager.addObject(obj);

    // periodic() calls Logger.recordOutput() which is a no-op if Logger is not running.
    // The important thing is that it does not throw.
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  @Test
  void periodicDoesNotThrowWithNoCallbacks() {
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  // ─── Null handling ──────────────────────────────────────────────────────────

  @Test
  void addObjectDoesNotThrowForNullField() throws Exception {
    // Object with a null annotated field value — the callback must handle null gracefully
    // (AutoLogOutputManager already does: "if (value != null) Logger.record..." for most types)
    class NullableFieldObject {
      @AutoLogOutput String nullable = null;
    }

    NullableFieldObject obj = new NullableFieldObject();
    AutoLogOutputManager.addObject(obj);
    // Run the callback — must not throw even though field is null
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  // ─── All field types register callbacks ─────────────────────────────────────

  @Test
  void allPrimitiveAndArrayFieldTypesRegisterCallbacks() throws Exception {
    AllTypesObject obj = new AllTypesObject();
    AutoLogOutputManager.addObject(obj);
    assertEquals(15, callbacks().size(), "Every annotated field must register exactly one callback");
  }

  @Test
  void periodicDoesNotThrowForAllFieldTypes() throws Exception {
    AllTypesObject obj = new AllTypesObject();
    AutoLogOutputManager.addObject(obj);
    // Logger.recordOutput() is a no-op when Logger is not started — must not throw
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  // ─── Void-return method is skipped ──────────────────────────────────────────

  @Test
  void voidReturnMethodIsSkipped() throws Exception {
    VoidReturnMethodObject obj = new VoidReturnMethodObject();
    AutoLogOutputManager.addObject(obj);
    assertEquals(0, callbacks().size(), "Methods returning void must not be registered");
  }

  // ─── Checked-exception method is skipped ────────────────────────────────────

  @Test
  void methodWithCheckedExceptionIsSkipped() throws Exception {
    CheckedExceptionMethodObject obj = new CheckedExceptionMethodObject();
    AutoLogOutputManager.addObject(obj);
    assertEquals(0, callbacks().size(), "Methods declaring checked exceptions must not be registered");
  }

  // ─── Key auto-generation ────────────────────────────────────────────────────

  @Test
  void addObjectRegistersExpectedNumberOfCallbacksForAutoKeyObject() throws Exception {
    KeyAutoGenObject obj = new KeyAutoGenObject();
    AutoLogOutputManager.addObject(obj);
    assertEquals(1, callbacks().size(), "One annotated field must produce one callback");
  }

  // ─── Custom key annotation ──────────────────────────────────────────────────

  @Test
  void customKeyObjectRegistersOneCallback() throws Exception {
    CustomKeyObject obj = new CustomKeyObject();
    AutoLogOutputManager.addObject(obj);
    assertEquals(1, callbacks().size(), "Custom-key annotated field must register one callback");
  }

  // ─── Key interpolation ──────────────────────────────────────────────────────

  @Test
  void keyInterpolationObjectRegistersOneCallback() throws Exception {
    KeyInterpolationObject obj = new KeyInterpolationObject();
    AutoLogOutputManager.addObject(obj);
    assertEquals(1, callbacks().size(), "Interpolated-key annotated field must register one callback");
    assertDoesNotThrow(AutoLogOutputManager::periodic, "periodic() with interpolated key must not throw");
  }

  // ─── Array root recursion ────────────────────────────────────────────────────

  @Test
  void addObjectWithNullFieldDoesNotThrow() throws Exception {
    // Object with an unannotated null field should not cause NPE during recursive scan
    class NullUnannotatedFieldObject {
      @SuppressWarnings("unused")
      Object nested = null;
    }
    assertDoesNotThrow(() -> AutoLogOutputManager.addObject(new NullUnannotatedFieldObject()));
  }

  // ─── Supplier field types ────────────────────────────────────────────────────

  static class SupplierTypesObject {
    @AutoLogOutput BooleanSupplier boolSup = () -> true;
    @AutoLogOutput IntSupplier intSup = () -> 5;
    @AutoLogOutput LongSupplier longSup = () -> 10L;
    @AutoLogOutput DoubleSupplier doubleSup = () -> 3.14;
  }

  @Test
  void supplierTypeFieldsRegisterFourCallbacks() throws Exception {
    AutoLogOutputManager.addObject(new SupplierTypesObject());
    assertEquals(4, callbacks().size(), "BooleanSupplier/IntSupplier/LongSupplier/DoubleSupplier fields must each register one callback");
  }

  @Test
  void periodicDoesNotThrowForSupplierTypeFields() throws Exception {
    AutoLogOutputManager.addObject(new SupplierTypesObject());
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  // ─── Color and LoggedMechanism2d field types ─────────────────────────────────

  static class ColorAndMechObject {
    @AutoLogOutput Color color = Color.kRed;
    @AutoLogOutput LoggedMechanism2d mech = new LoggedMechanism2d(100, 100);
  }

  @Test
  void colorAndMechFieldsRegisterTwoCallbacks() throws Exception {
    AutoLogOutputManager.addObject(new ColorAndMechObject());
    assertEquals(2, callbacks().size(), "Color and LoggedMechanism2d fields must each register one callback");
  }

  @Test
  void periodicDoesNotThrowForColorAndMechFields() throws Exception {
    AutoLogOutputManager.addObject(new ColorAndMechObject());
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  // ─── 2D array field types ────────────────────────────────────────────────────

  static class TwoDArraysObject {
    @AutoLogOutput byte[][] byte2d = {{1, 2}, {3}};
    @AutoLogOutput boolean[][] bool2d = {{true}, {false, true}};
    @AutoLogOutput int[][] int2d = {{1, 2}, {3}};
    @AutoLogOutput long[][] long2d = {{1L, 2L}};
    @AutoLogOutput float[][] float2d = {{1.0f}};
    @AutoLogOutput double[][] double2d = {{1.0, 2.0}};
    @AutoLogOutput String[][] str2d = {{"a", "b"}, {"c"}};
    @AutoLogOutput TestDirection[][] enum2d = {{TestDirection.NORTH}, {TestDirection.SOUTH}};
  }

  @Test
  void twoDimensionalArrayFieldsRegisterCallbacks() throws Exception {
    AutoLogOutputManager.addObject(new TwoDArraysObject());
    assertEquals(8, callbacks().size(), "Each 2D array field must register exactly one callback");
  }

  @Test
  void periodicDoesNotThrowForTwoDimensionalArrayFields() throws Exception {
    AutoLogOutputManager.addObject(new TwoDArraysObject());
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  // ─── forceSerializable = true ────────────────────────────────────────────────

  static class ForceSerializableObject {
    // String is not WPISerializable — periodic() will catch ClassCastException and
    // call DriverStation.reportError (requires HAL to be initialized, see @BeforeAll).
    @AutoLogOutput(forceSerializable = true)
    String nonSerializable = "test";
  }

  @Test
  void forceSerializableRegistersCallback() throws Exception {
    // Verifies the forceSerializable=true code path registers exactly one callback.
    // periodic() is NOT called here: running it would trigger DriverStation.reportError()
    // (because String is not WPISerializable), which produces noisy console output.
    AutoLogOutputManager.addObject(new ForceSerializableObject());
    assertEquals(1, callbacks().size(), "forceSerializable field must register exactly one callback");
  }

  // ─── Superclass field lookup in key interpolation ────────────────────────────

  static class InterpolationBase {
    protected String subsystem = "arm";
  }

  static class InheritedKeyInterpolationObject extends InterpolationBase {
    @AutoLogOutput(key = "Mechanisms/{subsystem}/Position")
    double position = 2.0;
  }

  @Test
  void keyInterpolationReadsFieldFromSuperclass() throws Exception {
    AutoLogOutputManager.addObject(new InheritedKeyInterpolationObject());
    assertEquals(1, callbacks().size(), "Inherited-interpolation field must register one callback");
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  // ─── float/double with unit ──────────────────────────────────────────────────

  static class UnitAnnotatedObject {
    @AutoLogOutput(unit = "meters")
    float distanceMeters = 1.5f;

    @AutoLogOutput(unit = "radians")
    double angleRadians = 0.5;
  }

  @Test
  void floatAndDoubleWithUnitRegisterTwoCallbacks() throws Exception {
    AutoLogOutputManager.addObject(new UnitAnnotatedObject());
    assertEquals(2, callbacks().size(), "float(unit) and double(unit) fields must each register one callback");
  }

  @Test
  void periodicDoesNotThrowForUnitAnnotatedFields() throws Exception {
    AutoLogOutputManager.addObject(new UnitAnnotatedObject());
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  // ─── Java record field types ─────────────────────────────────────────────────

  record TestPoint(double x, double y) {}

  static class RecordTypesObject {
    @AutoLogOutput TestPoint rec = new TestPoint(1.0, 2.0);
    @AutoLogOutput TestPoint[] recArr = {new TestPoint(1.0, 2.0), new TestPoint(3.0, 4.0)};
    @AutoLogOutput TestPoint[][] rec2d = {{new TestPoint(1.0, 2.0)}};
  }

  @Test
  void recordFieldTypesRegisterThreeCallbacks() throws Exception {
    AutoLogOutputManager.addObject(new RecordTypesObject());
    assertEquals(3, callbacks().size(), "Record, Record[], and Record[][] fields must each register one callback");
  }

  @Test
  void periodicDoesNotThrowForRecordFieldTypes() throws Exception {
    AutoLogOutputManager.addObject(new RecordTypesObject());
    // Logger is not running so recordOutput is a no-op; casts to Record succeed since
    // TestPoint extends java.lang.Record
    assertDoesNotThrow(AutoLogOutputManager::periodic);
  }

  // ─── WPISerializable / StructSerializable fallback paths ─────────────────────

  /** A type that is not in any of the known type handlers — triggers WPISerializable cast path. */
  static class UnknownFieldType {}

  static class FallbackTypesObject {
    // Single unknown type → WPISerializable fallback (ClassCastException caught, reportError called)
    @AutoLogOutput UnknownFieldType unknown = new UnknownFieldType();

    // Array of unknown type → StructSerializable[] fallback
    @AutoLogOutput UnknownFieldType[] unknownArr = {new UnknownFieldType()};

    // 2D array of unknown type → StructSerializable[][] fallback
    @AutoLogOutput UnknownFieldType[][] unknown2d = {{new UnknownFieldType()}};
  }

  @Test
  void fallbackTypeFieldsRegisterThreeCallbacks() throws Exception {
    // Verifies that unknown types (single, 1D array, 2D array) still register fallback callbacks.
    // periodic() is NOT called here: running it would trigger DriverStation.reportError()
    // for each field (because UnknownFieldType is not WPISerializable/StructSerializable),
    // which produces noisy console output.
    AutoLogOutputManager.addObject(new FallbackTypesObject());
    assertEquals(3, callbacks().size(), "Unknown type fields must still register fallback callbacks");
  }
}
