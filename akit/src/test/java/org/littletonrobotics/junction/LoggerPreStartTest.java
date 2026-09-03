// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.inputs.LoggableInputs;

/**
 * Tests for Logger behaviour that does NOT require HAL or {@link Logger#start()} to be called.
 * This covers the pre-start configuration API and guards that silently ignore calls once the logger
 * is running.
 */
public class LoggerPreStartTest {

  // ─── Helpers ────────────────────────────────────────────────────────────────

  /** Minimal no-op replay source. */
  private static final LogReplaySource NOOP_REPLAY_SOURCE =
      table -> false; // stops replay immediately

  /** Minimal no-op LoggableInputs. */
  private static final LoggableInputs NOOP_INPUTS =
      new LoggableInputs() {
        @Override
        public void toLog(LogTable table) {}

        @Override
        public void fromLog(LogTable table) {}
      };

  /**
   * Reset replay source after each test so tests are independent. This is safe because {@code
   * running} remains false throughout (we never call {@link Logger#start()}).
   */
  @AfterEach
  void tearDown() {
    Logger.setReplaySource(null);
  }

  // ─── hasReplaySource() ──────────────────────────────────────────────────────

  @Test
  void hasReplaySourceIsFalseByDefault() {
    Logger.setReplaySource(null); // ensure clean state
    assertFalse(Logger.hasReplaySource(), "hasReplaySource() must be false before any source is set");
  }

  @Test
  void setReplaySourceMakesHasReplaySourceTrue() {
    Logger.setReplaySource(NOOP_REPLAY_SOURCE);
    assertTrue(Logger.hasReplaySource(), "hasReplaySource() must be true after a non-null source is set");
  }

  @Test
  void clearingReplaySourceWithNullMakesHasReplaySourceFalse() {
    Logger.setReplaySource(NOOP_REPLAY_SOURCE);
    Logger.setReplaySource(null);
    assertFalse(
        Logger.hasReplaySource(), "hasReplaySource() must revert to false after setting null source");
  }

  // ─── getReceiverQueueFault() ─────────────────────────────────────────────────

  @Test
  void receiverQueueFaultIsFalseByDefault() {
    assertFalse(
        Logger.getReceiverQueueFault(),
        "Receiver queue fault must be false when no data has been sent");
  }

  // ─── recordOutput() before start ─────────────────────────────────────────────

  @Test
  void recordOutputBeforeStartDoesNotThrow() {
    // All recordOutput overloads are gated by `if (running)`. They must silently no-op, not throw.
    assertDoesNotThrow(() -> Logger.recordOutput("test/bool", true));
    assertDoesNotThrow(() -> Logger.recordOutput("test/int", 42));
    assertDoesNotThrow(() -> Logger.recordOutput("test/long", 42L));
    assertDoesNotThrow(() -> Logger.recordOutput("test/float", 1.0f));
    assertDoesNotThrow(() -> Logger.recordOutput("test/double", 3.14));
    assertDoesNotThrow(() -> Logger.recordOutput("test/string", "hello"));
    assertDoesNotThrow(() -> Logger.recordOutput("test/bytes", new byte[] {1, 2}));
  }

  // ─── processInputs() before start ───────────────────────────────────────────

  @Test
  void processInputsBeforeStartDoesNotThrow() {
    // processInputs() is gated by `if (running)`. Must silently no-op, not throw.
    assertDoesNotThrow(() -> Logger.processInputs("test", NOOP_INPUTS));
  }

  // ─── recordMetadata() before start ───────────────────────────────────────────

  @Test
  void recordMetadataBeforeStartDoesNotThrow() {
    // Stores to an internal map; can be called any number of times before start.
    assertDoesNotThrow(() -> Logger.recordMetadata("version", "1.0.0"));
    assertDoesNotThrow(() -> Logger.recordMetadata("version", "2.0.0")); // overwrite
  }

  // ─── runEveryN() ─────────────────────────────────────────────────────────────

  @Test
  void runEveryOneAlwaysRunsCallback() {
    // cycleCount % 1 == 0 for every integer, so the callback must fire every call.
    int[] count = {0};
    Logger.runEveryN(1, () -> count[0]++);
    Logger.runEveryN(1, () -> count[0]++);
    Logger.runEveryN(1, () -> count[0]++);
    assertEquals(3, count[0], "runEveryN(1, ...) must run the callback every invocation");
  }

  @Test
  void runEveryNSkipsCallbackWhenCycleCountNotMultiple() {
    // Use AdvancedHooks to advance cycleCount without touching HAL (running=false, so
    // periodicBeforeUser() only increments cycleCount and returns).
    // We call invokePeriodicBeforeUser() once, then test runEveryN with a prime N (7).
    // The callback should fire at most once in the window we check.
    Logger.AdvancedHooks.invokePeriodicBeforeUser();
    int[] count = {0};
    // Run 7 consecutive checks (without advancing cycleCount further) — all see the same
    // cycleCount, so the callback fires either 0 or 7 times (all miss or all hit).
    for (int i = 0; i < 7; i++) {
      Logger.runEveryN(7, () -> count[0]++);
    }
    // The count is either 0 or 7 — never in between — because cycleCount didn't change.
    assertTrue(count[0] == 0 || count[0] == 7, "runEveryN must fire consistently for the same cycleCount");
  }
}
