// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.Test;

/** Tests for ReceiverThread data delivery, multi-receiver fan-out, and shutdown behaviour. */
public class ReceiverThreadTest {

  // ─── Test doubles ───────────────────────────────────────────────────────────

  /** Captures every table delivered to it and records lifecycle calls. */
  private static class CapturingReceiver implements LogDataReceiver {
    final List<LogTable> received = new ArrayList<>();
    boolean started = false;
    boolean ended = false;

    @Override
    public void start() {
      started = true;
    }

    @Override
    public void end() {
      ended = true;
    }

    @Override
    public void putTable(LogTable table) {
      received.add(table);
    }
  }

  /**
   * Throws {@link InterruptedException} from {@link #putTable} to simulate a receiver that treats
   * the call as an interrupt signal.
   */
  private static class InterruptThrowingReceiver implements LogDataReceiver {
    boolean endCalled = false;

    @Override
    public void end() {
      endCalled = true;
    }

    @Override
    public void putTable(LogTable table) throws InterruptedException {
      throw new InterruptedException("simulated receiver interrupt");
    }
  }

  // ─── Helpers ────────────────────────────────────────────────────────────────

  /** Waits up to {@code maxMs} for {@code condition} to become true, polling every 10 ms. */
  private static void awaitCondition(long maxMs, java.util.function.BooleanSupplier condition)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + maxMs;
    while (!condition.getAsBoolean()) {
      if (System.currentTimeMillis() > deadline) break;
      Thread.sleep(10);
    }
  }

  // ─── Lifecycle ──────────────────────────────────────────────────────────────

  @Test
  void receiverStartIsCalledWhenThreadStarts() throws InterruptedException {
    BlockingQueue<LogTable> queue = new ArrayBlockingQueue<>(10);
    ReceiverThread thread = new ReceiverThread(queue);
    CapturingReceiver receiver = new CapturingReceiver();
    thread.addDataReceiver(receiver);

    thread.start();
    awaitCondition(1000, () -> receiver.started);
    assertTrue(receiver.started, "start() must be called when the thread begins running");

    thread.interrupt();
    thread.join(2000);
  }

  @Test
  void receiverEndIsCalledAfterInterrupt() throws InterruptedException {
    BlockingQueue<LogTable> queue = new ArrayBlockingQueue<>(10);
    ReceiverThread thread = new ReceiverThread(queue);
    CapturingReceiver receiver = new CapturingReceiver();
    thread.addDataReceiver(receiver);

    thread.start();
    awaitCondition(500, () -> receiver.started);
    thread.interrupt();
    thread.join(2000);

    assertTrue(receiver.ended, "end() must be called after the thread is interrupted");
  }

  @Test
  void threadIsDaemon() {
    BlockingQueue<LogTable> queue = new ArrayBlockingQueue<>(10);
    ReceiverThread thread = new ReceiverThread(queue);
    assertTrue(thread.isDaemon(), "ReceiverThread must be a daemon thread");
  }

  // ─── Data delivery ──────────────────────────────────────────────────────────

  @Test
  void singleTableIsDeliveredToReceiver() throws InterruptedException {
    BlockingQueue<LogTable> queue = new ArrayBlockingQueue<>(10);
    ReceiverThread thread = new ReceiverThread(queue);
    CapturingReceiver receiver = new CapturingReceiver();
    thread.addDataReceiver(receiver);
    thread.start();

    LogTable entry = new LogTable(1000L);
    entry.put("val", 42.0);
    queue.put(entry);

    awaitCondition(1000, () -> !receiver.received.isEmpty());
    assertEquals(1, receiver.received.size());
    assertEquals(42.0, receiver.received.get(0).get("val", 0.0));

    thread.interrupt();
    thread.join(2000);
  }

  @Test
  void multipleTablesDeliveredInFifoOrder() throws InterruptedException {
    BlockingQueue<LogTable> queue = new ArrayBlockingQueue<>(20);
    ReceiverThread thread = new ReceiverThread(queue);
    CapturingReceiver receiver = new CapturingReceiver();
    thread.addDataReceiver(receiver);

    // Enqueue all before starting so ordering is deterministic
    for (int i = 0; i < 5; i++) {
      LogTable entry = new LogTable((long) i);
      entry.put("idx", (long) i);
      queue.put(entry);
    }

    thread.start();
    awaitCondition(2000, () -> receiver.received.size() >= 5);

    assertEquals(5, receiver.received.size());
    for (int i = 0; i < 5; i++) {
      assertEquals((long) i, receiver.received.get(i).get("idx", -1L), "Entry " + i + " out of order");
    }

    thread.interrupt();
    thread.join(2000);
  }

  @Test
  void allReceiversGetEveryTable() throws InterruptedException {
    BlockingQueue<LogTable> queue = new ArrayBlockingQueue<>(10);
    ReceiverThread thread = new ReceiverThread(queue);
    CapturingReceiver r1 = new CapturingReceiver();
    CapturingReceiver r2 = new CapturingReceiver();
    CapturingReceiver r3 = new CapturingReceiver();
    thread.addDataReceiver(r1);
    thread.addDataReceiver(r2);
    thread.addDataReceiver(r3);
    thread.start();

    LogTable entry = new LogTable(100L);
    queue.put(entry);

    awaitCondition(1000, () -> !r1.received.isEmpty() && !r2.received.isEmpty() && !r3.received.isEmpty());
    assertEquals(1, r1.received.size(), "Receiver 1 must get the table");
    assertEquals(1, r2.received.size(), "Receiver 2 must get the table");
    assertEquals(1, r3.received.size(), "Receiver 3 must get the table");

    thread.interrupt();
    thread.join(2000);
  }

  // ─── Shutdown / drain ───────────────────────────────────────────────────────

  @Test
  void queueIsDrainedBeforeEndOnShutdown() throws InterruptedException {
    BlockingQueue<LogTable> queue = new ArrayBlockingQueue<>(20);
    ReceiverThread thread = new ReceiverThread(queue);
    CapturingReceiver receiver = new CapturingReceiver();
    thread.addDataReceiver(receiver);

    // Fill queue before the thread has a chance to drain it
    for (int i = 0; i < 5; i++) {
      LogTable entry = new LogTable((long) i);
      entry.put("val", (long) i);
      queue.put(entry);
    }

    thread.start();
    // Small sleep so thread starts waiting in queue.take()
    Thread.sleep(50);
    thread.interrupt();
    thread.join(2000);

    // All entries must have been processed (either in the run loop or the drain loop)
    assertEquals(
        5,
        receiver.received.size(),
        "All queued entries must be delivered before the thread terminates");
  }

  // ─── InterruptedException from putTable is isolated per-receiver ────────────

  @Test
  void interruptExceptionFromReceiverPutTableDoesNotSkipSubsequentReceivers()
      throws InterruptedException {
    // A receiver whose putTable() throws InterruptedException must not prevent
    // subsequent receivers in the same cycle from receiving the table.
    // The fix: each putTable() call is wrapped in its own try/catch; the caught
    // exception re-interrupts the thread so that queue.take() on the next iteration
    // initiates clean shutdown, but all receivers still get the current entry.
    BlockingQueue<LogTable> queue = new ArrayBlockingQueue<>(10);
    ReceiverThread thread = new ReceiverThread(queue);

    InterruptThrowingReceiver bad = new InterruptThrowingReceiver();
    CapturingReceiver good = new CapturingReceiver();

    // bad is first; its exception must be caught without skipping good
    thread.addDataReceiver(bad);
    thread.addDataReceiver(good);
    thread.start();

    awaitCondition(500, () -> good.started);

    queue.put(new LogTable(1L));
    thread.join(2000); // Thread terminates after re-interrupt propagates to queue.take()

    assertFalse(thread.isAlive(), "Thread must terminate after receiver signals interrupt");
    assertEquals(
        1,
        good.received.size(),
        "Receiver after the throwing one must still receive the table for that cycle");
  }
}
