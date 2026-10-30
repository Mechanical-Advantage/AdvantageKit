// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.wpilog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.Random;
import org.littletonrobotics.junction.LogDataReceiver;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LogValue;
import org.littletonrobotics.junction.LogTable.LoggableType;
import org.littletonrobotics.junction.Logger;
import org.wpilib.datalog.DataLogWriter;
import org.wpilib.driverstation.DriverStationErrors;
import org.wpilib.driverstation.MatchType;
import org.wpilib.framework.RobotBase;
import org.wpilib.system.RobotController;
import org.wpilib.util.Alert;
import org.wpilib.util.Alert.Level;

/** Records log values to a WPILOG file. */
public class WPILOGWriter implements LogDataReceiver {
  // Wait several seconds after DS attached to ensure time is updated
  private static final double timestampUpdateDelayNs = 5_000_000_000L;
  private static final long flushPeriodNs = 250_000_000L;
  private static final long mountCheckPeriodNs = 250_000_000L;
  private static final long mountTimeoutNs = 15_000_000_000L;
  private static final String defaultPathRobot = "/U/logs";
  private static final String defaultPathSim = "logs";
  private static final DateTimeFormatter timeFormatter =
      DateTimeFormatter.ofPattern("yy-MM-dd_HH-mm-ss");
  private static final String advantageScopeFileName = "ascope-log-path.txt";

  private String folder;
  private String filename;
  private final Optional<String> usbPrefix;
  private final String randomIdentifier;
  private OptionalLong dsAttachedTime;

  private boolean autoRename;
  private LocalDateTime logDate;
  private String logMatchText = "";

  private DataLogWriter log;
  private boolean isOpen = false;
  private boolean waitingForMount = false;
  private long mountWaitStartTime = 0;
  private long lastMountCheckTimestamp = 0;
  private final Queue<LogTable> pendingTables = new ArrayDeque<>();
  private FileOutputStream fileOutputStream;
  private final AdvantageScopeOpenBehavior openBehavior;
  private LogTable lastTable;
  private int timestampID;
  private long lastFlushTimestamp = 0;
  private Map<String, Integer> entryIDs;
  private Map<String, LoggableType> entryTypes;
  private Map<String, String> entryUnits;

  private boolean failedFault = false;
  private static final Alert failedAlert =
      new Alert(
          "AdvantageKit",
          "wpilog-failed",
          "Failed to start WPILOG logging, no data will be saved.",
          Level.MEDIUM);

  /**
   * Create a new WPILOGWriter for writing to a ".wpilog" file.
   *
   * @param path Path to log file or folder. If only a folder is provided, the filename will be
   *     generated based on the current time and match number (if applicable).
   * @param openBehavior Whether to automatically open the log file in AdvantageScope. See {@link
   *     org.littletonrobotics.junction.wpilog.WPILOGWriter.AdvantageScopeOpenBehavior
   *     AdvantageScopeOpenBehavior} for details.
   */
  public WPILOGWriter(String path, AdvantageScopeOpenBehavior openBehavior) {
    this.openBehavior = openBehavior;

    // Create random identifier
    Random random = new Random();
    StringBuilder randomIdentifierBuilder = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      randomIdentifierBuilder.append(String.format("%04x", random.nextInt(0x10000)));
    }
    randomIdentifier = randomIdentifierBuilder.toString();

