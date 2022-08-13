package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.littletonrobotics.junction.rlog.RLOGDataReceiver;
import org.littletonrobotics.junction.rlog.RLOGEncoder;

public class ReceiverThread extends Thread {

  private final BlockingQueue<LogTable> queue;

  private RLOGEncoder rlogEncoder;
  private List<LogDataReceiver> dataReceivers = new ArrayList<>();
  private List<RLOGDataReceiver> rlogReceivers = new ArrayList<>();

  ReceiverThread(BlockingQueue<LogTable> queue) {
    super("LogReceiver");
    this.setDaemon(true);
    this.queue = queue;
  }

  void addDataReceiver(LogDataReceiver dataReceiver) {
    dataReceivers.add(dataReceiver);
  }

  void addDataReceiver(RLOGDataReceiver dataReceiver) {
    rlogReceivers.add(dataReceiver);
  }

  public void run() {
    // Create new RLOG encoder (resets persistent data)
    if (rlogReceivers.size() > 0) {
      rlogEncoder = new RLOGEncoder();
    } else {
      rlogEncoder = null;
    }

    // Start data receivers
    for (int i = 0; i < dataReceivers.size(); i++) {
      dataReceivers.get(i).start();
    }
    for (int i = 0; i < rlogReceivers.size(); i++) {
      rlogReceivers.get(i).start(rlogEncoder);
    }

    try {
      while (true) {
        LogTable entry = queue.take(); // Wait for data

        // Encode to RLOG
        if (rlogReceivers.size() > 0) {
          rlogEncoder.encodeTable(entry);
        }

        // Send data to receivers
        for (int i = 0; i < dataReceivers.size(); i++) {
          dataReceivers.get(i).putEntry(entry);
        }
        for (int i = 0; i < rlogReceivers.size(); i++) {
          rlogReceivers.get(i).processEntry();
        }
      }
    } catch (InterruptedException exception) {

      // End all data receivers
      for (int i = 0; i < dataReceivers.size(); i++) {
        dataReceivers.get(i).end();
      }
      for (int i = 0; i < rlogReceivers.size(); i++) {
        rlogReceivers.get(i).end();
      }
    }
  }
}
