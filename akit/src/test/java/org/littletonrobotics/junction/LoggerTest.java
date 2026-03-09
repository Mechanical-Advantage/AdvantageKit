// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.littletonrobotics.junction.inputs.LoggableInputs;

/**
 * Tests for the Logger class.
 *
 * <p>Logger holds static state, so tests are ordered to avoid interference. Tests at Order 1-9
 * verify pre-start behaviour. The lifecycle test at Order 10 starts Logger exactly once — the
 * receiver thread cannot be restarted after it is joined by {@link Logger#end()}.
 *
 * <p>Note: {@code Logger.periodicAfterUser()} calls {@code ConduitApi} which loads
 * {@code libwpilibio.dylib}. That library's rpath does not include the WPI native lib directory,
 * so it fails to open in the unit-test JVM on macOS. For that reason the lifecycle test verifies
 * Logger's output table directly via reflection rather than running a full periodic cycle.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoggerTest {

  // Simple LoggableInputs used across tests
  private static class DoubleInputs implements LoggableInputs {
    double value = 0.0;

    @Override
    public void toLog(LogTable table) {
      table.put("Value", value);
    }

    @Override
    public void fromLog(LogTable table) {
      value = table.get("Value", 0.0);
    }
  }

  @BeforeAll
  static void setUp() {
    assertTrue(HAL.initialize(500, 0), "HAL initialization must succeed");
    Logger.AdvancedHooks.disableRobotBaseCheck();
    Logger.disableConsoleCapture();
  }

  // ── Pre-start behaviour ────────────────────────────────────────────────────

  @Test
  @Order(1)
  void hasReplaySourceReturnsFalseByDefault() {
    assertFalse(Logger.hasReplaySource());
  }

  @Test
  @Order(2)
  void getReceiverQueueFaultFalseInitially() {
    assertFalse(Logger.getReceiverQueueFault());
  }

  @Test
  @Order(3)
  void recordMetadataBeforeStartDoesNotThrow() {
    assertDoesNotThrow(() -> Logger.recordMetadata("BuildDate", "2026-01-01"));
  }

  @Test
  @Order(4)
  void recordOutputNoOpWhenNotRunning() {
    assertDoesNotThrow(
        () -> {
          Logger.recordOutput("NotRunningBool", true);
          Logger.recordOutput("NotRunningLong", 99L);
          Logger.recordOutput("NotRunningDouble", 1.23);
          Logger.recordOutput("NotRunningString", "noop");
          Logger.recordOutput("NotRunningBoolArr", new boolean[] {true, false});
          Logger.recordOutput("NotRunningIntArr", new int[] {1, 2});
          Logger.recordOutput("NotRunningLongArr", new long[] {1L, 2L});
          Logger.recordOutput("NotRunningFloatArr", new float[] {1.0f});
          Logger.recordOutput("NotRunningDoubleArr", new double[] {1.0, 2.0});
          Logger.recordOutput("NotRunningByteArr", new byte[] {0x01});
          Logger.recordOutput("NotRunningStringArr", new String[] {"a"});
          Logger.recordOutput("NotRunningInt", 5);
          Logger.recordOutput("NotRunningFloat", 1.5f);
          Logger.recordOutput("NotRunningLongLambda", (java.util.function.LongSupplier) () -> 0L);
          Logger.recordOutput("NotRunningIntLambda", (java.util.function.IntSupplier) () -> 0);
          Logger.recordOutput(
              "NotRunningBoolLambda", (java.util.function.BooleanSupplier) () -> false);
          Logger.recordOutput(
              "NotRunningDoubleLambda", (java.util.function.DoubleSupplier) () -> 0.0);
        });
  }

  @Test
  @Order(5)
  void processInputsNoOpWhenNotRunning() {
    DoubleInputs inputs = new DoubleInputs();
    inputs.value = 999.0;
    Logger.processInputs("MySubsystem", inputs);
    // processInputs is a no-op; value must be untouched
    assertEquals(999.0, inputs.value);
  }

  @Test
  @Order(6)
  void runEveryNWithN1AlwaysFires() {
    // runEveryN(1, ...) satisfies cycleCount % 1 == 0 for any cycleCount
    boolean[] ran = {false};
    Logger.runEveryN(1, () -> ran[0] = true);
    assertTrue(ran[0], "runEveryN(1, ...) must fire on every call");
  }

  @Test
  @Order(7)
  void setReplaySourceRoundTrip() {
    LogReplaySource src =
        new LogReplaySource() {
          @Override
          public boolean updateTable(LogTable table) {
            return false;
          }
        };

    Logger.setReplaySource(src);
    assertTrue(Logger.hasReplaySource());

    Logger.setReplaySource(null);
    assertFalse(Logger.hasReplaySource());
  }

  @Test
  @Order(8)
  void advancedHooksPeriodicBeforeUserDoesNotThrow() {
    // Can call periodicBeforeUser before start; running guard means it mostly no-ops
    assertDoesNotThrow(() -> Logger.AdvancedHooks.invokePeriodicBeforeUser());
  }

  @Test
  @Order(9)
  void registerURCLDoesNotThrow() {
    assertDoesNotThrow(() -> Logger.registerURCL(() -> new java.nio.ByteBuffer[0]));
    // Reset to avoid influencing the lifecycle test
    Logger.registerURCL(null);
  }

  // ── Full lifecycle ─────────────────────────────────────────────────────────

  /**
   * Starts Logger once, exercises recordOutput and processInputs, then ends Logger.
   *
   * <p>The output table is verified directly via reflection to avoid calling
   * {@code periodicAfterUser()} which triggers {@code ConduitApi} / {@code libwpilibio.dylib}.
   *
   * <p>Must run last — Logger's receiver thread cannot be restarted once joined by {@code end()}.
   */
  @Test
  @Order(10)
  void fullLifecycleRecordsOutputsInOutputTable() throws Exception {
    Logger.start();

    // Setup-phase guards: these must be no-ops once Logger is running
    Logger.recordMetadata("ignoredAfterStart", "value"); // should be silently dropped
    Logger.setReplaySource(
        new LogReplaySource() {
          @Override
          public boolean updateTable(LogTable table) {
            return false;
          }
        }); // should be silently dropped
    assertFalse(Logger.hasReplaySource(), "setReplaySource must be ignored after start");

    // getTimestamp() while running must return a positive value (HAL FPGA time)
    assertTrue(Logger.getTimestamp() > 0, "getTimestamp() must return positive FPGA time");

    // getReceiverQueueFault must be false immediately after start
    assertFalse(Logger.getReceiverQueueFault());

    // --- record scalar output types ---
    Logger.recordOutput("BoolOut", true);
    Logger.recordOutput("BoolOut2", false);
    Logger.recordOutput("IntOut", 7);
    Logger.recordOutput("LongOut", 12345L);
    Logger.recordOutput("FloatOut", 2.5f);
    Logger.recordOutput("DoubleOut", Math.PI);
    Logger.recordOutput("StringOut", "hello");

    // --- record 1-D array output types ---
    Logger.recordOutput("BoolArrOut", new boolean[] {true, false, true});
    Logger.recordOutput("IntArrOut", new int[] {1, 2, 3});
    Logger.recordOutput("LongArrOut", new long[] {10L, 20L});
    Logger.recordOutput("FloatArrOut", new float[] {1.1f, 2.2f});
    Logger.recordOutput("DoubleArrOut", new double[] {1.0, 2.0, 3.0});
    Logger.recordOutput("StringArrOut", new String[] {"a", "b"});
    Logger.recordOutput("ByteArrOut", new byte[] {0x01, 0x02});

    // --- record 2-D array output types ---
    Logger.recordOutput("Bool2dOut", new boolean[][] {{true, false}, {false, true}});
    Logger.recordOutput("Int2dOut", new int[][] {{1, 2}, {3}});
    Logger.recordOutput("Long2dOut", new long[][] {{10L, 20L}});
    Logger.recordOutput("Double2dOut", new double[][] {{1.0, 2.0}});
    Logger.recordOutput("Byte2dOut", new byte[][] {{0x01}, {0x02}});

    // --- lambda suppliers ---
    Logger.recordOutput("LambdaBool", (java.util.function.BooleanSupplier) () -> true);
    Logger.recordOutput("LambdaInt", (java.util.function.IntSupplier) () -> 42);
    Logger.recordOutput("LambdaLong", (java.util.function.LongSupplier) () -> 99L);
    Logger.recordOutput("LambdaDouble", (java.util.function.DoubleSupplier) () -> 3.14);

    // --- log inputs (real robot → toLog path) ---
    DoubleInputs inputs = new DoubleInputs();
    inputs.value = 7.5;
    Logger.processInputs("MySubsystem", inputs);

    // --- read the private outputTable field via reflection ---
    Field outputTableField = Logger.class.getDeclaredField("outputTable");
    outputTableField.setAccessible(true);
    LogTable outputs = (LogTable) outputTableField.get(null);

    // --- also read the entry table to check processInputs ---
    Field entryField = Logger.class.getDeclaredField("entry");
    entryField.setAccessible(true);
    LogTable entry = (LogTable) entryField.get(null);

    // runEveryN while running
    boolean[] ranInCycle = {false};
    Logger.runEveryN(1, () -> ranInCycle[0] = true);
    assertTrue(ranInCycle[0], "runEveryN(1) must fire when Logger is running");

    Logger.end();

    // --- verify all output types ---
    assertTrue(outputs.get("BoolOut", false));
    assertFalse(outputs.get("BoolOut2", true));
    assertEquals(7, outputs.get("IntOut", 0));
    assertEquals(12345L, outputs.get("LongOut", 0L));
    assertEquals(2.5f, outputs.get("FloatOut", 0.0f), 1e-6f);
    assertEquals(Math.PI, outputs.get("DoubleOut", 0.0), 1e-12);
    assertEquals("hello", outputs.get("StringOut", ""));
    assertArrayEquals(new boolean[] {true, false, true}, outputs.get("BoolArrOut", new boolean[0]));
    assertArrayEquals(new int[] {1, 2, 3}, outputs.get("IntArrOut", new int[0]));
    assertArrayEquals(new long[] {10L, 20L}, outputs.get("LongArrOut", new long[0]));
    assertArrayEquals(new float[] {1.1f, 2.2f}, outputs.get("FloatArrOut", new float[0]));
    assertArrayEquals(
        new double[] {1.0, 2.0, 3.0}, outputs.get("DoubleArrOut", new double[0]), 0.0);
    assertArrayEquals(new String[] {"a", "b"}, outputs.get("StringArrOut", new String[0]));
    assertArrayEquals(new byte[] {0x01, 0x02}, outputs.get("ByteArrOut", new byte[0]));

    // Lambda outputs are recorded as their resolved scalar values
    assertTrue(outputs.get("LambdaBool", false));
    assertEquals(42, outputs.get("LambdaInt", 0));
    assertEquals(99L, outputs.get("LambdaLong", 0L));
    assertEquals(3.14, outputs.get("LambdaDouble", 0.0), 1e-12);

    // processInputs wrote inputs to the entry table (toLog path on real robot)
    assertEquals(7.5, entry.getSubtable("MySubsystem").get("Value", 0.0), 1e-12);
  }
}