    // Set up folder and filename
    if (path.endsWith(".wpilog")) {
      File pathFile = new File(path);
      folder = pathFile.getParent();
      filename = pathFile.getName();
      autoRename = false;
    } else {
      folder = path;
      filename = "akit_" + randomIdentifier + ".wpilog";
      autoRename = true;
    }
    usbPrefix = USBDriveUtil.getUSBDrivePrefix(folder);
  }

  /**
   * Create a new WPILOGWriter for writing to a ".wpilog" file.
   *
   * @param path Path to log file or folder. If only a folder is provided, the filename will be
   *     generated based on the current time and match number (if applicable).
   */
  public WPILOGWriter(String path) {
    this(path, AdvantageScopeOpenBehavior.AUTO);
  }

  /**
   * Create a new WPILOGWriter for writing to a ".wpilog" file.
   *
   * <p>The logs will be saved to "/U/logs" on the robot and "logs" in sim. The filename will be
   * generated based on the current time and match number (if applicable).
   *
   * @param openBehavior Whether to automatically open the log file in AdvantageScope. See {@link
   *     org.littletonrobotics.junction.wpilog.WPILOGWriter.AdvantageScopeOpenBehavior
   *     AdvantageScopeOpenBehavior} for details.
   */
  public WPILOGWriter(AdvantageScopeOpenBehavior openBehavior) {
    this(RobotBase.isSimulation() ? defaultPathSim : defaultPathRobot, openBehavior);
  }

  /**
   * Create a new WPILOGWriter for writing to a ".wpilog" file.
   *
   * <p>The logs will be saved to "/U/logs" on the robot and "logs" in sim. The filename will be
   * generated based on the current time and match number (if applicable).
   */
  public WPILOGWriter() {
    this(
        RobotBase.isSimulation() ? defaultPathSim : defaultPathRobot,
        AdvantageScopeOpenBehavior.AUTO);
  }

  private static void syncDirectory(String dirPath) {
    if (dirPath == null) {
      return;
    }
    try (FileChannel dirChannel = FileChannel.open(Path.of(dirPath), StandardOpenOption.READ)) {
      dirChannel.force(true);
    } catch (Exception ignored) {
    }
  }

  public void start() {
    if (usbPrefix.isPresent()) {
      if (USBDriveUtil.isUSBMounted(usbPrefix.get())) {
        openLogFile();
      } else {
        waitingForMount = true;
        mountWaitStartTime = RobotController.getMonotonicTime();
        lastMountCheckTimestamp = 0;
        System.out.println(
            "[AdvantageKit] Waiting for USB drive to mount before opening log at \""
                + folder
                + "\"...");
      }
    } else {
      openLogFile();
    }
  }

  private void openLogFile() {
    // Create folder if necessary
    if (folder != null) {
      File logFolder = new File(folder);
      if (!logFolder.exists()) {
        logFolder.mkdirs();
      }
    }

    // Delete log if it already exists
    File logFile = folder == null ? new File(filename) : new File(folder, filename);
    if (logFile.exists()) {
      logFile.delete();
    }

    // Create new log
    String logPath =
        folder == null ? filename : FileSystems.getDefault().getPath(folder, filename).toString();
    System.out.println("[AdvantageKit] Logging to \"" + logPath + "\"");
    try {
      log = new DataLogWriter(logPath, WPILOGConstants.extraHeader);
      if (RobotBase.isReal()) {
        fileOutputStream = new FileOutputStream(logPath, true);
        syncDirectory(folder);
      }
    } catch (IOException e) {
      DriverStationErrors.reportError(
          "[AdvantageKit] Failed to open output log file, no data will be saved.", true);
      failedFault = true;
      failedAlert.set(true);
      return;
    }
    isOpen = true;
    waitingForMount = false;
    timestampID =
        log.start(
            timestampKey,
            LoggableType.Integer.getWPILOGType(),
            WPILOGConstants.entryMetadataUnits.replace("$UNITSTR", "nanoseconds"),
            0);
    lastTable = new LogTable(0);

    // Reset data
    entryIDs = new HashMap<>();
    entryTypes = new HashMap<>();
    entryUnits = new HashMap<>();
    logDate = null;
    logMatchText = null;
  }

  public void end() {
    if (waitingForMount) {
      waitingForMount = false;
      pendingTables.clear();
      return;
    }

    if (!isOpen) {
      return;
    }

    log.close();
    if (fileOutputStream != null) {
      try {
        fileOutputStream.getFD().sync();
        fileOutputStream.close();
        syncDirectory(folder);
      } catch (IOException e) {
      }
    }

    // Send log path to AdvantageScope
    boolean shouldOpen =
        switch (openBehavior) {
          case ALWAYS -> RobotBase.isSimulation();
          case AUTO -> RobotBase.isSimulation() && Logger.hasReplaySource();
          case NEVER -> false;
        };
    if (shouldOpen) {
      try {
        String fullLogPath =
            folder == null
                ? FileSystems.getDefault().getPath(filename).normalize().toAbsolutePath().toString()
                : FileSystems.getDefault()
                    .getPath(folder, filename)
                    .normalize()
                    .toAbsolutePath()
                    .toString();
        Path advantageScopeTempPath =
            Paths.get(System.getProperty("java.io.tmpdir"), advantageScopeFileName);
        PrintWriter writer = new PrintWriter(advantageScopeTempPath.toString(), "UTF-8");
        writer.println(fullLogPath);
        writer.close();
        System.out.println("[AdvantageKit] Log sent to AdvantageScope.");
      } catch (Exception e) {
        DriverStationErrors.reportError(
            "[AdvantageKit] Failed to send log to AdvantageScope.", false);
      }
    }
  }

  public void putTable(LogTable table) {
    if (waitingForMount) {
      pendingTables.add(table);
      long now = table.getTimestamp();
      if (now - lastMountCheckTimestamp > mountCheckPeriodNs) {
        lastMountCheckTimestamp = now;
        if (USBDriveUtil.isUSBMounted(usbPrefix.get())) {
          openLogFile();
          if (isOpen) {
            while (!pendingTables.isEmpty()) {
              putTableInternal(pendingTables.poll());
            }
          } else {
            waitingForMount = false;
            pendingTables.clear();
          }
        } else if (RobotController.getMonotonicTime() - mountWaitStartTime > mountTimeoutNs) {
          waitingForMount = false;
          pendingTables.clear();
          DriverStationErrors.reportWarning(
              "[AdvantageKit] Timed out waiting for USB drive at \""
                  + folder
                  + "\", no data will be saved.",
              false);
          failedFault = true;
          failedAlert.set(true);
        }
      }
      return;
    }

    // Exit if log not open
    if (!isOpen) return;

    putTableInternal(table);
  }

  private void putTableInternal(LogTable table) {
    // Auto rename
    if (autoRename) {

      // Update timestamp
      if (logDate == null) {
        if ((table.get("DriverStation/DSAttached", false)
                && table.get("SystemStats/EpochTimeValid", false))
            || RobotBase.isSimulation()) {
          if (dsAttachedTime == null) {
            dsAttachedTime = OptionalLong.of(RobotController.getMonotonicTime());
          } else if (RobotController.getMonotonicTime() - dsAttachedTime.getAsLong()
                  > timestampUpdateDelayNs
              || RobotBase.isSimulation()) {
            logDate = LocalDateTime.now();
          }
        } else {
          dsAttachedTime = null;
        }
      }

      // Update match text
      boolean fmsAttached = table.get("DriverStation/FMSAttached", false);
      if (fmsAttached) {
        StringBuilder matchTextBuilder = new StringBuilder();
        String eventName = table.get("DriverStation/EventName", "").toLowerCase();
        if (eventName.length() > 0) {
          matchTextBuilder.append("_");
          matchTextBuilder.append(eventName);
        }
        MatchType matchType;
        switch (table.get("DriverStation/MatchType", 0)) {
          case 1:
            matchType = MatchType.PRACTICE;
            break;
          case 2:
            matchType = MatchType.QUALIFICATION;
            break;
          case 3:
            matchType = MatchType.ELIMINATION;
            break;
          default:
            matchType = MatchType.NONE;
            break;
        }
        if (matchType != MatchType.NONE) {
          switch (matchType) {
            case PRACTICE:
              matchTextBuilder.append("_p");
              break;
            case QUALIFICATION:
              matchTextBuilder.append("_q");
              break;
            case ELIMINATION:
              matchTextBuilder.append("_e");
              break;
            default:
              break;
          }
          matchTextBuilder.append(table.get("DriverStation/MatchNumber", 0));
        }
        logMatchText = matchTextBuilder.toString();
      }

      // Update filename
      StringBuilder newFilenameBuilder = new StringBuilder();
      newFilenameBuilder.append("akit_");
      if (logDate == null) {
        newFilenameBuilder.append(randomIdentifier);
      } else {
        newFilenameBuilder.append(timeFormatter.format(logDate));
      }
      if (logMatchText != null) {
        newFilenameBuilder.append(logMatchText);
      }
      newFilenameBuilder.append(".wpilog");
      String newFilename = newFilenameBuilder.toString();
      if (!newFilename.equals(filename)) {
        String logPath =
            folder == null
                ? newFilename
                : FileSystems.getDefault().getPath(folder, newFilename).toString();
        System.out.println("[AdvantageKit] Renaming log to \"" + logPath + "\"");

        File fileA = folder == null ? new File(filename) : new File(folder, filename);
        File fileB = folder == null ? new File(newFilename) : new File(folder, newFilename);
        if (fileA.renameTo(fileB)) {
          filename = newFilename;
          if (RobotBase.isReal()) {
            if (fileOutputStream != null) {
              try {
                fileOutputStream.getFD().sync();
                fileOutputStream.close();
              } catch (IOException ignored) {
              }
            }
            try {
              fileOutputStream = new FileOutputStream(logPath, true);
            } catch (IOException ignored) {
              fileOutputStream = null;
            }
            syncDirectory(folder);
          }
        }
      }
    }

    // Save timestamp
    log.appendInteger(timestampID, table.getTimestamp(), table.getTimestamp());

    // Get new and old data
    Map<String, LogValue> newMap = table.getAll(false);
    Map<String, LogValue> oldMap = lastTable.getAll(false);

    // Encode fields
    for (Map.Entry<String, LogValue> field : newMap.entrySet()) {

      // Check if field should be updated
      LoggableType type = field.getValue().type;
      String unit = field.getValue().unitStr;
      boolean appendData = false;
      if (!entryIDs.containsKey(field.getKey())) { // New field
        String metadata =
            unit == null
                ? WPILOGConstants.entryMetadata
                : WPILOGConstants.entryMetadataUnits.replace("$UNITSTR", unit);
        entryIDs.put(
            field.getKey(),
            log.start(
                field.getKey(), field.getValue().getWPILOGType(), metadata, table.getTimestamp()));
        entryTypes.put(field.getKey(), type);
        if (unit != null) {
          entryUnits.put(field.getKey(), unit);
        }
        appendData = true;
      } else if (!field.getValue().equals(oldMap.get(field.getKey()))) { // Updated field
        appendData = true;
      }

      // Append data
      if (appendData) {
        int id = entryIDs.get(field.getKey());

        // Check if unit changed
        if (unit != null && !unit.equals(entryUnits.get(field.getKey()))) {
          log.setMetadata(
              id,
              WPILOGConstants.entryMetadataUnits.replace("$UNITSTR", unit),
              table.getTimestamp());
          entryUnits.put(field.getKey(), unit);
        }

        // Add field value
        switch (field.getValue().type) {
          case Raw:
            log.appendRaw(id, field.getValue().getRaw(), table.getTimestamp());
            break;
          case Boolean:
            log.appendBoolean(id, field.getValue().getBoolean(), table.getTimestamp());
            break;
          case Integer:
            log.appendInteger(id, field.getValue().getInteger(), table.getTimestamp());
            break;
          case Float:
            log.appendFloat(id, field.getValue().getFloat(), table.getTimestamp());
            break;
          case Double:
            log.appendDouble(id, field.getValue().getDouble(), table.getTimestamp());
            break;
          case String:
            log.appendString(id, field.getValue().getString(), table.getTimestamp());
            break;
          case BooleanArray:
            log.appendBooleanArray(id, field.getValue().getBooleanArray(), table.getTimestamp());
            break;
          case IntegerArray:
            log.appendIntegerArray(id, field.getValue().getIntegerArray(), table.getTimestamp());
            break;
          case FloatArray:
            log.appendFloatArray(id, field.getValue().getFloatArray(), table.getTimestamp());
            break;
          case DoubleArray:
            log.appendDoubleArray(id, field.getValue().getDoubleArray(), table.getTimestamp());
            break;
          case StringArray:
            log.appendStringArray(id, field.getValue().getStringArray(), table.getTimestamp());
            break;
        }
      }
    }

    // Flush to disk
    log.flush();
    if (fileOutputStream != null && table.getTimestamp() - lastFlushTimestamp > flushPeriodNs) {
      lastFlushTimestamp = table.getTimestamp();
      try {
        fileOutputStream.getFD().sync();
        syncDirectory(folder);
      } catch (IOException e) {
      }
    }

    // Update last table
    lastTable = table;
  }

  /**
   * Returns the state of the general "failed" fault. This is tripped when the log file fails to
   * open due to a general IO error or USB timeout.
   *
   * @return Whether the fault is active.
   */
  public boolean getFailedFault() {
    return failedFault;
  }

  /**
   * The behavior to use when sending the log file to AdvantageScope after the robot program exits
   * in simulation.
   */
  public static enum AdvantageScopeOpenBehavior {
    /** Always open the log file in AdvantageScope when running in sim. */
    ALWAYS,

    /** Open the log file in AdvantageScope when running in replay. */
    AUTO,

    /** Never open the log file in AdvantageScope */
    NEVER
  }
}
