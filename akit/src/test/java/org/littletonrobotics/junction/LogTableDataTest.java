// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.wpilibj.util.Color;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.LogTable.LogValue;
import org.littletonrobotics.junction.inputs.LoggableInputs;

/**
 * Comprehensive tests for LogTable primitive storage and retrieval.
 *
 * <p>Note: tests that trigger {@code DriverStation.reportWarning()} (i.e., writing a key with a
 * mismatched type) require HAL initialization which crashes the JVM on macOS in a test
 * environment. Those scenarios are tested via the <em>get</em>-side only: put the key once, then
 * read it back with the wrong default type to confirm the stored value is not corrupted.
 */
public class LogTableDataTest {

  private LogTable table;

  @BeforeEach
  void setUp() {
    table = new LogTable(0);
  }

  // ─── Boolean ────────────────────────────────────────────────────────────────

  @Test
  void booleanTrueRoundTrip() {
    table.put("key", true);
    assertTrue(table.get("key", false));
  }

  @Test
  void booleanFalseRoundTrip() {
    // Default is true so that a stored false is distinguishable from a missing key
    table.put("key", false);
    assertFalse(table.get("key", true));
  }

  @Test
  void booleanMissingKeyReturnsDefault() {
    assertTrue(table.get("missing", true));
    assertFalse(table.get("missing", false));
  }

  // ─── Integer (int) ──────────────────────────────────────────────────────────

  @Test
  void intRoundTrip() {
    table.put("key", 42);
    assertEquals(42, table.get("key", 0));
  }

  @Test
  void intNegativeRoundTrip() {
    table.put("key", -100);
    assertEquals(-100, table.get("key", 0));
  }

  @Test
  void intMaxValueRoundTrip() {
    table.put("key", Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, table.get("key", 0));
  }

  @Test
  void intMinValueRoundTrip() {
    table.put("key", Integer.MIN_VALUE);
    assertEquals(Integer.MIN_VALUE, table.get("key", 0));
  }

  @Test
  void intZeroRoundTrip() {
    // Non-zero default to distinguish stored 0 from missing key
    table.put("key", 0);
    assertEquals(0, table.get("key", 99));
  }

  @Test
  void intStoredAsLong() {
    // put(key, int) internally delegates to put(key, (long) value)
    table.put("key", (int) 5);
    assertEquals(5L, table.get("key", 0L));
    assertEquals(5, table.get("key", 0));
  }

  // ─── Long ───────────────────────────────────────────────────────────────────

  @Test
  void longRoundTrip() {
    table.put("key", 9876543210L);
    assertEquals(9876543210L, table.get("key", 0L));
  }

  @Test
  void longMaxValueRoundTrip() {
    table.put("key", Long.MAX_VALUE);
    assertEquals(Long.MAX_VALUE, table.get("key", 0L));
  }

  @Test
  void longMinValueRoundTrip() {
    table.put("key", Long.MIN_VALUE);
    assertEquals(Long.MIN_VALUE, table.get("key", 0L));
  }

  // ─── Float ──────────────────────────────────────────────────────────────────

  @Test
  void floatRoundTrip() {
    table.put("key", 3.14f);
    assertEquals(3.14f, table.get("key", 0.0f));
  }

  @Test
  void floatNaNRoundTrip() {
    table.put("key", Float.NaN);
    assertTrue(Float.isNaN(table.get("key", 0.0f)));
  }

  @Test
  void floatPositiveInfinityRoundTrip() {
    table.put("key", Float.POSITIVE_INFINITY);
    assertEquals(Float.POSITIVE_INFINITY, table.get("key", 0.0f));
  }

  @Test
  void floatNegativeInfinityRoundTrip() {
    table.put("key", Float.NEGATIVE_INFINITY);
    assertEquals(Float.NEGATIVE_INFINITY, table.get("key", 0.0f));
  }

  @Test
  void floatMaxValueRoundTrip() {
    table.put("key", Float.MAX_VALUE);
    assertEquals(Float.MAX_VALUE, table.get("key", 0.0f));
  }

  // ─── Double ─────────────────────────────────────────────────────────────────

  @Test
  void doubleRoundTrip() {
    table.put("key", Math.PI);
    assertEquals(Math.PI, table.get("key", 0.0));
  }

  @Test
  void doubleNaNRoundTrip() {
    table.put("key", Double.NaN);
    assertTrue(Double.isNaN(table.get("key", 0.0)));
  }

