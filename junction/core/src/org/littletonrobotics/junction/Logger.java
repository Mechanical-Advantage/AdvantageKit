package org.littletonrobotics.junction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.wpi.first.hal.HALUtil;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.inputs.*;
import org.littletonrobotics.junction.io.LogDataReceiver;
import org.littletonrobotics.junction.io.LogRawDataReceiver;
import org.littletonrobotics.junction.io.LogReplaySource;

/** Central class for recording and replaying log data. */
public class Logger {
  private static final int receiverQueueCapcity = 500; // 10s at 50Hz
  private static final boolean debugTiming = false;

  private static Logger instance;

  private boolean running = false;
  private double lastDebugPrint = 0.0;
  private LogTable entry;
  private LogTable outputTable;
  private Map<String, String> metadata = new HashMap<>();

  private LogReplaySource replaySource;
  private final BlockingQueue<LogTable> receiverQueue = new ArrayBlockingQueue<LogTable>(receiverQueueCapcity);
  private final ReceiverThread receiverThread = new ReceiverThread(receiverQueue);

  private Logger() {
  }

  public static Logger getInstance() {
    if (instance == null) {
      instance = new Logger();
    }
    return instance;
  }

  /**
   * Sets the source to use for replaying data. Use null to disable replay. This
   * method only works during setup before starting to log.
   */
  public void setReplaySource(LogReplaySource replaySource) {
    if (!running) {
      this.replaySource = replaySource;
    }
  }

  /**
   * Adds a new data receiver to process real or replayed data. This method only
   * works during setup before starting to log.
   */
  public void addDataReceiver(LogDataReceiver dataReceiver) {
    if (!running) {
      receiverThread.addDataReceiver(dataReceiver);
    }
  }

  /**
   * Adds a new raw data receiver to process real or replayed data. This method
   * only works during setup before starting to log.
   */
  public void addDataReceiver(LogRawDataReceiver dataReceiver) {
    if (!running) {
      receiverThread.addDataReceiver(dataReceiver);
    }
  }

  /**
   * Records a metadata value. This method only works during setup before starting
   * to log, then data will be recorded during the first cycle.
   * 
   * @param key   The name used to identify this metadata field.
   * @param value The value of the metadata field.
   */
  public void recordMetadata(String key, String value) {
    if (!running) {
      metadata.put(key, value);
    }
  }

  /**
   * Returns whether a replay source is currently being used.
   */
  public boolean hasReplaySource() {
    return replaySource != null;
  }

  /**
   * Starts running the logging system, including any data receivers or the replay
   * source.
   */
  public void start() {
    if (!running) {
      running = true;
      entry = null;

      // Start replay source
      if (replaySource != null) {
        replaySource.start();
      }

      // Start receiver thread
      receiverThread.start();

      // Run first periodic cycle
      periodic();

      // Record metadata
      LogTable metadataTable = entry.getSubtable(replaySource == null ? "RealMetadata" : "ReplayMetadata");
      for (Map.Entry<String, String> item : metadata.entrySet()) {
        metadataTable.put(item.getKey(), item.getValue());
      }
    }
  }

  /**
   * Ends the logging system, including any data receivers or the replay source.
   */
  public void end() {
    if (running) {
      running = false;
      if (replaySource != null) {
        replaySource.end();
      }
      receiverThread.interrupt();
    }
  }

  /**
   * Periodic method to be called before robotInit and each loop cycle. Updates
   * timestamp and globally logged data.
   */
  public void periodic() {
    if (running) {
      double periodicStart = getRealTimestamp();
      if (entry != null) {
        try {
          receiverQueue.put(entry);
        } catch (InterruptedException exception) {
          return; // Main thread interrupted
        }
      }

      // Get next entry
      double captureStart = getRealTimestamp();
      ConduitApi conduit = ConduitApi.getInstance();
      conduit.captureData();
      if (replaySource == null) {
        entry = new LogTable(conduit.getTimestamp() / 1000000.0);
        outputTable = entry.getSubtable("RealOutputs");
      } else {
        entry = replaySource.getEntry();
        if (entry == null) {
          end();
          System.exit(0);
        }
        outputTable = entry.getSubtable("ReplayOutputs");
      }

      // Update default inputs
      double driverStationStart = getRealTimestamp();
      LoggedDriverStation.getInstance().periodic();
      double systemStatsStart = getRealTimestamp();
      processInputs("SystemStats", LoggedSystemStats.getInstance());
      double networkTablesStart = getRealTimestamp();
      processInputs("NetworkTables", LoggedNetworkTables.getInstance());
      double periodicEnd = getRealTimestamp();

      // Print timing data
      if (debugTiming && getRealTimestamp() > lastDebugPrint + 0.5) {
        lastDebugPrint = getRealTimestamp();
        String receiveLength = Double.toString((double) Math.round((captureStart - periodicStart) * 100000) / 100);
        String captureLength = Double.toString((double) Math.round((driverStationStart - captureStart) * 100000) / 100);
        String driverStationLength = Double
            .toString((double) Math.round((systemStatsStart - driverStationStart) * 100000) / 100);
        String systemStatsLength = Double
            .toString((double) Math.round((networkTablesStart - systemStatsStart) * 100000) / 100);
        String networkTablesLength = Double
            .toString((double) Math.round((periodicEnd - networkTablesStart) * 100000) / 100);
        System.out.println("RE=" + receiveLength + ", CA=" + captureLength + ", DS="
            + driverStationLength + ", SS=" + systemStatsLength + ", NT="
            + networkTablesLength + ", #=" + Integer.toString(receiverQueue.size()));
      }
    } else {
      // Retrieve new driver station data even if logger is disabled
      LoggedDriverStation.getInstance().periodic();
    }
  }

  /**
   * Returns the current FPGA timestamp or replayed time based on the current log
   * entry.
   */
  public double getTimestamp() {
    if (entry == null) {
      return 0.0;
    } else {
      return entry.getTimestamp();
    }
  }

  /**
   * Returns the true FPGA timestamp, regardless of the timestamp used for
   * logging. Useful for analyzing performance. DO NOT USE this method for any
   * logic which might need to be replayed.
   */
  public double getRealTimestamp() {
    return HALUtil.getFPGATime() / 1000000.0;
  }

  /**
   * Processes a set of inputs, logging them on the real robot or updating them in
   * the simulator. This should be called every loop cycle after updating the
   * inputs from the hardware (if applicable).
   * 
   * @param key    The name used to identify this set of inputs.
   * @param inputs The inputs to log or update.
   */
  public void processInputs(String key, LoggableInputs inputs) {
    if (running) {
      if (replaySource == null) {
        inputs.toLog(entry.getSubtable(key));
      } else {
        inputs.fromLog(entry.getSubtable(key));
      }
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, Boolean value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, boolean[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, Integer value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, int[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, Double value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, double[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, String value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, String[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, byte value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, byte[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }
}
