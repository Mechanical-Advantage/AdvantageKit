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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable.LogValue;
import org.littletonrobotics.junction.console.ConsoleSource;
import org.littletonrobotics.junction.console.RIOConsoleSource;
import org.littletonrobotics.junction.console.SimConsoleSource;
import org.littletonrobotics.junction.inputs.LoggableInputs;
import org.littletonrobotics.junction.inputs.LoggedDriverStation;
import org.littletonrobotics.junction.inputs.LoggedPowerDistribution;
import org.littletonrobotics.junction.inputs.LoggedSystemStats;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;
import org.littletonrobotics.junction.networktables.LoggedNetworkInput;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Unit;
import edu.wpi.first.util.protobuf.Protobuf;
import edu.wpi.first.util.struct.Struct;
import edu.wpi.first.util.struct.StructSerializable;
import edu.wpi.first.util.WPISerializable;
import us.hebi.quickbuf.ProtoMessage;

/** Central class for recording and replaying log data. */
public class Logger {
  private static final int receiverQueueCapcity = 500; // 10s at 50Hz

  private static boolean running = false;
  private static long cycleCount = 0;
  private static LogTable entry = new LogTable(0);
  private static LogTable outputTable;
  private static Map<String, String> metadata = new HashMap<>();
  private static ConsoleSource console = null;
  private static List<LoggedNetworkInput> dashboardInputs = new ArrayList<>();
  private static Supplier<ByteBuffer[]> urclSupplier = null;
  private static boolean enableConsole = true;

  private static LogReplaySource replaySource;
  private static final BlockingQueue<LogTable> receiverQueue = new ArrayBlockingQueue<LogTable>(receiverQueueCapcity);
  private static final ReceiverThread receiverThread = new ReceiverThread(receiverQueue);
  private static boolean receiverQueueFault = false;

  private Logger() {
  }

  /**
   * Sets the source to use for replaying data. Use null to disable replay. This
   * method only works during setup before starting to log.
   */
  public static void setReplaySource(LogReplaySource replaySource) {
    if (!running) {
      Logger.replaySource = replaySource;
    }
  }

  /**
   * Adds a new data receiver to process real or replayed data. This method only
   * works during setup before starting to log.
   */
  public static void addDataReceiver(LogDataReceiver dataReceiver) {
    if (!running) {
      receiverThread.addDataReceiver(dataReceiver);
    }
  }

  /**
   * Registers a new dashboard input to be included in the periodic loop. This
   * function should not be called by the user.
   */
  public static void registerDashboardInput(LoggedNetworkInput dashboardInput) {
    dashboardInputs.add(dashboardInput);
  }

  /**
   * Registers a log supplier for <a href=
   * "https://github.com/Mechanical-Advantage/URCL">URCL</a> (Unofficial
   * REV-Compatible Logger). This method should be called during setup before
   * starting to log. Example
   * usage shown below.
   * 
   * <pre>
   * <code>Logger.registerURCL(URCL.startExternal());</code>
   * </pre>
   */
  public static void registerURCL(Supplier<ByteBuffer[]> logSupplier) {
    urclSupplier = logSupplier;
  }

  /**
   * Records a metadata value. This method only works during setup before starting
   * to log, then data will be recorded during the first cycle.
   * 
   * @param key   The name used to identify this metadata field.
   * @param value The value of the metadata field.
   */
  public static void recordMetadata(String key, String value) {
    if (!running) {
      metadata.put(key, value);
    }
  }

  /**
   * Disables automatic console capture.
   */
  public static void disableConsoleCapture() {
    enableConsole = false;
  }

  /**
   * Returns whether a replay source is currently being used.
   */
  public static boolean hasReplaySource() {
    return replaySource != null;
  }