  @Test
  void doublePositiveInfinityRoundTrip() {
    table.put("key", Double.POSITIVE_INFINITY);
    assertEquals(Double.POSITIVE_INFINITY, table.get("key", 0.0));
  }

  @Test
  void doubleNegativeInfinityRoundTrip() {
    table.put("key", Double.NEGATIVE_INFINITY);
    assertEquals(Double.NEGATIVE_INFINITY, table.get("key", 0.0));
  }

  @Test
  void doubleMaxValueRoundTrip() {
    table.put("key", Double.MAX_VALUE);
    assertEquals(Double.MAX_VALUE, table.get("key", 0.0));
  }

  @Test
  void doubleMissingKeyReturnsDefault() {
    assertEquals(99.0, table.get("missing", 99.0));
  }

  // ─── Byte array ─────────────────────────────────────────────────────────────

  @Test
  void byteArrayRoundTrip() {
    byte[] value = {1, 2, 3, (byte) 255, 0};
    table.put("key", value);
    assertArrayEquals(value, table.get("key", new byte[0]));
  }

  @Test
  void emptyByteArrayRoundTrip() {
    table.put("key", new byte[0]);
    // Non-empty default so we can confirm the stored empty array was returned
    assertArrayEquals(new byte[0], table.get("key", new byte[] {99}));
  }

  @Test
  void nullByteArrayIsIgnored() {
    table.put("key", (byte[]) null);
    assertArrayEquals(new byte[] {99}, table.get("key", new byte[] {99}));
  }

  @Test
  void byteArrayInputMutationDoesNotAffectTable() {
    // put() clones the input array; mutating the original must not affect stored data
    byte[] original = {10, 20, 30};
    table.put("key", original);
    original[0] = 99;
    assertArrayEquals(new byte[] {10, 20, 30}, table.get("key", new byte[0]));
  }

  @Test
  void byteArrayReturnedValueMutationDoesNotAffectStoredData() {
    // get(byte[]) must return a defensive copy so external mutation cannot corrupt the table.
    byte[] input = {1, 2, 3};
    table.put("key", input);
    byte[] returned = table.get("key", new byte[0]);
    returned[0] = 99; // mutate the returned copy

    byte[] secondRead = table.get("key", new byte[0]);
    assertEquals(
        1, secondRead[0], "get(byte[]) must return a defensive copy; mutation must not persist");
  }

  // ─── Boolean array ──────────────────────────────────────────────────────────

  @Test
  void booleanArrayRoundTrip() {
    boolean[] value = {true, false, true, true, false};
    table.put("key", value);
    assertArrayEquals(value, table.get("key", new boolean[0]));
  }

  @Test
  void emptyBooleanArrayRoundTrip() {
    table.put("key", new boolean[0]);
    assertArrayEquals(new boolean[0], table.get("key", new boolean[] {true}));
  }

  @Test
  void nullBooleanArrayIsIgnored() {
    table.put("key", (boolean[]) null);
    assertArrayEquals(new boolean[] {true}, table.get("key", new boolean[] {true}));
  }

  @Test
  void booleanArrayInputMutationDoesNotAffectTable() {
    boolean[] original = {true, false, true};
    table.put("key", original);
    original[0] = false;
    assertArrayEquals(new boolean[] {true, false, true}, table.get("key", new boolean[0]));
  }

  // ─── Int array ──────────────────────────────────────────────────────────────

  @Test
  void intArrayRoundTrip() {
    int[] value = {1, -2, 3, Integer.MAX_VALUE, Integer.MIN_VALUE};
    table.put("key", value);
    assertArrayEquals(value, table.get("key", new int[0]));
  }

  @Test
  void emptyIntArrayRoundTrip() {
    table.put("key", new int[0]);
    assertArrayEquals(new int[0], table.get("key", new int[] {99}));
  }

  // ─── Long array ─────────────────────────────────────────────────────────────

  @Test
  void longArrayRoundTrip() {
    long[] value = {1L, -2L, Long.MAX_VALUE, Long.MIN_VALUE};
    table.put("key", value);
    assertArrayEquals(value, table.get("key", new long[0]));
  }

  @Test
  void longArrayInputMutationDoesNotAffectTable() {
    long[] original = {10L, 20L, 30L};
    table.put("key", original);
    original[0] = 99L;
    assertArrayEquals(new long[] {10L, 20L, 30L}, table.get("key", new long[0]));
  }

  // ─── Float array ────────────────────────────────────────────────────────────

