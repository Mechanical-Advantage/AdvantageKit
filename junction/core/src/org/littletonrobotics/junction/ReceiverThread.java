package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.littletonrobotics.junction.io.ByteEncoder;
import org.littletonrobotics.junction.io.LogDataReceiver;
import org.littletonrobotics.junction.io.LogRawDataReceiver;

public class ReceiverThread extends Thread {

  private final BlockingQueue<LogTable> queue;

  private ByteEncoder encoder;
  private List<LogDataReceiver> dataReceivers = new ArrayList<>();
  private List<LogRawDataReceiver> rawDataReceivers = new ArrayList<>();

  ReceiverThread(BlockingQueue<LogTable> queue) {
    super("LogReceiver");
    this.queue = queue;
  }

  void addDataReceiver(LogDataReceiver dataReceiver) {
    dataReceivers.add(dataReceiver);
  }

  void addDataReceiver(LogRawDataReceiver dataReceiver) {
    rawDataReceivers.add(dataReceiver);
  }

  public void run() {
    // Create new byte encoder (resets persistent data)
    if (rawDataReceivers.size() > 0) {
      encoder = new ByteEncoder();
    } else {
      encoder = null;
    }

    // Start data receivers
    for (int i = 0; i < dataReceivers.size(); i++) {
      dataReceivers.get(i).start();
    }
    for (int i = 0; i < rawDataReceivers.size(); i++) {
      rawDataReceivers.get(i).start(encoder);
    }

    try {
      while (true) {
        LogTable entry = queue.take(); // Wait for data

        // Encode to byte format
        if (rawDataReceivers.size() > 0) {
          encoder.encodeTable(entry);
        }

        // Send data to receivers
        for (int i = 0; i < dataReceivers.size(); i++) {
          dataReceivers.get(i).putEntry(entry);
        }
        for (int i = 0; i < rawDataReceivers.size(); i++) {
          rawDataReceivers.get(i).processEntry();
        }
      }
    } catch (InterruptedException exception) {

      // End all data receivers
      for (int i = 0; i < dataReceivers.size(); i++) {
        dataReceivers.get(i).end();
      }
      for (int i = 0; i < rawDataReceivers.size(); i++) {
        rawDataReceivers.get(i).end();
      }
    }
  }
}
