// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ReceiverThread extends Thread {

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

      // End all data receivers
      for (int i = 0; i < dataReceivers.size(); i++) {
        dataReceivers.get(i).end();
      }
    }
  }
}