  @Test
  void floatArrayRoundTrip() {
    float[] value = {1.0f, -2.5f, Float.POSITIVE_INFINITY};
    table.put("key", value);
    assertArrayEquals(value, table.get("key", new float[0]));
  }

  @Test
  void floatArrayWithNaNRoundTrip() {
    table.put("key", new float[] {Float.NaN});
    float[] result = table.get("key", new float[0]);
    assertEquals(1, result.length);
    assertTrue(Float.isNaN(result[0]));
  }

  // ─── Double array ───────────────────────────────────────────────────────────

  @Test
  void doubleArrayRoundTrip() {
    double[] value = {1.0, -2.5, Double.POSITIVE_INFINITY, Math.PI};
    table.put("key", value);
    assertArrayEquals(value, table.get("key", new double[0]));
  }

  @Test
  void doubleArrayWithNaNRoundTrip() {
    table.put("key", new double[] {Double.NaN});
    double[] result = table.get("key", new double[0]);
    assertEquals(1, result.length);
    assertTrue(Double.isNaN(result[0]));
  }

  @Test
  void emptyDoubleArrayRoundTrip() {
    table.put("key", new double[0]);
    assertArrayEquals(new double[0], table.get("key", new double[] {99.0}));
  }

  @Test
  void nullDoubleArrayIsIgnored() {
    table.put("key", (double[]) null);
    assertArrayEquals(new double[] {99.0}, table.get("key", new double[] {99.0}));
  }

  // ─── String ─────────────────────────────────────────────────────────────────

  @Test
  void stringRoundTrip() {
    table.put("key", "hello world");
    assertEquals("hello world", table.get("key", ""));
  }

  @Test
  void emptyStringRoundTrip() {
    table.put("key", "");
    // Non-empty default to distinguish stored "" from missing key
    assertEquals("", table.get("key", "default"));
  }

  @Test
  void nullStringIsIgnored() {
    table.put("key", (String) null);
    assertEquals("default", table.get("key", "default"));
  }

  @Test
  void stringWithSpecialCharacters() {
    String special = "hello\nworld\ttab\u0000null";
    table.put("key", special);
    assertEquals(special, table.get("key", ""));
  }

  // ─── String array ───────────────────────────────────────────────────────────

  @Test
  void stringArrayRoundTrip() {
    String[] value = {"alpha", "beta", "gamma"};
    table.put("key", value);
    assertArrayEquals(value, table.get("key", new String[0]));
  }

  @Test
  void emptyStringArrayRoundTrip() {
    table.put("key", new String[0]);
    assertArrayEquals(new String[0], table.get("key", new String[] {"default"}));
  }

  @Test
  void nullStringArrayIsIgnored() {
    table.put("key", (String[]) null);
    assertArrayEquals(new String[] {"default"}, table.get("key", new String[] {"default"}));
  }

  @Test
  void stringArrayInputMutationDoesNotAffectTable() {
    String[] original = {"a", "b", "c"};
    table.put("key", original);
    original[0] = "MUTATED";
    assertArrayEquals(new String[] {"a", "b", "c"}, table.get("key", new String[0]));
  }

  // ─── Enum ───────────────────────────────────────────────────────────────────

  private enum TestEnum {
    FIRST,
    SECOND,
    THIRD
  }

  @Test
  void enumRoundTrip() {
    table.put("key", TestEnum.SECOND);
    assertEquals(TestEnum.SECOND, table.get("key", TestEnum.FIRST));
  }

  @Test
  void enumDefaultReturnedForMissingKey() {
    assertEquals(TestEnum.THIRD, table.get("missing", TestEnum.THIRD));
  }

  @Test
  void enumArrayRoundTrip() {
    TestEnum[] value = {TestEnum.FIRST, TestEnum.THIRD, TestEnum.SECOND};
    table.put("key", value);
    assertArrayEquals(value, table.get("key", new TestEnum[0]));
  }

  // ─── Color ──────────────────────────────────────────────────────────────────

  @Test
  void colorRoundTrip() {
    table.put("key", Color.kRed);
    Color result = table.get("key", Color.kBlue);
    assertEquals(Color.kRed.toHexString(), result.toHexString());
  }

  @Test
  void colorMissingKeyReturnsDefault() {
    Color result = table.get("missing", Color.kBlue);
    assertEquals(Color.kBlue.toHexString(), result.toHexString());
  }

  // ─── 2D arrays ──────────────────────────────────────────────────────────────

