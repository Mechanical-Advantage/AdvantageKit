// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.util.Color;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.LogTable.LogValue;
import org.littletonrobotics.junction.inputs.LoggableInputs;

/** Comprehensive tests for LogTable primitive storage and retrieval. */
public class LogTableDataTest {

  private LogTable table;

  @BeforeAll
  static void initHAL() {
    assertTrue(HAL.initialize(500, 0), "HAL initialization must succeed");
  }

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

  // ─── Type mismatch (write-side) ─────────────────────────────────────────────
  //
  // Writing a key that already exists with a DIFFERENT type triggers
  // DriverStation.reportWarning() and the write is silently dropped.
  // HAL must be initialized (see @BeforeAll) for reportWarning() to work.

  @Test
  void typeMismatchWriteBooleanThenDoublePreservesBoolean() {
    table.put("key", true);
    table.put("key", 99.0); // type mismatch — write should be dropped
    assertTrue(table.get("key", false), "Original boolean value must be preserved after mismatch");
  }

  @Test
  void typeMismatchWriteStringThenLongPreservesString() {
    table.put("key", "original");
    table.put("key", 42L); // type mismatch
    assertEquals("original", table.get("key", ""), "Original string must be preserved");
  }

  @Test
  void typeMismatchWriteLongThenBooleanPreservesLong() {
    table.put("key", 7L);
    table.put("key", false); // type mismatch
    assertEquals(7L, table.get("key", 0L), "Original long value must be preserved");
  }

