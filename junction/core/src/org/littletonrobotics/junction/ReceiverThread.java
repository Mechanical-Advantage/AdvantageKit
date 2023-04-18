package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ReceiverThread extends Thread {

  private final BlockingQueue<LogTable> queue;
  private final List<LogDataReceiver> dataReceivers = new ArrayList<>();

  ReceiverThread(BlockingQueue<LogTable> queue) {
    super("LogReceiver");
    this.setDaemon(true);
    this.queue = queue;
  }

  void addDataReceiver(LogDataReceiver dataReceiver) {
    dataReceivers.add(dataReceiver);
  }

  public void run() {
    // Start data receivers
    for (LogDataReceiver dataReceiver : dataReceivers) {
      dataReceiver.start();
    }

    try {
      while (true) {
        LogTable entry = queue.take(); // Wait for data

        // Send data to receivers
        for (LogDataReceiver dataReceiver : dataReceivers) {
          dataReceiver.putTable(entry);
        }
      }
    } catch (InterruptedException exception) {

      // End all data receivers
      for (LogDataReceiver dataReceiver : dataReceivers) {
        dataReceiver.end();
      }
    }
  }
}