  @Test
  void byte2DArrayRoundTripWithJaggedRows() {
    byte[][] value = {{1, 2, 3}, {4, 5}, {6, 7, 8, 9}};
    table.put("key", value);
    byte[][] result = table.get("key", new byte[0][]);
    assertEquals(3, result.length);
    assertArrayEquals(new byte[] {1, 2, 3}, result[0]);
    assertArrayEquals(new byte[] {4, 5}, result[1]);
    assertArrayEquals(new byte[] {6, 7, 8, 9}, result[2]);
  }

  @Test
  void double2DArrayRoundTrip() {
    double[][] value = {{1.0, 2.0}, {3.0, 4.0, 5.0}};
    table.put("key", value);
    double[][] result = table.get("key", new double[0][]);
    assertEquals(2, result.length);
    assertArrayEquals(new double[] {1.0, 2.0}, result[0]);
    assertArrayEquals(new double[] {3.0, 4.0, 5.0}, result[1]);
  }

  @Test
  void string2DArrayRoundTrip() {
    String[][] value = {{"a", "b"}, {"c"}};
    table.put("key", value);
    String[][] result = table.get("key", new String[0][]);
    assertEquals(2, result.length);
    assertArrayEquals(new String[] {"a", "b"}, result[0]);
    assertArrayEquals(new String[] {"c"}, result[1]);
  }

  @Test
  void boolean2DArrayRoundTrip() {
    boolean[][] value = {{true, false}, {false, true, true}};
    table.put("key", value);
    boolean[][] result = table.get("key", new boolean[0][]);
    assertEquals(2, result.length);
    assertArrayEquals(new boolean[] {true, false}, result[0]);
    assertArrayEquals(new boolean[] {false, true, true}, result[1]);
  }

  @Test
  void long2DArrayRoundTrip() {
    long[][] value = {{1L, 2L}, {Long.MAX_VALUE}};
    table.put("key", value);
    long[][] result = table.get("key", new long[0][]);
    assertEquals(2, result.length);
    assertArrayEquals(new long[] {1L, 2L}, result[0]);
    assertArrayEquals(new long[] {Long.MAX_VALUE}, result[1]);
  }

  // ─── Type mismatch (get-side) ────────────────────────────────────────────────
  //
  // Putting a key that already exists with a DIFFERENT type triggers
  // DriverStation.reportWarning(), which requires HAL native init and crashes
  // the JVM on macOS in a pure-Java test environment.
  //
  // We instead test the get-side: once a key is stored as type T, reading it
  // back via get(key, defaultValue) with a different type must return the
  // caller's default — the stored value is unchanged and the incorrect read
  // returns a safe sentinel.

  @Test
  void typeMismatchReadBooleanKeyAsDoubleReturnsDefault() {
    table.put("key", true);
    // LogValue.getDouble() returns the default when the stored type is Boolean
    assertEquals(
        0.0, table.get("key", 0.0), "Reading a Boolean key as double must return the double default");
  }

  @Test
  void typeMismatchReadLongKeyAsStringReturnsDefault() {
    table.put("key", 42L);
    assertEquals(
        "sentinel",
        table.get("key", "sentinel"),
        "Reading a Long key as String must return the String default");
  }

  @Test
  void typeMismatchReadStringKeyAsBooleanReturnsDefault() {
    table.put("key", "hello");
    assertFalse(
        table.get("key", false),
        "Reading a String key as boolean must return the boolean default (false)");
  }

  @Test
  void typeMismatchReadDoubleKeyAsLongReturnsDefault() {
    table.put("key", Math.PI);
    assertEquals(
        -1L, table.get("key", -1L), "Reading a Double key as long must return the long default");
  }

  // ─── Timestamp ──────────────────────────────────────────────────────────────

  @Test
  void timestampInitialValue() {
    LogTable t = new LogTable(12345L);
    assertEquals(12345L, t.getTimestamp());
  }

  @Test
  void setTimestampUpdatesValue() {
    table.setTimestamp(99999L);
    assertEquals(99999L, table.getTimestamp());
  }

  @Test
  void subtableSharesTimestampWithParent() {
    LogTable subtable = table.getSubtable("sub");
    table.setTimestamp(777L);
    assertEquals(777L, subtable.getTimestamp());
  }

  @Test
  void subtableTimestampChangeAffectsParent() {
    LogTable subtable = table.getSubtable("sub");
    subtable.setTimestamp(888L);
    assertEquals(888L, table.getTimestamp());
  }

  // ─── Clone ──────────────────────────────────────────────────────────────────

  @Test
  void cloneContainsSameData() {
    table.put("key", 42.0);
    LogTable cloned = LogTable.clone(table);
    assertEquals(42.0, cloned.get("key", 0.0));
  }

