// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.inputs.LoggableInputs;

public class ThreadingTest {
  private static record TestRecord(int x, double y) {}

  private static class DummyInputs implements LoggableInputs {
    public boolean toLogCalled = false;

    @Override
    public void toLog(LogTable table) {
      toLogCalled = true;
    }

    @Override
    public void fromLog(LogTable table) {}
  }

  @AfterEach
  public void cleanup() {
    Logger.setRunning(false);
    Logger.setMainThread(null);
    Logger.AdvancedHooks.setWarningHandler(null);
  }

  @Test
  public void testAsyncQueueDrainOnLogTable() throws InterruptedException {
    Logger.setMainThread(Thread.currentThread());
    LogTable table = new LogTable(0);

    Thread bgThread =
        new Thread(
            () -> {
              table.put("AsyncDouble", 123.456);
              table.put("AsyncString", "hello");
              table.put("AsyncRecord", new TestRecord(99, 88.8));
            });

    bgThread.start();
    bgThread.join();

    // Values should be queued and not yet in data
    assertNull(table.get("AsyncDouble"));
    assertNull(table.get("AsyncString"));
    assertNull(table.get("AsyncRecord"));

    // Drain the queue on the main thread
    table.drainAsyncQueue();

    // Values should now be accessible
    assertEquals(123.456, table.get("AsyncDouble").getDouble());
    assertEquals("hello", table.get("AsyncString").getString());
    TestRecord defaultRecord = new TestRecord(0, 0.0);
    TestRecord restored = table.get("AsyncRecord", defaultRecord);
    assertEquals(new TestRecord(99, 88.8), restored);
  }

  @Test
  public void testProcessInputsFromBackgroundThread() throws InterruptedException {
    Logger.setRunning(true);
    Logger.setMainThread(Thread.currentThread());

    AtomicBoolean warned = new AtomicBoolean(false);
    Logger.AdvancedHooks.setWarningHandler((msg, trace) -> warned.set(true));

    DummyInputs bgInputs = new DummyInputs();
    Thread bgThread =
        new Thread(
            () -> {
              Logger.processInputs("BgInputs", bgInputs);
            });

    bgThread.start();
    bgThread.join();

    // bgInputs.toLog should NOT have been called (no-op)
    assertFalse(bgInputs.toLogCalled);
    assertTrue(warned.get());

    // Calling processInputs from the main thread should work
    DummyInputs mainInputs = new DummyInputs();
    Logger.processInputs("MainInputs", mainInputs);
    assertTrue(mainInputs.toLogCalled);
  }

  @Test
  public void testLoggerRecordOutputFromBackgroundThread() throws InterruptedException {
    Logger.setRunning(true);
    Logger.setMainThread(Thread.currentThread());

    Thread bgThread =
        new Thread(
            () -> {
              Logger.recordOutput("BgOutput", 42.0);
              Logger.recordOutput("BgString", "multithreaded");
            });

    bgThread.start();
    bgThread.join();

    LogTable entry = Logger.AdvancedHooks.getEntry();
    LogTable outputTable = entry.getSubtable("RealOutputs");

    // Before drain, background outputs should not be visible in data
    assertNull(outputTable.get("BgOutput"));
    assertNull(outputTable.get("BgString"));

    // Drain queue on main thread
    entry.drainAsyncQueue();

    // After drain, background outputs should be visible
    assertEquals(42.0, outputTable.get("BgOutput").getDouble());
    assertEquals("multithreaded", outputTable.get("BgString").getString());
  }

  @Test
  public void testConcurrentOutputLogging() throws InterruptedException {
    Logger.setMainThread(Thread.currentThread());
    LogTable table = new LogTable(0);

    int threadCount = 8;
    int itemsPerThread = 50;
    List<Thread> threads = new ArrayList<>();

    for (int t = 0; t < threadCount; t++) {
      final int threadId = t;
      Thread thread =
          new Thread(
              () -> {
                for (int i = 0; i < itemsPerThread; i++) {
                  table.put("Thread_" + threadId + "/Item_" + i, (double) (threadId * 1000 + i));
                }
              });
      threads.add(thread);
    }

    for (Thread thread : threads) {
      thread.start();
    }

    // Main thread also writes directly
    for (int i = 0; i < itemsPerThread; i++) {
      table.put("MainThread/Item_" + i, (double) i);
    }

    for (Thread thread : threads) {
      thread.join();
    }

    // Main thread items should already be in table
    for (int i = 0; i < itemsPerThread; i++) {
      assertEquals((double) i, table.get("MainThread/Item_" + i).getDouble());
    }

    // Drain background thread items
    table.drainAsyncQueue();

    // Verify all background thread items
    for (int t = 0; t < threadCount; t++) {
      for (int i = 0; i < itemsPerThread; i++) {
        assertEquals((double) (t * 1000 + i), table.get("Thread_" + t + "/Item_" + i).getDouble());
      }
    }
  }
}
