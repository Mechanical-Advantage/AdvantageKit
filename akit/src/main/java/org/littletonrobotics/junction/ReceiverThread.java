// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.wpilib.system.Timer;

class ReceiverThread extends Thread {
  private final BlockingQueue<LogTable> queue;
  private List<LogDataReceiver> dataReceivers = new ArrayList<>();
  private List<String> dataReceiverNames = new ArrayList<>();
  private Map<String, Double> traceTimes = new HashMap<>();
  private Map<String, Double> traceTimesLocal = new HashMap<>();

  ReceiverThread(BlockingQueue<LogTable> queue) {
    super("AdvantageKit_LogReceiver");
    this.setDaemon(true);
    this.queue = queue;
  }

  void addDataReceiver(LogDataReceiver dataReceiver) {
    dataReceivers.add(dataReceiver);

    // Save receiver name
    String name = dataReceiver.getClass().getSimpleName();
    if (!dataReceiverNames.contains(name)) {
      dataReceiverNames.add(name);
      return;
    }

    // Name conflict, add number identifier
    int id = 1;
    while (dataReceiverNames.contains(name + "(" + id + ")")) {
      id++;
    }
    dataReceiverNames.add(name + "(" + id + ")");
  }

  /** Log periodic diagnostic outputs. Only call from the main thread. */
  public synchronized void logOutputs() {
    Logger.recordOutput("Logger/DataReceivers/QueuedCycles", queue.size());
    traceTimes.forEach(
        (receiverName, value) -> {
          Logger.recordOutput("Logger/DataReceivers/" + receiverName + "MS", value);
        });
  }

  public void run() {
    // Start data receivers
    for (int i = 0; i < dataReceivers.size(); i++) {
      dataReceivers.get(i).start();
    }

    try {
      while (true) {
        LogTable entry = queue.take(); // Wait for data

        // Reset trace times
        traceTimesLocal.clear();

        // Send data to receivers
        for (int i = 0; i < dataReceivers.size(); i++) {
          double startTime = Timer.getMonotonicTimestamp();
          dataReceivers.get(i).putTable(entry);
          double endTime = Timer.getMonotonicTimestamp();
          traceTimesLocal.put(dataReceiverNames.get(i), (endTime - startTime) * 1000.0);
        }

        // Save trace times
        synchronized (this) {
          var oldTraceTimes = traceTimes;
          traceTimes = traceTimesLocal;
          traceTimesLocal = oldTraceTimes;
        }
      }
    } catch (InterruptedException exception) {
      // Empty queue
      while (!queue.isEmpty()) {
        LogTable entry = queue.poll();
        for (int i = 0; i < dataReceivers.size(); i++) {
          try {
            dataReceivers.get(i).putTable(entry);
          } catch (InterruptedException e) {
          }
        }
      }

      // End all data receivers
      for (int i = 0; i < dataReceivers.size(); i++) {
        dataReceivers.get(i).end();
      }
    }
  }
}