  /**
   * Starts running the logging system, including any data receivers or the replay
   * source.
   */
  public static void start() {
    if (!running) {
      running = true;

      // Exit if LoggedRobot not present
      var stackTrace = Thread.currentThread().getStackTrace();
      boolean isValid = false;
      for (var element : stackTrace) {
        try {
          Class<?> elementClass = Class.forName(element.getClassName());
          if (LoggedRobot.class.isAssignableFrom(elementClass)) {
            isValid = true;
            break;
          }
        } catch (ClassNotFoundException e) {
        }
      }
      if (!isValid) {
        DriverStation.reportError(
            "The main robot class must inherit from LoggedRobot when using AdvantageKit. For more details, check the AdvantageKit installation documentation: https://docs.advantagekit.org/installation\n\n*** EXITING DUE TO INVALID ADVANTAGEKIT INSTALLATION, SEE ABOVE. ***",
            false);
        System.exit(1);
      }

      // Start console capture
      if (enableConsole) {
        if (RobotBase.isReal()) {
          console = new RIOConsoleSource();
        } else {
          console = new SimConsoleSource();
        }
      }

      // Start replay source
      if (replaySource != null) {
        replaySource.start();
      }

      // Create output table
      if (replaySource == null) {
        outputTable = entry.getSubtable("RealOutputs");
      } else {
        outputTable = entry.getSubtable("ReplayOutputs");
      }

      // Record metadata
      LogTable metadataTable = entry.getSubtable(replaySource == null ? "RealMetadata" : "ReplayMetadata");
      for (Map.Entry<String, String> item : metadata.entrySet()) {
        metadataTable.put(item.getKey(), item.getValue());
      }

      // Start receiver thread
      receiverThread.start();

      // Update RobotController to AdvantageKit timestamp
      RobotController.setTimeSource(Logger::getTimestamp);

      // Start first periodic cycle
      periodicBeforeUser();
    }
  }

