package org.littletonrobotics.junction.wpilog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.littletonrobotics.junction.LogDataReceiver;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.LogTable.LogValue;
import org.littletonrobotics.junction.LogTable.LoggableType;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.MatchType;

/** Records log values to a WPILOG file. */
public class WPILOGReceiver implements LogDataReceiver {
  private static final String extraHeader = "AdvantageKit";
  private static final String entryMetadata = "{\"source\":\"AdvantageKit\"}";
  private static final double writePeriodSecs = 0.25;
  private static final double timestampUpdateDelay = 3.0; // Wait several seconds to ensure timezone is updated
  private static final Map<LoggableType, String> logTypes = Map.of(LoggableType.Boolean, "boolean",
      LoggableType.BooleanArray, "boolean[]", LoggableType.Integer, "int64", LoggableType.IntegerArray, "int64[]",
      LoggableType.Double, "double", LoggableType.DoubleArray, "double[]", LoggableType.String, "string",
      LoggableType.StringArray, "string[]", LoggableType.ByteArray, "raw"); // The loggable types should probably be
                                                                            // updated to reflect the supported types
                                                                            // (no single bytes, add floats, switch int
                                                                            // to long).

  private String folder;
  private String filename;

  private boolean autoRename = false;
  private boolean updatedTime = false;
  private boolean updatedMatch = false;
  private Double firstUpdatedTime = null;

  private DataLog log;
  private LogTable lastTable = new LogTable(0);
  private Map<String, Integer> entryIDs = new HashMap<>();
  private Map<String, String> entryTypes = new HashMap<>();

  /**
   * Create a new WPILogReceiver for writing to a ".wpilog" file.
   * 
   * @param path Path to log file or folder. If only a folder is provided, the
   *             filename will be generated based on the current time and match
   *             number (if applicable).
   */
  public WPILOGReceiver(String path) {
    if (path.endsWith(".wpilog")) {
      File pathFile = new File(path);
      folder = pathFile.getParent();
      filename = pathFile.getName();
      autoRename = false;
    } else {
      folder = path;
      filename = "temp.wpilog";
      autoRename = true;
    }
  }

  public void start() {
    log = new DataLog(folder, filename, writePeriodSecs, extraHeader);
  }

  public void end() {
    log.close();
  }

  public void putEntry(LogTable table) {
    // Auto rename
    if (autoRename) {

      // Update timestamp
      if (!updatedTime) {
        if (System.currentTimeMillis() > 1638334800000L) { // 12/1/2021, the RIO 2 defaults to 7/1/2021
          if (firstUpdatedTime == null) {
            firstUpdatedTime = Logger.getInstance().getRealTimestamp() / 1000000.0;
          } else if (Logger.getInstance().getRealTimestamp() - firstUpdatedTime > timestampUpdateDelay) {
            log.setFilename(new SimpleDateFormat("'Log'_yy-MM-dd_HH-mm-ss'.wpilog'").format(new Date()));
            updatedTime = true;
          }
        }

        // Update match
      } else if (DriverStation.getMatchType() != MatchType.None && !updatedMatch) {
        String matchText = "";
        switch (DriverStation.getMatchType()) {
          case Practice:
            matchText = "p";
            break;
          case Qualification:
            matchText = "q";
            break;
          case Elimination:
            matchText = "e";
            break;
          default:
            break;
        }
        matchText += Integer.toString(DriverStation.getMatchNumber());
        log.setFilename(filename.substring(0, filename.length() - 7) + "_" + matchText + ".wpilog");
        updatedMatch = true;
      }
    }

    // Get new and old data
    Map<String, LogValue> newMap = table.getAll(false);
    Map<String, LogValue> oldMap = lastTable.getAll(false);

    // Encode fields
    for (Map.Entry<String, LogValue> field : newMap.entrySet()) {

      // Check if field should be updated
      String type = logTypes.get(field.getValue().type);
      boolean appendData = false;
      if (!entryIDs.containsKey(field.getKey())) {
        // New field
        entryIDs.put(field.getKey(), log.start(field.getKey(), type, entryMetadata, table.getTimestamp()));
        entryTypes.put(field.getKey(), type);
        appendData = true;
      } else if (entryTypes.get(field.getKey()).equals(type)
          && field.getValue().hasChanged(oldMap.get(field.getKey()))) {
        // Updated field
        appendData = true;
      }

      // Append data
      if (appendData) {
        int id = entryIDs.get(field.getKey());
        switch (field.getValue().type) {
          case Boolean:
            log.appendBoolean(id, field.getValue().getBoolean(), table.getTimestamp());
            break;
          case BooleanArray:
            log.appendBooleanArray(id, field.getValue().getBooleanArray(), table.getTimestamp());
            break;
          case Integer:
            log.appendInteger(id, field.getValue().getInteger(), table.getTimestamp());
            break;
          case IntegerArray:
            log.appendIntegerArray(id, Arrays.stream(field.getValue().getIntegerArray()).mapToLong(i -> i).toArray(),
                table.getTimestamp());
            break;
          case Double:
            log.appendDouble(id, field.getValue().getDouble(), table.getTimestamp());
            break;
          case DoubleArray:
            log.appendDoubleArray(id, field.getValue().getDoubleArray(), table.getTimestamp());
            break;
          case String:
            log.appendString(id, field.getValue().getString(), table.getTimestamp());
            break;
          case StringArray:
            log.appendStringArray(id, field.getValue().getStringArray(), table.getTimestamp());
            break;
          case ByteArray:
            log.appendRaw(id, field.getValue().getByteArray(), table.getTimestamp());
            break;
          default:
            break;
        }
      }
    }

    // Update last table
    lastTable = table;
  }
}
