package org.littletonrobotics.junction;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.console.ConsoleSource;
import org.littletonrobotics.junction.console.RIOConsoleSource;
import org.littletonrobotics.junction.console.SimConsoleSource;
import org.littletonrobotics.junction.inputs.LoggableInputs;
import org.littletonrobotics.junction.inputs.LoggedDriverStation;
import org.littletonrobotics.junction.inputs.LoggedPowerDistribution;
import org.littletonrobotics.junction.inputs.LoggedSystemStats;
import org.littletonrobotics.junction.networktables.LoggedDashboardInput;

import edu.wpi.first.hal.HALUtil;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.util.WPIUtilJNI;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;

/** Central class for recording and replaying log data. */
public class Logger {
  private static final int receiverQueueCapcity = 500; // 10s at 50Hz

  private static Logger instance;

  private boolean running = false;
  private LogTable entry = new LogTable(0);
  private LogTable outputTable;
  private Map<String, String> metadata = new HashMap<>();
  private ConsoleSource console;
  private List<LoggedDashboardInput> dashboardInputs = new ArrayList<>();

  private LogReplaySource replaySource;
  private final BlockingQueue<LogTable> receiverQueue = new ArrayBlockingQueue<LogTable>(receiverQueueCapcity);
  private final ReceiverThread receiverThread = new ReceiverThread(receiverQueue);
  private boolean receiverQueueFault = false;

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
   * Registers a new dashboard input to be included in the periodic loop. This
   * function should not be called by the user.
   */
  public void registerDashboardInput(LoggedDashboardInput dashboardInput) {
    dashboardInputs.add(dashboardInput);
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

      // Start console capture
      if (RobotBase.isReal()) {
        console = new RIOConsoleSource();
      } else {
        console = new SimConsoleSource();
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

      // Enable mock time for WPIUtil
      WPIUtilJNI.setMockTime(getRealTimestamp());
      WPIUtilJNI.enableMockTime();

      // Start first periodic cycle
      periodicBeforeUser();
    }
  }

  /**
   * Ends the logging system, including any data receivers or the replay source.
   */
  public void end() {
    if (running) {
      running = false;
      try {
        console.close();
      } catch (Exception e) {
        DriverStation.reportError("Failed to stop console capture.", true);
      }
      if (replaySource != null) {
        replaySource.end();
      }
      receiverThread.interrupt();
      WPIUtilJNI.disableMockTime();
    }
  }

  /**
   * Periodic method to be called before robotInit and each loop cycle. Updates
   * timestamp and globally logged data.
   */
  void periodicBeforeUser() {
    if (running) {

      // Capture conduit data
      ConduitApi conduit = ConduitApi.getInstance();
      long conduitCaptureStart = getRealTimestamp();
      conduit.captureData();
      long conduitCaptureEnd = getRealTimestamp();

      // Get next entry
      if (replaySource == null) {
        entry.setTimestamp(conduit.getTimestamp());
      } else {
        if (!replaySource.updateTable(entry)) {
          end();
          System.exit(0);
        }
      }

      // Set mock time for WPIUtil
      WPIUtilJNI.setMockTime(entry.getTimestamp());

      // Update default inputs
      long saveDataStart = getRealTimestamp();
      LoggedDriverStation.getInstance().periodic();
      LoggedSystemStats.getInstance().periodic();
      LoggedPowerDistribution loggedPowerDistribution = LoggedPowerDistribution.getInstance();
      if (loggedPowerDistribution != null) {
        loggedPowerDistribution.periodic();
      }
      for (int i = 0; i < dashboardInputs.size(); i++) {
        dashboardInputs.get(i).periodic();
      }
      long saveDataEnd = getRealTimestamp();

      // Log output data
      recordOutput("Logger/ConduitPeriodicMS", (conduitCaptureEnd - conduitCaptureStart) / 1000.0);
      recordOutput("Logger/SavePeriodicMS", (saveDataEnd - saveDataStart) / 1000.0);
      recordOutput("Logger/QueuedCycles", receiverQueue.size());
    } else {
      // Retrieve new data even if logger is disabled
      ConduitApi.getInstance().captureData();
      LoggedDriverStation.getInstance().periodic();
      LoggedPowerDistribution loggedPowerDistribution = LoggedPowerDistribution.getInstance();
      if (loggedPowerDistribution != null) {
        loggedPowerDistribution.periodic();
      }
      LoggedSystemStats.getInstance().periodic();
    }
  }