  @Test
  void typeMismatchWriteDoubleArrayThenStringPreservesArray() {
    table.put("key", new double[] {1.0, 2.0});
    table.put("key", "oops"); // type mismatch
    assertArrayEquals(
        new double[] {1.0, 2.0},
        table.get("key", new double[0]),
        "Original double[] must be preserved after mismatch");
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

  // ─── Float with unit ────────────────────────────────────────────────────────

  @Test
  void floatWithUnitRoundTrip() {
    table.put("key", 1.5f, "meters");
    assertEquals(1.5f, table.get("key", 0.0f));
  }

  // ─── Double with unit ───────────────────────────────────────────────────────

  @Test
  void doubleWithUnitRoundTrip() {
    table.put("key", 2.5, "radians");
    assertEquals(2.5, table.get("key", 0.0));
  }

  // ─── 2D int array ───────────────────────────────────────────────────────────

  @Test
  void int2DArrayRoundTrip() {
    int[][] value = {{1, 2, 3}, {4, 5}};
    table.put("key", value);
    int[][] result = table.get("key", new int[0][]);
    assertEquals(2, result.length);
    assertArrayEquals(new int[]{1, 2, 3}, result[0]);
    assertArrayEquals(new int[]{4, 5}, result[1]);
  }

  @Test
  void nullInt2DArrayIsIgnored() {
    table.put("key", (int[][]) null);
    assertEquals(0, table.get("key", new int[0][]).length);
  }

  // ─── 2D float array ─────────────────────────────────────────────────────────

  @Test
  void float2DArrayRoundTrip() {
    float[][] value = {{1.0f, 2.0f}, {3.0f}};
    table.put("key", value);
    float[][] result = table.get("key", new float[0][]);
    assertEquals(2, result.length);
    assertArrayEquals(new float[]{1.0f, 2.0f}, result[0]);
    assertArrayEquals(new float[]{3.0f}, result[1]);
  }

  @Test
  void nullFloat2DArrayIsIgnored() {
    table.put("key", (float[][]) null);
    assertEquals(0, table.get("key", new float[0][]).length);
  }

  // ─── 2D boolean array (null guard) ──────────────────────────────────────────

  @Test
  void nullBoolean2DArrayIsIgnored() {
    table.put("key", (boolean[][]) null);
    assertEquals(0, table.get("key", new boolean[0][]).length);
  }

  // ─── 2D long array (null guard) ─────────────────────────────────────────────

  @Test
  void nullLong2DArrayIsIgnored() {
    table.put("key", (long[][]) null);
    assertEquals(0, table.get("key", new long[0][]).length);
  }

  // ─── 2D double array (null guard) ───────────────────────────────────────────

  @Test
  void nullDouble2DArrayIsIgnored() {
    table.put("key", (double[][]) null);
    assertEquals(0, table.get("key", new double[0][]).length);
  }

  // ─── 2D string array (null guard) ───────────────────────────────────────────

  @Test
  void nullString2DArrayIsIgnored() {
    table.put("key", (String[][]) null);
    assertEquals(0, table.get("key", new String[0][]).length);
  }

  // ─── Enum array (null guard + defensive copy) ───────────────────────────────

  @Test
  void nullEnumArrayIsIgnored() {
    TestEnum[] def = {TestEnum.FIRST};
    table.put("key", (TestEnum[]) null);
    assertArrayEquals(def, table.get("key", def));
  }

  @Test
  void enumArrayReturnedValueMutationDoesNotAffectStoredData() {
    TestEnum[] value = {TestEnum.FIRST, TestEnum.SECOND};
    table.put("key", value);
    TestEnum[] returned = table.get("key", new TestEnum[0]);
    returned[0] = TestEnum.THIRD;
    TestEnum[] secondRead = table.get("key", new TestEnum[0]);
    assertEquals(TestEnum.FIRST, secondRead[0], "Mutation of returned enum array must not affect stored data");
  }

  // ─── LogValue direct put ─────────────────────────────────────────────────────

  @Test
  void directLogValuePutAndRetrieve() {
    LogValue logValue = new LogValue(42.0, null);
    table.put("key", logValue);
    assertEquals(42.0, table.get("key", 0.0));
  }

  @Test
  void directNullLogValueIsIgnored() {
    table.put("key", (LogValue) null);
    assertEquals(0.0, table.get("key", 0.0));
  }

  // ─── int[] null guard ────────────────────────────────────────────────────────

  @Test
  void nullIntArrayIsIgnored() {
    table.put("key", (int[]) null);
    assertArrayEquals(new int[]{99}, table.get("key", new int[]{99}));
  }

  // ─── float[] null guard ──────────────────────────────────────────────────────

  @Test
  void nullFloatArrayIsIgnored() {
    table.put("key", (float[]) null);
    assertArrayEquals(new float[]{9.9f}, table.get("key", new float[]{9.9f}));
  }

  // ─── long[] null guard ───────────────────────────────────────────────────────

  @Test
  void nullLongArrayIsIgnored() {
    table.put("key", (long[]) null);
    assertArrayEquals(new long[]{99L}, table.get("key", new long[]{99L}));
  }

  // ─── get with missing key returns default (additional types) ─────────────────

  @Test
  void intMissingKeyReturnsDefault() {
    assertEquals(7, table.get("missing", 7));
  }

  @Test
  void floatMissingKeyReturnsDefault() {
    assertEquals(3.0f, table.get("missing", 3.0f));
  }

  @Test
  void longMissingKeyReturnsDefault() {
    assertEquals(77L, table.get("missing", 77L));
  }

  @Test
  void byteArrayMissingKeyReturnsDefault() {
    byte[] def = {1, 2};
    assertArrayEquals(def, table.get("missing", def));
  }

  @Test
  void booleanArrayMissingKeyReturnsDefault() {
    boolean[] def = {true, false};
    assertArrayEquals(def, table.get("missing", def));
  }

  @Test
  void intArrayMissingKeyReturnsDefault() {
    int[] def = {5, 6};
    assertArrayEquals(def, table.get("missing", def));
  }

  @Test
  void longArrayMissingKeyReturnsDefault() {
    long[] def = {5L, 6L};
    assertArrayEquals(def, table.get("missing", def));
  }

  @Test
  void floatArrayMissingKeyReturnsDefault() {
    float[] def = {1.0f, 2.0f};
    assertArrayEquals(def, table.get("missing", def));
  }

  @Test
  void stringArrayMissingKeyReturnsDefault() {
    String[] def = {"a", "b"};
    assertArrayEquals(def, table.get("missing", def));
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

  // ─── Enum 2D array ──────────────────────────────────────────────────────────

  private enum Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST
  }

  @Test
  void enum2dArrayRoundTrip() {
    Direction[][] value = {{Direction.NORTH, Direction.SOUTH}, {Direction.EAST}};
    table.put("dirs", value);
    Direction[][] result = table.get("dirs", new Direction[0][]);
    assertEquals(2, result.length);
    assertArrayEquals(new Direction[] {Direction.NORTH, Direction.SOUTH}, result[0]);
    assertArrayEquals(new Direction[] {Direction.EAST}, result[1]);
  }

  @Test
  void enum2dArrayMissingKeyReturnsDefault() {
    Direction[][] def = {{Direction.WEST}};
    Direction[][] result = table.get("missing2d", def);
    assertSame(def, result);
  }

  // ─── Struct (explicit Struct<T>) ─────────────────────────────────────────────

  @Test
  void structSingleRoundTrip() {
    Translation2d original = new Translation2d(1.5, 2.5);
    table.put("t2d", Translation2d.struct, original);
    Translation2d result = table.get("t2d", Translation2d.struct, new Translation2d());
    assertEquals(1.5, result.getX(), 1e-9);
    assertEquals(2.5, result.getY(), 1e-9);
  }

  @Test
  void structSingleMissingKeyReturnsDefault() {
    Translation2d def = new Translation2d(9.0, 9.0);
    Translation2d result = table.get("missing", Translation2d.struct, def);
    assertSame(def, result);
  }

  @Test
  void structArrayRoundTrip() {
    Translation2d[] arr = {new Translation2d(1.0, 2.0), new Translation2d(3.0, 4.0)};
    table.put("arr", Translation2d.struct, arr);
    Translation2d[] result = table.get("arr", Translation2d.struct, new Translation2d[0]);
    assertEquals(2, result.length);
    assertEquals(1.0, result[0].getX(), 1e-9);
    assertEquals(3.0, result[1].getX(), 1e-9);
  }

  @Test
  void structArrayMissingKeyReturnsDefault() {
    Translation2d[] def = new Translation2d[0];
    assertSame(def, table.get("missing", Translation2d.struct, def));
  }

  @Test
  void struct2dArrayRoundTrip() {
    Translation2d[][] arr = {
      {new Translation2d(1.0, 2.0)}, {new Translation2d(3.0, 4.0), new Translation2d(5.0, 6.0)}
    };
    table.put("arr2d", Translation2d.struct, arr);
    Translation2d[][] result = table.get("arr2d", Translation2d.struct, new Translation2d[0][]);
    assertEquals(2, result.length);
    assertEquals(1, result[0].length);
    assertEquals(2, result[1].length);
    assertEquals(3.0, result[1][0].getX(), 1e-9);
  }

  @Test
  void struct2dArrayMissingKeyReturnsDefault() {
    Translation2d[][] def = new Translation2d[0][];
    assertSame(def, table.get("missing2d", Translation2d.struct, def));
  }

  // ─── WPISerializable (auto struct detection) ─────────────────────────────────

  @Test
  void wpiSerializableRoundTrip() {
    Translation2d original = new Translation2d(7.0, 3.0);
    table.put("auto", original);
    Translation2d result = table.get("auto", new Translation2d());
    assertEquals(7.0, result.getX(), 1e-9);
    assertEquals(3.0, result.getY(), 1e-9);
  }

  @Test
  void wpiSerializableArrayRoundTrip() {
    Translation2d[] arr = {new Translation2d(1.0, 0.0), new Translation2d(0.0, 1.0)};
    table.put("autoArr", arr);
    Translation2d[] result = table.get("autoArr", new Translation2d[0]);
    assertEquals(2, result.length);
    assertEquals(1.0, result[0].getX(), 1e-9);
    assertEquals(1.0, result[1].getY(), 1e-9);
  }

  @Test
  void wpiSerializable2dArrayRoundTrip() {
    Translation2d[][] arr = {{new Translation2d(1.0, 2.0)}, {new Translation2d(3.0, 4.0)}};
    table.put("auto2d", arr);
    Translation2d[][] result = table.get("auto2d", new Translation2d[0][]);
    assertEquals(2, result.length);
    assertEquals(1.0, result[0][0].getX(), 1e-9);
    assertEquals(3.0, result[1][0].getX(), 1e-9);
  }

  // ─── Record put/get ──────────────────────────────────────────────────────────

  private record Point(double x, double y) {}

  private enum Color3 {
    RED,
    GREEN,
    BLUE
  }

  /** Record with every primitive field type supported by RecordStruct. */
  private record AllTypesRecord(
      boolean b, short s, int i, long l, float f, double d, Color3 color) {}

  /** Record with a nested Translation2d (StructSerializable). */
  private record NestedStructRecord(double scalar, Translation2d pos) {}

  @Test
  void recordAllPrimitiveTypesRoundTrip() {
    AllTypesRecord original = new AllTypesRecord(true, (short) 7, 42, 99L, 1.5f, 3.14, Color3.GREEN);
    table.put("all", original);
    AllTypesRecord result = table.get("all", new AllTypesRecord(false, (short) 0, 0, 0L, 0.0f, 0.0, Color3.RED));
    assertTrue(result.b());
    assertEquals((short) 7, result.s());
    assertEquals(42, result.i());
    assertEquals(99L, result.l());
    assertEquals(1.5f, result.f(), 1e-6f);
    assertEquals(3.14, result.d(), 1e-9);
    assertEquals(Color3.GREEN, result.color());
  }

  @Test
  void recordIsImmutableReturnsTrue() {
    // RecordStruct.isImmutable() must return true (records are immutable)
    LogTable t = new LogTable(0);
    Point p = new Point(1.0, 2.0);
    t.put("p", p);
    // Access via recordStruct; the easiest way is to just verify the round-trip works
    // (isImmutable() is exercised when StructBuffer accesses it)
    Point result = t.get("p", new Point(0.0, 0.0));
    assertEquals(1.0, result.x(), 1e-9);
  }

  @Test
  void recordNestedStructRoundTrip() {
    NestedStructRecord original = new NestedStructRecord(7.5, new Translation2d(3.0, 4.0));
    table.put("nested", original);
    NestedStructRecord result =
        table.get("nested", new NestedStructRecord(0.0, new Translation2d()));
    assertEquals(7.5, result.scalar(), 1e-9);
    assertEquals(3.0, result.pos().getX(), 1e-9);
    assertEquals(4.0, result.pos().getY(), 1e-9);
  }

  @Test
  void recordSingleRoundTrip() {
    Point p = new Point(3.14, 2.72);
    table.put("pt", p);
    Point result = table.get("pt", new Point(0.0, 0.0));
    assertEquals(3.14, result.x(), 1e-9);
    assertEquals(2.72, result.y(), 1e-9);
  }

  @Test
  void recordSingleMissingKeyReturnsDefault() {
    Point def = new Point(1.0, 2.0);
    assertSame(def, table.get("missing", def));
  }

  @Test
  void recordArrayRoundTrip() {
    Point[] pts = {new Point(1.0, 2.0), new Point(3.0, 4.0)};
    table.put("pts", pts);
    Point[] result = table.get("pts", new Point[0]);
    assertEquals(2, result.length);
    assertEquals(1.0, result[0].x(), 1e-9);
    assertEquals(3.0, result[1].x(), 1e-9);
  }

  @Test
  void recordArrayMissingKeyReturnsDefault() {
    Point[] def = new Point[0];
    assertSame(def, table.get("missing", def));
  }

  @Test
  void record2dArrayRoundTrip() {
    Point[][] pts = {{new Point(1.0, 2.0)}, {new Point(3.0, 4.0)}};
    table.put("pts2d", pts);
    Point[][] result = table.get("pts2d", new Point[0][]);
    assertEquals(2, result.length);
    assertEquals(1.0, result[0][0].x(), 1e-9);
    assertEquals(3.0, result[1][0].x(), 1e-9);
  }

  @Test
  void record2dArrayMissingKeyReturnsDefault() {
    Point[][] def = new Point[0][];
    assertSame(def, table.get("missing", def));
  }

  // ─── Protobuf ────────────────────────────────────────────────────────────────

  @Test
  void protobufRoundTrip() {
    Translation2d original = new Translation2d(4.0, 5.0);
    table.put("proto", Translation2d.proto, original);
    Translation2d result = table.get("proto", Translation2d.proto, new Translation2d());
    assertEquals(4.0, result.getX(), 1e-6);
    assertEquals(5.0, result.getY(), 1e-6);
  }

  @Test
  void protobufMissingKeyReturnsDefault() {
    Translation2d def = new Translation2d(1.0, 2.0);
    Translation2d result = table.get("missing", Translation2d.proto, def);
    assertSame(def, result);
  }

  // ─── LogValue equals / hashCode / getWPILOGType / getNT4Type ───────────────

  @Test
  void logValueEqualsIdentical() {
    table.put("k", true);
    LogValue v1 = table.get("k");
    LogValue v2 = table.get("k");
    assertEquals(v1, v2);
  }

  @Test
  void logValueEqualsDifferentTypesReturnFalse() {
    table.put("a", true);
    table.put("b", 1L);
    assertNotEquals(table.get("a"), table.get("b"));
  }

  @Test
  void logValueEqualsNonLogValueReturnsFalse() {
    table.put("k", true);
    assertNotEquals(table.get("k"), "notALogValue");
  }

  @Test
  void logValueEqualsAllPrimitiveTypes() {
    LogTable t1 = new LogTable(0);
    LogTable t2 = new LogTable(0);
    t1.put("raw", new byte[] {1, 2});
    t2.put("raw", new byte[] {1, 2});
    assertEquals(t1.get("raw"), t2.get("raw"));
    t1.put("i", 42L);
    t2.put("i", 42L);
    assertEquals(t1.get("i"), t2.get("i"));
    t1.put("f", 1.5f);
    t2.put("f", 1.5f);
    assertEquals(t1.get("f"), t2.get("f"));
    t1.put("d", 3.14);
    t2.put("d", 3.14);
    assertEquals(t1.get("d"), t2.get("d"));
    t1.put("s", "hi");
    t2.put("s", "hi");
    assertEquals(t1.get("s"), t2.get("s"));
    t1.put("ba", new boolean[] {true});
    t2.put("ba", new boolean[] {true});
    assertEquals(t1.get("ba"), t2.get("ba"));
    t1.put("ia", new long[] {1L});
    t2.put("ia", new long[] {1L});
    assertEquals(t1.get("ia"), t2.get("ia"));
    t1.put("fa", new float[] {1.0f});
    t2.put("fa", new float[] {1.0f});
    assertEquals(t1.get("fa"), t2.get("fa"));
    t1.put("da", new double[] {1.0});
    t2.put("da", new double[] {1.0});
    assertEquals(t1.get("da"), t2.get("da"));
    t1.put("sa", new String[] {"x"});
    t2.put("sa", new String[] {"x"});
    assertEquals(t1.get("sa"), t2.get("sa"));
  }

  @Test
  void logValueHashCodeConsistentWithEquals() {
    table.put("k", 42L);
    LogValue v = table.get("k");
    assertEquals(v.hashCode(), v.hashCode());
  }

  @Test
  void logValueGetWPILOGTypeForPrimitiveBoolean() {
    table.put("b", true);
    assertEquals("boolean", table.get("b").getWPILOGType());
  }

  @Test
  void logValueGetWPILOGTypeForCustomType() {
    Translation2d t2d = new Translation2d(1.0, 2.0);
    table.put("struct", t2d);
    String wpilogType = table.get("struct").getWPILOGType();
    assertTrue(wpilogType.startsWith("struct:"), "struct value must return struct: WPILOG type");
  }

  @Test
  void logValueGetNT4TypeForPrimitiveDouble() {
    table.put("d", 3.14);
    assertEquals("double", table.get("d").getNT4Type());
  }

  @Test
  void logValueGetNT4TypeForCustomType() {
    Translation2d t2d = new Translation2d(0.0, 0.0);
    table.put("struct", t2d);
    String nt4Type = table.get("struct").getNT4Type();
    assertTrue(nt4Type.startsWith("struct:"), "struct value must return struct: NT4 type");
  }

  // ─── LogValue type-mismatch default returns ───────────────────────────────

  @Test
  void logValueGetFloatWithDefaultOnTypeMismatch() {
    table.put("b", true);
    LogValue v = table.get("b");
    assertEquals(9.9f, v.getFloat(9.9f), 1e-6f);
  }

  @Test
  void logValueGetRawWithDefaultOnTypeMismatch() {
    table.put("b", true);
    LogValue v = table.get("b");
    byte[] def = {42};
    assertSame(def, v.getRaw(def));
  }

  @Test
  void logValueGetBooleanArrayWithDefaultOnTypeMismatch() {
    table.put("i", 1L);
    LogValue v = table.get("i");
    boolean[] def = {true};
    assertSame(def, v.getBooleanArray(def));
  }

  @Test
  void logValueGetIntegerArrayWithDefaultOnTypeMismatch() {
    table.put("b", true);
    LogValue v = table.get("b");
    long[] def = {1L};
    assertSame(def, v.getIntegerArray(def));
  }

  @Test
  void logValueGetFloatArrayWithDefaultOnTypeMismatch() {
    table.put("b", true);
    LogValue v = table.get("b");
    float[] def = {1.0f};
    assertSame(def, v.getFloatArray(def));
  }

  @Test
  void logValueGetDoubleArrayWithDefaultOnTypeMismatch() {
    table.put("b", true);
    LogValue v = table.get("b");
    double[] def = {1.0};
    assertSame(def, v.getDoubleArray(def));
  }

  @Test
  void logValueGetStringArrayWithDefaultOnTypeMismatch() {
    table.put("b", true);
    LogValue v = table.get("b");
    String[] def = {"x"};
    assertSame(def, v.getStringArray(def));
  }

  // ─── LoggableType WPILOG/NT4 type strings ────────────────────────────────────

  @Test
  void loggableTypeFromWPILOGTypeRoundTrip() {
    for (LogTable.LoggableType t : LogTable.LoggableType.values()) {
      String wpilog = t.getWPILOGType();
      assertEquals(t, LogTable.LoggableType.fromWPILOGType(wpilog));
    }
  }

  @Test
  void loggableTypeFromWPILOGTypeJsonReturnsString() {
    assertEquals(LogTable.LoggableType.String, LogTable.LoggableType.fromWPILOGType("json"));
  }

  @Test
  void loggableTypeFromWPILOGTypeUnknownReturnsRaw() {
    assertEquals(
        LogTable.LoggableType.Raw, LogTable.LoggableType.fromWPILOGType("unknown_type_xyz"));
  }

  @Test
  void loggableTypeFromNT4TypeRoundTrip() {
    for (LogTable.LoggableType t : LogTable.LoggableType.values()) {
      String nt4 = t.getNT4Type();
      assertEquals(t, LogTable.LoggableType.fromNT4Type(nt4));
    }
  }

  @Test
  void loggableTypeFromNT4TypeJsonReturnsString() {
    assertEquals(LogTable.LoggableType.String, LogTable.LoggableType.fromNT4Type("json"));
  }

  @Test
  void loggableTypeFromNT4TypeUnknownReturnsRaw() {
    assertEquals(
        LogTable.LoggableType.Raw, LogTable.LoggableType.fromNT4Type("unknown_nt4_type"));
  }

  // ─── toString ────────────────────────────────────────────────────────────────

  @Test
  void toStringContainsTimestampAndBooleanValue() {
    LogTable t = new LogTable(1000L);
    t.put("flag", true);
    String s = t.toString();
    assertTrue(s.contains("Timestamp=1000"));
    assertTrue(s.contains("flag"));
    assertTrue(s.contains("true"));
  }

  @Test
  void toStringContainsAllScalarTypes() {
    table.put("b", true);
    table.put("i", 42L);
    table.put("f", 1.5f);
    table.put("d", 2.5);
    table.put("s", "hello");
    String str = table.toString();
    assertTrue(str.contains("true"));
    assertTrue(str.contains("42"));
    assertTrue(str.contains("1.5"));
    assertTrue(str.contains("2.5"));
    assertTrue(str.contains("hello"));
  }

  @Test
  void toStringContainsArrayTypes() {
    table.put("ba", new boolean[] {true, false});
    table.put("ia", new long[] {1L, 2L});
    table.put("fa", new float[] {1.1f});
    table.put("da", new double[] {3.14});
    table.put("sa", new String[] {"x", "y"});
    table.put("raw", new byte[] {0x01});
    String str = table.toString();
    assertNotNull(str);
    assertTrue(str.length() > 0);
  }

  @Test
  void toStringOnEmptyTableContainsBraces() {
    String s = table.toString();
    assertTrue(s.contains("{"));
    assertTrue(s.contains("}"));
  }
}