  /**
   * Ends the logging system, including any data receivers or the replay source.
   */
  public static void end() {
    if (running) {
      running = false;
      if (console != null) {
        try {
          console.close();
        } catch (Exception e) {
          DriverStation.reportError("[AdvantageKit] Failed to stop console capture.", true);
        }
      }
      if (replaySource != null) {
        replaySource.end();
      }
      receiverThread.interrupt();
      try {
        receiverThread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      RobotController.setTimeSource(RobotController::getFPGATime);
    }
  }

  /**
   * Periodic method to be called during the constructor of Robot and each loop
   * cycle. Updates timestamp, replay entry, and dashboard inputs.
   */
  static void periodicBeforeUser() {
    cycleCount++;
    if (running) {
      // Get next entry
      long entryUpdateStart = RobotController.getFPGATime();
      if (replaySource == null) {
        synchronized (entry) {
          entry.setTimestamp(RobotController.getFPGATime());
        }
      } else {
        if (!replaySource.updateTable(entry)) {
          end();
          System.exit(0);
        }
      }

      // Update Driver Station
      long dsStart = RobotController.getFPGATime();
      if (hasReplaySource()) {
        LoggedDriverStation.replayFromLog(entry.getSubtable("DriverStation"));
      }

      // Update dashboard inputs
      long dashboardInputsStart = RobotController.getFPGATime();
      for (int i = 0; i < dashboardInputs.size(); i++) {
        dashboardInputs.get(i).periodic();
      }
      long dashboardInputsEnd = RobotController.getFPGATime();

      // Record timing data
      recordOutput("Logger/EntryUpdateMS", (dsStart - entryUpdateStart) / 1000.0);
      if (hasReplaySource()) {
        recordOutput("Logger/DriverStationMS", (dashboardInputsStart - dsStart) / 1000.0);
      }
      recordOutput("Logger/DashboardInputsMS", (dashboardInputsEnd - dashboardInputsStart) / 1000.0);
    }
  }

  /**
   * Periodic method to be called after the constructor of Robot and each loop
   * cycle. Updates default log values and sends data to data receivers. Running
   * this after user code allows IO operations to occur between cycles rather than
   * interferring with the main thread.
   */
  static void periodicAfterUser(long userCodeLength, long periodicBeforeLength) {
    if (running) {
      // Capture conduit data
      ConduitApi conduit = ConduitApi.getInstance();
      long conduitCaptureStart = RobotController.getFPGATime();
      conduit.captureData();

      // Update Driver Station
      long dsStart = RobotController.getFPGATime();
      if (!hasReplaySource()) {
        LoggedDriverStation.saveToLog(entry.getSubtable("DriverStation"));
      }

      // Save other conduit inputs
      long conduitSaveStart = RobotController.getFPGATime();
      if (!hasReplaySource()) {
        LoggedSystemStats.saveToLog(entry.getSubtable("SystemStats"));
        LoggedPowerDistribution loggedPowerDistribution = LoggedPowerDistribution.getInstance();
        if (loggedPowerDistribution != null) {
          loggedPowerDistribution.saveToLog(entry.getSubtable("PowerDistribution"));
        }
        if (urclSupplier != null && RobotBase.isReal()) {
          ByteBuffer[] buffers = urclSupplier.get();
          if (buffers.length == 3) {
            for (int i = 0; i < 3; i++) {
              buffers[i].rewind();
              byte[] bytes = new byte[buffers[i].remaining()];
              buffers[i].get(bytes);
              switch (i) {
                case 0:
                  entry.put("URCL/Raw/Persistent", new LogValue(bytes, "URCLr3_persistent"));
                  break;
                case 1:
                  entry.put("URCL/Raw/Periodic", new LogValue(bytes, "URCLr3_periodic"));
                  break;
                case 2:
                  entry.put("URCL/Raw/Aliases", new LogValue(bytes, "URCLr3_aliases"));
                  break;
              }
            }
          }
        }
      }

      // Update automatic outputs from user code
      long autoLogStart = RobotController.getFPGATime();
      AutoLogOutputManager.periodic();
      long alertLogStart = RobotController.getFPGATime();
      AlertLogger.periodic();
      long radioLogStart = RobotController.getFPGATime();
      if (!hasReplaySource()) {
        RadioLogger.periodic(entry.getSubtable("RadioStatus"));
      }
      long consoleCaptureStart = RobotController.getFPGATime();
      if (enableConsole) {
        String consoleData = console.getNewData();
        if (!consoleData.isEmpty()) {
          recordOutput("Console", consoleData.trim());
        }
      }
      long consoleCaptureEnd = RobotController.getFPGATime();

      // Record timing data
      recordOutput("Logger/ConduitCaptureMS", (dsStart - conduitCaptureStart) / 1000.0);
      if (!hasReplaySource()) {
        recordOutput("Logger/DriverStationMS", (conduitSaveStart - dsStart) / 1000.0);
      }
      recordOutput("Logger/ConduitSaveMS", (autoLogStart - conduitSaveStart) / 1000.0);
      recordOutput("Logger/AutoLogMS", (alertLogStart - autoLogStart) / 1000.0);
      recordOutput("Logger/AlertLogMS", (radioLogStart - alertLogStart) / 1000.0);
      recordOutput("Logger/RadioLogMS", (consoleCaptureStart - radioLogStart) / 1000.0);
      recordOutput("Logger/ConsoleMS", (consoleCaptureEnd - consoleCaptureStart) / 1000.0);
      recordOutput("LoggedRobot/UserCodeMS", userCodeLength / 1000.0);
      long periodicAfterLength = consoleCaptureEnd - conduitCaptureStart;
      recordOutput("LoggedRobot/LogPeriodicMS", (periodicBeforeLength + periodicAfterLength) / 1000.0);
      recordOutput("LoggedRobot/FullCycleMS", (periodicBeforeLength + userCodeLength + periodicAfterLength) / 1000.0);
      recordOutput("Logger/QueuedCycles", receiverQueue.size());

      try {
        // Send a copy of the data to the receivers. The original object will be
        // kept and updated with the next timestamp (and new data if replaying).
        receiverQueue.add(LogTable.clone(entry));
        receiverQueueFault = false;
      } catch (IllegalStateException exception) {
        receiverQueueFault = true;
        DriverStation.reportError("[AdvantageKit] Capacity of receiver queue exceeded, data will NOT be logged.",
            false);
      }
    }
  }

  /**
   * Returns the state of the receiver queue fault. This is tripped when the
   * receiver queue fills up, meaning that data is no longer being saved.
   */
  public static boolean getReceiverQueueFault() {
    return receiverQueueFault;
  }

  /**
   * Returns the current FPGA timestamp or replayed time based on the current log
   * entry (microseconds).
   */
  public static long getTimestamp() {
    synchronized (entry) {
      if (!running || entry == null) {
        return RobotController.getFPGATime();
      } else {
        return entry.getTimestamp();
      }
    }
  }

  /**
   * Returns the true FPGA timestamp in microseconds, regardless of the timestamp
   * used for logging. Useful for analyzing performance. DO NOT USE this method
   * for any logic which might need to be replayed.
   * 
   * @deprecated Use {@code RobotController.getFPGATime()} instead.
   */
  @Deprecated
  public static long getRealTimestamp() {
    return RobotController.getFPGATime();
  }

  /**
   * Runs the provided callback function every N loop cycles. This method can be
   * used
   * to update inputs or log outputs at a lower rate than the standard loop cycle.
   * 
   * <p>
   * <b>Note that this method must be called periodically to continue running the
   * callback function</b>.
   */
  public static void runEveryN(int n, Runnable function) {
    if (cycleCount % n == 0) {
      function.run();
    }
  }

  /**
   * Processes a set of inputs, logging them on the real robot or updating them in
   * the simulator. This should be called every loop cycle after updating the
   * inputs from the hardware (if applicable).
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key    The name used to identify this set of inputs.
   * @param inputs The inputs to log or update.
   */
  public static void processInputs(String key, LoggableInputs inputs) {
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
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, byte[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, byte[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, boolean value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, BooleanSupplier value) {
    if (running) {
      outputTable.put(key, value.getAsBoolean());
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, boolean[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, boolean[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, int value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, IntSupplier value) {
    if (running) {
      outputTable.put(key, value.getAsInt());
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, int[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, int[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, long value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, LongSupplier value) {
    if (running) {
      outputTable.put(key, value.getAsLong());
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, long[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, long[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, float value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, float[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, float[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, double value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, DoubleSupplier value) {
    if (running) {
      outputTable.put(key, value.getAsDouble());
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, double[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, double[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, String value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, String[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, String[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <E extends Enum<E>> void recordOutput(String key, E value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <E extends Enum<E>> void recordOutput(String key, E[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <E extends Enum<E>> void recordOutput(String key, E[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <U extends Unit> void recordOutput(String key, Measure<U> value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * <p>
   * This method serializes a single object as a struct. Example usage:
   * {@code recordOutput("MyPose", Pose2d.struct, new Pose2d())}
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <T> void recordOutput(String key, Struct<T> struct, T value) {
    if (running) {
      outputTable.put(key, struct, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method serializes an array of objects as a struct. Example usage:
   * {@code
   * recordOutput("MyPoses", Pose2d.struct, new Pose2d(), new Pose2d());
   * recordOutput("MyPoses", Pose2d.struct, new Pose2d[] {new Pose2d(), new
   * Pose2d()});
   * }
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  @SuppressWarnings("unchecked")
  public static <T> void recordOutput(String key, Struct<T> struct, T... value) {
    if (running) {
      outputTable.put(key, struct, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <T> void recordOutput(String key, Struct<T> struct, T[][] value) {
    if (running) {
      outputTable.put(key, struct, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method serializes a single object as a protobuf. Protobuf should only be
   * used for objects that do not support struct serialization. Example usage:
   * {@code recordOutput("MyPose", Pose2d.proto, new Pose2d())}
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <T, MessageType extends ProtoMessage<?>> void recordOutput(String key, Protobuf<T, MessageType> proto,
      T value) {
    if (running) {
      outputTable.put(key, proto, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method serializes a single object as a struct or protobuf automatically.
   * Struct is preferred if both methods are supported.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param <T>   The type
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <T extends WPISerializable> void recordOutput(String key, T value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method serializes an array of objects as a struct automatically.
   * Top-level protobuf arrays are not supported.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param <T>   The type
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  @SuppressWarnings("unchecked")
  public static <T extends StructSerializable> void recordOutput(String key, T... value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method serializes an array of objects as a struct automatically.
   * Top-level protobuf arrays are not supported.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param <T>   The type
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <T extends StructSerializable> void recordOutput(String key, T[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method serializes a single object as a struct or protobuf automatically.
   * Struct is preferred if both methods are supported.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param <R>   The type
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <R extends Record> void recordOutput(String key, R value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method serializes an array of objects as a struct automatically.
   * Top-level protobuf arrays are not supported.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param <R>   The type
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  @SuppressWarnings("unchecked")
  public static <R extends Record> void recordOutput(String key, R... value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method serializes an array of objects as a struct automatically.
   * Top-level protobuf arrays are not supported.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param <R>   The type
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static <R extends Record> void recordOutput(String key, R[][] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * The current position of the Mechanism2d is logged once as a set of nested
   * fields. If the position is updated, this method must be called again.
   * 
   * <p>
   * This method is <b>not thread-safe</b> and should only be called from the
   * main thread. See the "Common Issues" page in the documentation for more
   * details.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, LoggedMechanism2d value) {
    if (running) {
      value.logOutput(outputTable.getSubtable(key));
    }
  }
}
