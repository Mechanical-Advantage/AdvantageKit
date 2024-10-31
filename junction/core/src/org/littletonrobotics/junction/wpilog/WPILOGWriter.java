// Copyright 2021-2024 FRC 6328
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

package org.littletonrobotics.junction.wpilog;

import edu.wpi.first.util.datalog.DataLogWriter;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.MatchType;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
  private static final double timestampUpdateDelay = 5.0; // Wait several seconds after DS attached to ensure
                                                          // timestamp/timezone is updated
  private static final String defaultPathRio = "/U/logs";
  private static final String defaultPathSim = "logs";
  private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yy-MM-dd_HH-mm-ss");

  private String folder;
  private String filename;
  private final String randomIdentifier;
  private Double dsAttachedTime;

  private boolean autoRename;
  private LocalDateTime logDate;
  private String logMatchText;

  private DataLogWriter log;
  private boolean isOpen = false;
  private LogTable lastTable;
  private int timestampID;
  private Map<String, Integer> entryIDs;
  private Map<String, LoggableType> entryTypes;

  /**
   * Create a new WPILOGWriter for writing to a ".wpilog" file.
   *
   * @param path Path to log file or folder. If only a folder is provided, the
   *             filename will be generated based on the current time and match
   *             number (if applicable).
   */
  public WPILOGWriter(String path) {
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
      filename = "Log_" + randomIdentifier + ".wpilog";
      autoRename = true;
    }
  }

  /**
   * Create a new WPILOGWriter for writing to a ".wpilog" file.
   *
   * <p>
   * The logs will be saved to "/U/logs" on the RIO and "logs" in sim. The
   * filename will be generated based on the current time and match number (if
   * applicable).
   */
  public WPILOGWriter() {
    this(RobotBase.isSimulation() ? defaultPathSim : defaultPathRio);
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
    System.out.println("Logging to \"" + logPath + "\"");
    try {
      log = new DataLogWriter(logPath, WPILOGConstants.extraHeader);
    } catch (IOException e) {
      DriverStation.reportError("Failed to open output log file.", true);
      return;
    }
    isOpen = true;
    timestampID = log.start(
        timestampKey, LoggableType.Integer.getWPILOGType(), WPILOGConstants.entryMetadata, 0);
    lastTable = new LogTable(0);

    // Reset data
    entryIDs = new HashMap<>();
    entryTypes = new HashMap<>();
    logDate = null;
    logMatchText = null;
  }

  public void end() {
    log.close();
  }

  public void putTable(LogTable table) {
    // Exit if log not open
    if (!isOpen)
      return;

    // Auto rename
    if (autoRename) {

      // Update timestamp
      if (logDate == null) {
        if ((table.get("DriverStation/DSAttached", false)
            && table.get("SystemStats/SystemTimeValid", false))
            || RobotBase.isSimulation()) {
          if (dsAttachedTime == null) {
            dsAttachedTime = Logger.getRealTimestamp() / 1000000.0;
          } else if (Logger.getRealTimestamp() / 1000000.0 - dsAttachedTime > timestampUpdateDelay
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
      newFilenameBuilder.append("Log_");
      if (logDate == null) {
        newFilenameBuilder.append(randomIdentifier);
      } else {
        newFilenameBuilder.append(timeFormatter.format(logDate));
      }
      if (logMatchText != null) {
        newFilenameBuilder.append("_");
        newFilenameBuilder.append(logMatchText);
      }
      newFilenameBuilder.append(".wpilog");
      String newFilename = newFilenameBuilder.toString();
      if (!newFilename.equals(filename) && Timer.getFPGATimestamp() > 15.0) {
        String logPath = Path.of(folder, filename).toString();
        System.out.println("Renaming log to \"" + logPath + "\"");

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
      boolean appendData = false;
      if (!entryIDs.containsKey(field.getKey())) { // New field
        entryIDs.put(
            field.getKey(),
            log.start(
                field.getKey(),
                field.getValue().getWPILOGType(),
                WPILOGConstants.entryMetadata,
                table.getTimestamp()));
        entryTypes.put(field.getKey(), type);
        appendData = true;
      } else if (!field.getValue().equals(oldMap.get(field.getKey()))) { // Updated field
        appendData = true;
      }

      // Append data
      if (appendData) {
        int id = entryIDs.get(field.getKey());
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
}
