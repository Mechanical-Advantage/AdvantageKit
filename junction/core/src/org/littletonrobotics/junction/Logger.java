package org.littletonrobotics.junction;

import java.lang.reflect.Field;
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

import edu.wpi.first.hal.FRCNetComm.tInstances;
import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.HALUtil;
import edu.wpi.first.math.MathShared;
import edu.wpi.first.math.MathSharedStore;
import edu.wpi.first.math.MathUsageId;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;

import edu.wpi.first.util.protobuf.Protobuf;
import edu.wpi.first.util.struct.Struct;
import us.hebi.quickbuf.ProtoMessage;

/** Central class for recording and replaying log data. */
public class Logger {
  private static final int receiverQueueCapcity = 500; // 10s at 50Hz

  private static boolean running = false;
  private static LogTable entry = new LogTable(0);
  private static LogTable outputTable;
  private static Map<String, String> metadata = new HashMap<>();
  private static ConsoleSource console;
  private static List<LoggedDashboardInput> dashboardInputs = new ArrayList<>();
  private static boolean deterministicTimestamps = true;

  private static LogReplaySource replaySource;
  private static final BlockingQueue<LogTable> receiverQueue = new ArrayBlockingQueue<LogTable>(receiverQueueCapcity);
  private static final ReceiverThread receiverThread = new ReceiverThread(receiverQueue);
  private static boolean receiverQueueFault = false;

  private Logger() {
  }

  private static Logger instance = new Logger();

  /** @deprecated Call the static methods directly. */
  @Deprecated
  public static Logger getInstance() {
    return instance;
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
  public static void registerDashboardInput(LoggedDashboardInput dashboardInput) {
    dashboardInputs.add(dashboardInput);
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
   * Causes the timestamp returned by "Timer.getFPGATimestamp()" and similar to
   * match the "real" time as reported by the FPGA instead of the logged time from
   * AdvantageKit.
   * 
   * <p>
   * Not recommended for most users as the behavior of the replayed
   * code will NOT match the real robot. Only use this method if your control
   * logic requires precise timestamps WITHIN a single cycle and you have no way
   * to move timestamp-critical operations to an IO interface. Also consider using
   * "getRealTimestamp()" for logic that doesn't need to match the replayed
   * version (like for analyzing performance).
   */
  public static void disableDeterministicTimestamps() {
    deterministicTimestamps = false;
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

      // Update MathShared to mock timestamp
      setMathShared(true);

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
      try {
        console.close();
      } catch (Exception e) {
        DriverStation.reportError("Failed to stop console capture.", true);
      }
      if (replaySource != null) {
        replaySource.end();
      }
      receiverThread.interrupt();
      setMathShared(false);
    }
  }

  /**
   * Periodic method to be called before robotInit and each loop cycle. Updates
   * timestamp and globally logged data.
   */
  static void periodicBeforeUser() {
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

      // Update default inputs
      long saveDataStart = getRealTimestamp();
      LoggedDriverStation.periodic();
      LoggedSystemStats.periodic();
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
      LoggedDriverStation.periodic();
      LoggedPowerDistribution loggedPowerDistribution = LoggedPowerDistribution.getInstance();
      if (loggedPowerDistribution != null) {
        loggedPowerDistribution.periodic();
      }
      LoggedSystemStats.periodic();
    }
  }

  /**
   * Periodic method to be called after robotInit and each loop cycle. Sends data
   * to data receivers. Running this after user code allows IO operations to
   * occur between cycles rather than interferring with the main thread.
   */
  static void periodicAfterUser() {
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
   * Updates the MathShared object for wpimath to enable or disable AdvantageKit's
   * mocked timestamps.
   */
  private static void setMathShared(boolean mocked) {
    MathSharedStore.setMathShared(
        new MathShared() {
          @Override
          public void reportError(String error, StackTraceElement[] stackTrace) {
            DriverStation.reportError(error, stackTrace);
          }

          @Override
          public void reportUsage(MathUsageId id, int count) {
            switch (id) {
              case kKinematics_DifferentialDrive:
                HAL.report(
                    tResourceType.kResourceType_Kinematics,
                    tInstances.kKinematics_DifferentialDrive);
                break;
              case kKinematics_MecanumDrive:
                HAL.report(
                    tResourceType.kResourceType_Kinematics, tInstances.kKinematics_MecanumDrive);
                break;
              case kKinematics_SwerveDrive:
                HAL.report(
                    tResourceType.kResourceType_Kinematics, tInstances.kKinematics_SwerveDrive);
                break;
              case kTrajectory_TrapezoidProfile:
                HAL.report(tResourceType.kResourceType_TrapezoidProfile, count);
                break;
              case kFilter_Linear:
                HAL.report(tResourceType.kResourceType_LinearFilter, count);
                break;
              case kOdometry_DifferentialDrive:
                HAL.report(
                    tResourceType.kResourceType_Odometry, tInstances.kOdometry_DifferentialDrive);
                break;
              case kOdometry_SwerveDrive:
                HAL.report(tResourceType.kResourceType_Odometry, tInstances.kOdometry_SwerveDrive);
                break;
              case kOdometry_MecanumDrive:
                HAL.report(tResourceType.kResourceType_Odometry, tInstances.kOdometry_MecanumDrive);
                break;
              case kController_PIDController2:
                HAL.report(tResourceType.kResourceType_PIDController2, count);
                break;
              case kController_ProfiledPIDController:
                HAL.report(tResourceType.kResourceType_ProfiledPIDController, count);
                break;
              default:
                break;
            }
          }

          @Override
          public double getTimestamp() {
            return (mocked ? Logger.getTimestamp() : Logger.getRealTimestamp()) * 1.0e-6;
          }
        });
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
    if (!running || entry == null || !deterministicTimestamps) {
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
  public static long getRealTimestamp() {
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
   * This method serializes a single object as a protobuf. Protobuf should only be
   * used for objects that do not support struct serialization. Example usage:
   * {@code recordOutput("MyPose", Pose2d.proto, new Pose2d())}
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  @SuppressWarnings("unchecked")
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
   * This method serializes a single object as a struct or protobuf automatically
   * by searching for a {@code struct} or {@code proto} field. Struct is preferred
   * if both methods are supported.
   * 
   * @param T     The type
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  @SuppressWarnings("unchecked")
  public static <T> void recordOutput(String key, T value) {
    if (running) {
      outputTable.put(key, value);
    }
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * This method serializes an array of objects as a struct automatically
   * by searching for a {@code struct} field. Top-level protobuf arrays are not
   * supported.
   * 
   * @param T     The type
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  @SuppressWarnings("unchecked")
  public static <T> void recordOutput(String key, T... value) {
    if (running) {
      outputTable.put(key, value);
    }
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
  public static void recordOutput(String key, Trajectory value) {
    recordOutput(key, Pose2d.struct, value.getStates().stream().map(state -> state.poseMeters).toArray(Pose2d[]::new));
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, SwerveModuleState... value) {
    recordOutput(key, CustomStructs.swerveModuleState, value);
  }

  /**
   * Records a single output field for easy access when viewing the log. On the
   * simulator, use this method to record extra data based on the original inputs.
   * 
   * <p>
   * The current position of the Mechanism2d is logged once as a set of nested
   * fields. If the position is updated, this method must be called again.
   * 
   * @param key   The name of the field to record. It will be stored under
   *              "/RealOutputs" or "/ReplayOutputs"
   * @param value The value of the field.
   */
  public static void recordOutput(String key, Mechanism2d value) {
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
