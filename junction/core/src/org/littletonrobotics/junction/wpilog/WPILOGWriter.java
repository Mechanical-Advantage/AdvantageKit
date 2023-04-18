package org.littletonrobotics.junction.wpilog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.littletonrobotics.junction.LogDataReceiver;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LogValue;
import org.littletonrobotics.junction.LogTable.LoggableType;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.MatchType;
import edu.wpi.first.wpilibj.RobotBase;

/** Records log values to a WPILOG file. */
public class WPILOGWriter implements LogDataReceiver {
  private static final double writePeriodSecs = 0.25;
  private static final double timestampUpdateDelay = 8.0; // Wait several seconds after DS attached to ensure
                                                          // timestamp/timezone is updated

  private final String folder;
  private String filename;
  private final String randomIdentifier;

  private final boolean autoRename;
  private Date logDate;
  private String logMatchText;
  private Double dsAttachedTime;

  private DataLog log;
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

  public void start() {
    // Delete log if it already exists
    File logFile = new File(folder, filename);
    if (logFile.exists()) {
      logFile.delete();
    }

    // Create new log
    log = new DataLog(folder, filename, writePeriodSecs, WPILOGConstants.extraHeader);
    timestampID = log.start(timestampKey, LoggableType.Integer.getWPILOGType(),
        WPILOGConstants.entryMetadata, 0);
    lastTable = new LogTable(0);

    // Reset data
    entryIDs = new HashMap<>();
    entryTypes = new HashMap<>();
    logDate = null;
    logMatchText = null;
    dsAttachedTime = null;
  }

  public void end() {
    log.close();
  }

  public void putTable(LogTable table) {
    // Auto rename
    if (autoRename) {

      // Update timestamp
      if (logDate == null) {
        if (DriverStation.isDSAttached() || RobotBase.isSimulation()) {
          if (dsAttachedTime == null) {
            dsAttachedTime = Logger.getInstance().getRealTimestamp() / 1000000.0;
          } else if (Logger.getInstance().getRealTimestamp() / 1000000.0 - dsAttachedTime > timestampUpdateDelay
              || RobotBase.isSimulation()) {
            logDate = new Date();
          }
        } else {
          dsAttachedTime = null;
        }
      }

      // Update match
      if (logMatchText == null && DriverStation.getMatchType() != MatchType.None) {
        logMatchText = "";
        switch (DriverStation.getMatchType()) {
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
        logMatchText += Integer.toString(DriverStation.getMatchNumber());
      }

      // Update filename
      StringBuilder newFilenameBuilder = new StringBuilder();
      newFilenameBuilder.append("Log_");
      if (logDate == null) {
        newFilenameBuilder.append(randomIdentifier);
      } else {
        newFilenameBuilder.append(new SimpleDateFormat("yy-MM-dd_HH-mm-ss").format(logDate));
      }
      if (logMatchText != null) {
        newFilenameBuilder.append("_");
        newFilenameBuilder.append(logMatchText);
      }
      newFilenameBuilder.append(".wpilog");
      String newFilename = newFilenameBuilder.toString();
      if (!newFilename.equals(filename)) {
        log.setFilename(newFilename);
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
        entryIDs.put(field.getKey(),
            log.start(field.getKey(), type.getWPILOGType(), WPILOGConstants.entryMetadata, table.getTimestamp()));
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

    // Update last table
    lastTable = table;
  }
}