  @Test
  void cloneIsIndependentOfOriginalPuts() {
    table.put("key", 1.0);
    LogTable cloned = LogTable.clone(table);
    table.put("newkey", 99.0); // Added after clone
    assertEquals(0.0, cloned.get("newkey", 0.0), "Clone must not reflect post-clone puts");
  }

  @Test
  void originalIsIndependentOfClonePuts() {
    table.put("key", 1.0);
    LogTable cloned = LogTable.clone(table);
    cloned.put("cloneonly", 2.0);
    assertEquals(0.0, table.get("cloneonly", 0.0), "Original must not see clone's new data");
  }

  @Test
  void cloneHasIndependentTimestamp() {
    table.setTimestamp(100L);
    LogTable cloned = LogTable.clone(table);
    table.setTimestamp(200L);
    assertEquals(100L, cloned.getTimestamp(), "Clone must have an independent timestamp");
  }

  // ─── Subtable ───────────────────────────────────────────────────────────────

  @Test
  void subtableKeyIsScopedToPrefix() {
    LogTable subtable = table.getSubtable("sub");
    subtable.put("key", true);

    // Readable with the local key on the subtable
    assertTrue(subtable.get("key", false));
    // Not readable from the root with the same local key
    assertFalse(table.get("key", false));
  }

  @Test
  void subtableDataVisibleInParentFullMap() {
    LogTable subtable = table.getSubtable("sub");
    subtable.put("val", 123L);

    Map<String, LogValue> all = table.getAll(false);
    assertTrue(all.containsKey("/sub/val"));
  }

  @Test
  void getAllSubtableOnlyFiltersToSubtablePrefix() {
    LogTable subtable = table.getSubtable("sub");
    table.put("rootKey", 1.0);
    subtable.put("subKey", 2.0);

    Map<String, LogValue> subtableData = subtable.getAll(true);
    assertTrue(subtableData.containsKey("subKey"));
    assertFalse(subtableData.containsKey("rootKey"));
    // Full path must not appear in the filtered result
    assertFalse(subtableData.containsKey("/sub/subKey"));
  }

  @Test
  void getAllFalseReturnsEverything() {
    LogTable subtable = table.getSubtable("sub");
    table.put("rootKey", 1.0);
    subtable.put("subKey", 2.0);

    Map<String, LogValue> all = table.getAll(false);
    assertTrue(all.containsKey("/rootKey"));
    assertTrue(all.containsKey("/sub/subKey"));
  }

  @Test
  void nestedSubtableKeyIsCorrectlyPrefixed() {
    LogTable a = table.getSubtable("a");
    LogTable b = a.getSubtable("b");
    b.put("key", 42.0);

    Map<String, LogValue> all = table.getAll(false);
    assertTrue(all.containsKey("/a/b/key"));
  }

  @Test
  void twoSubtablesWithSameNameShareData() {
    // Two calls to getSubtable with the same name produce views over the same underlying map
    LogTable sub1 = table.getSubtable("shared");
    LogTable sub2 = table.getSubtable("shared");
    sub1.put("val", 10L);
    assertEquals(10L, sub2.get("val", 0L));
  }

  // ─── LoggableInputs ─────────────────────────────────────────────────────────

  private static class TestInputs implements LoggableInputs {
    double value = 0.0;
    boolean flag = false;

    @Override
    public void toLog(LogTable t) {
      t.put("Value", value);
      t.put("Flag", flag);
    }

    @Override
    public void fromLog(LogTable t) {
      value = t.get("Value", 0.0);
      flag = t.get("Flag", false);
    }
  }

  @Test
  void loggableInputsRoundTrip() {
    TestInputs inputs = new TestInputs();
    inputs.value = 3.14;
    inputs.flag = true;

    table.put("Inputs", inputs);

    TestInputs restored = new TestInputs();
    table.get("Inputs", restored);

    assertEquals(3.14, restored.value, 1e-9);
    assertTrue(restored.flag);
  }

  @Test
  void loggableInputsFromLogOnMissingKeyLeavesDefaultValues() {
    // fromLog is always called (there is no "missing key" guard in get(key, LoggableInputs))
    TestInputs defaults = new TestInputs();
    defaults.value = 99.0;
    defaults.flag = true;

    table.get("Missing", defaults);

    // fromLog will read missing sub-keys and return defaults from the subtable
    assertEquals(0.0, defaults.value);
    assertFalse(defaults.flag);
  }
}
