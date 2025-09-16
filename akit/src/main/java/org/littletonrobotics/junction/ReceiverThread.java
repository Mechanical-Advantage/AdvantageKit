// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

class ReceiverThread extends Thread {
  private final BlockingQueue<LogTable> queue;
  private List<LogDataReceiver> dataReceivers = new ArrayList<>();

  ReceiverThread(BlockingQueue<LogTable> queue) {
    super("AdvantageKit_LogReceiver");
    this.setDaemon(true);
    this.queue = queue;
  }

  void addDataReceiver(LogDataReceiver dataReceiver) {
    dataReceivers.add(dataReceiver);
  }

  public void run() {
    // Start data receivers
    for (int i = 0; i < dataReceivers.size(); i++) {
      dataReceivers.get(i).start();
    }

    try {
      while (true) {
        LogTable entry = queue.take(); // Wait for data

        // Send data to receivers
        for (int i = 0; i < dataReceivers.size(); i++) {
          dataReceivers.get(i).putTable(entry);
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