  /**
   * Periodic method to be called after robotInit and each loop cycle. Sends data
   * to data receivers. Running this after user code allows IO operations to
   * occur between cycles rather than interferring with the main thread.
   */
  void periodicAfterUser() {
    if (running) {
      try {
        // Update console output
        long consoleCaptureStart = getRealTimestamp();
        String consoleData = console.getNewData();
        if (!consoleData.isEmpty()) {
          recordOutput("Console", consoleData.trim());
        }
        long consoleCaptureEnd = getRealTimestamp();
        recordOutput("Logger/ConsolePeriodicMS", (consoleCaptureEnd - consoleCaptureStart) / 1000.0);

        // Send a copy of the data to the receivers. The original object will be
        // kept and updated with the next timestamp (and new data if replaying).
        receiverQueue.add(LogTable.clone(entry));
        receiverQueueFault = false;
      } catch (IllegalStateException exception) {
        receiverQueueFault = true;
        DriverStation.reportError("Capacity of receiver queue exceeded, data will NOT be logged.", false);
      }
    }
  }

  /**
   * Returns the state of the receiver queue fault. This is tripped when the
   * receiver queue fills up, meaning that data is no longer being saved.
   */
  public boolean getReceiverQueueFault() {
    return receiverQueueFault;
  }

  /**
   * Returns the current FPGA timestamp in microseconds or replayed time based on
   * the current log entry.
   */
  public long getTimestamp() {
    if (!running || entry == null) {
      return getRealTimestamp();
    } else {
      return entry.getTimestamp();
    }
  }

  /**
   * Returns the true FPGA timestamp in microseconds, regardless of the timestamp
   * used for logging. Useful for analyzing performance. DO NOT USE this method
   * for any logic which might need to be replayed.
   */
  public long getRealTimestamp() {
    return HALUtil.getFPGATime();
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
  public void recordOutput(String key, byte[] value) {
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
  public void recordOutput(String key, boolean value) {
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
  public void recordOutput(String key, long value) {
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
  public void recordOutput(String key, float value) {
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
  public void recordOutput(String key, double value) {
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
  public void recordOutput(String key, long[] value) {
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
  public void recordOutput(String key, float[] value) {
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
  public void recordOutput(String key, String[] value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * The poses are logged as a double array (x_1, y_1, rot_1, ...)
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, Pose2d... value) {
    double[] data = new double[value.length * 3];
    for (int i = 0; i < value.length; i++) {
      data[i * 3] = value[i].getX();
      data[i * 3 + 1] = value[i].getY();
      data[i * 3 + 2] = value[i].getRotation().getRadians();
    }
    recordOutput(key, data);
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * The poses are logged as a double array (x, y, z, w_rot, x_rot, y_rot,
   * z_rot, ...)
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, Pose3d... value) {
    double[] data = new double[value.length * 7];
    for (int i = 0; i < value.length; i++) {
      data[i * 7] = value[i].getX();
      data[i * 7 + 1] = value[i].getY();
      data[i * 7 + 2] = value[i].getZ();
      data[i * 7 + 3] = value[i].getRotation().getQuaternion().getW();
      data[i * 7 + 4] = value[i].getRotation().getQuaternion().getX();
      data[i * 7 + 5] = value[i].getRotation().getQuaternion().getY();
      data[i * 7 + 6] = value[i].getRotation().getQuaternion().getZ();
    }
    recordOutput(key, data);
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * The trajectory is logged as a series of poses.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, Trajectory value) {
    recordOutput(key, value.getStates().stream().map(state -> state.poseMeters).toArray(Pose2d[]::new));
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * The modules are logged as a double array (angle_1, speed_1, angle_2, speed_2,
   * ...)
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, SwerveModuleState... value) {
    double[] data = new double[value.length * 2];
    for (int i = 0; i < value.length; i++) {
      data[i * 2] = value[i].angle.getRadians();
      data[i * 2 + 1] = value[i].speedMetersPerSecond;
    }
    recordOutput(key, data);
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * The current position of the Mechanism2d is logged once as a set of nested
   * fields. If the position is updated, this method must be called again.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public void recordOutput(String key, Mechanism2d value) {
    if (running) {
      try {
        // Use reflection because we don't explicitly depend on the shimmed classes
        Mechanism2d.class.getMethod("akitLog", LogTable.class).invoke(value, outputTable.getSubtable(key));
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
          | SecurityException e) {
        e.printStackTrace();
      }
    }
  }
}
