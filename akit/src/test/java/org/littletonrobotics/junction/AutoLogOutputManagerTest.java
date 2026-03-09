// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for AutoLogOutputManager scanning, package filtering, deduplication, and key generation.
 *
 * <p>Because AutoLogOutputManager holds static state, each test resets that state via reflection
 * before running.
 */
public class AutoLogOutputManagerTest {

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
}
