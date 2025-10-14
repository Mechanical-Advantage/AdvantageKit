// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.wpilog;

import edu.wpi.first.util.datalog.DataLogWriter;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.MatchType;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotController;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.littletonrobotics.junction.LogDataReceiver;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LogValue;
import org.littletonrobotics.junction.LogTable.LoggableType;
import org.littletonrobotics.junction.Logger;

/** Records log values to a WPILOG file. */
public class WPILOGWriter implements LogDataReceiver {
  private static final double timestampUpdateDelay =
      5.0; // Wait several seconds after DS attached to ensure
  // timestamp/timezone is updated
  private static final String defaultPathRio = "/U/logs";
  private static final String defaultPathSim = "logs";
  private static final DateTimeFormatter timeFormatter =
      DateTimeFormatter.ofPattern("yy-MM-dd_HH-mm-ss");
  private static final String advantageScopeFileName = "ascope-log-path.txt";

  private String folder;
  private String filename;
  private final String randomIdentifier;
  private Double dsAttachedTime;

  private boolean autoRename;
  private LocalDateTime logDate;
  private String logMatchText;

  private DataLogWriter log;
  private boolean isOpen = false;
  private final AdvantageScopeOpenBehavior openBehavior;
  private LogTable lastTable;
  private int timestampID;
  private Map<String, Integer> entryIDs;
  private Map<String, LoggableType> entryTypes;
  private Map<String, String> entryUnits;

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
   * <p>The logs will be saved to "/U/logs" on the RIO and "logs" in sim. The filename will be
   * generated based on the current time and match number (if applicable).
   *
   * @param openBehavior Whether to automatically open the log file in AdvantageScope. See {@link
   *     org.littletonrobotics.junction.wpilog.WPILOGWriter.AdvantageScopeOpenBehavior
   *     AdvantageScopeOpenBehavior} for details.
   */
  public WPILOGWriter(AdvantageScopeOpenBehavior openBehavior) {
    this(RobotBase.isSimulation() ? defaultPathSim : defaultPathRio, openBehavior);
  }

  /**
   * Create a new WPILOGWriter for writing to a ".wpilog" file.
   *
   * <p>The logs will be saved to "/U/logs" on the RIO and "logs" in sim. The filename will be
   * generated based on the current time and match number (if applicable).
   */
  public WPILOGWriter() {
    this(
        RobotBase.isSimulation() ? defaultPathSim : defaultPathRio,
        AdvantageScopeOpenBehavior.AUTO);
  }

  public void start() {
    // Create folder if necessary
    File logFolder = new File(folder);
    if (!logFolder.exists()) {
      logFolder.mkdirs();
    }

    // Delete log if it already exists
    File logFile = new File(folder, filename);
    if (logFile.exists()) {
      logFile.delete();
    }

    // Create new log
    String logPath = Path.of(folder, filename).toString();
    System.out.println("[AdvantageKit] Logging to \"" + logPath + "\"");
    try {
      log = new DataLogWriter(logPath, WPILOGConstants.extraHeader);
    } catch (IOException e) {
      DriverStation.reportError("[AdvantageKit] Failed to open output log file.", true);
      return;
    }
    isOpen = true;
    timestampID =
        log.start(
            timestampKey, LoggableType.Integer.getWPILOGType(), WPILOGConstants.entryMetadata, 0);
    lastTable = new LogTable(0);

    // Reset data
    entryIDs = new HashMap<>();
    entryTypes = new HashMap<>();
    entryUnits = new HashMap<>();
    logDate = null;
    logMatchText = null;
  }

  public void end() {
    log.close();

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
            FileSystems.getDefault()
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
        DriverStation.reportError("[AdvantageKit] Failed to send log to AdvantageScope.", false);
      }
    }
  }

  public void putTable(LogTable table) {
    // Exit if log not open
    if (!isOpen) return;

    // Auto rename
    if (autoRename) {

      // Update timestamp
      if (logDate == null) {
        if ((table.get("DriverStation/DSAttached", false)
                && table.get("SystemStats/SystemTimeValid", false))
            || RobotBase.isSimulation()) {
          if (dsAttachedTime == null) {
            dsAttachedTime = RobotController.getFPGATime() / 1000000.0;
          } else if (RobotController.getFPGATime() / 1000000.0 - dsAttachedTime
                  > timestampUpdateDelay
              || RobotBase.isSimulation()) {
            logDate = LocalDateTime.now();
          }
        } else {
          dsAttachedTime = null;
        }
      }

      // Update match
      MatchType matchType;
      switch (table.get("DriverStation/MatchType", 0)) {
        case 1:
          matchType = MatchType.Practice;
          break;
        case 2:
          matchType = MatchType.Qualification;
          break;
        case 3:
          matchType = MatchType.Elimination;
          break;
        default:
          matchType = MatchType.None;
          break;
      }
      if (logMatchText == null && matchType != MatchType.None) {
        logMatchText = "";
        switch (matchType) {
          case Practice:
            logMatchText = "p";
            break;
          case Qualification:
            logMatchText = "q";
            break;
          case Elimination:
            logMatchText = "e";
            break;
          default:
            break;
        }
        logMatchText += Integer.toString(table.get("DriverStation/MatchNumber", 0));
      }

      // Update filename
      StringBuilder newFilenameBuilder = new StringBuilder();
      newFilenameBuilder.append("akit_");
      if (logDate == null) {
        newFilenameBuilder.append(randomIdentifier);
      } else {
        newFilenameBuilder.append(timeFormatter.format(logDate));
      }
      String eventName = table.get("DriverStation/EventName", "").toLowerCase();
      if (eventName.length() > 0) {
        newFilenameBuilder.append("_");
        newFilenameBuilder.append(eventName);
      }
      if (logMatchText != null) {
        newFilenameBuilder.append("_");
        newFilenameBuilder.append(logMatchText);
      }
      newFilenameBuilder.append(".wpilog");
      String newFilename = newFilenameBuilder.toString();
      if (!newFilename.equals(filename)) {
        String logPath = Path.of(folder, newFilename).toString();
        System.out.println("[AdvantageKit] Renaming log to \"" + logPath + "\"");

        File fileA = new File(folder, filename);
        File fileB = new File(folder, newFilename);
        fileA.renameTo(fileB);
        filename = newFilename;
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
          log.setMetadata(id, unit, table.getTimestamp());
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

    // Update last table
    lastTable = table;
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
