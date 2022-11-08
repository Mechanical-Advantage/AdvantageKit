package org.littletonrobotics.junction.wpilog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LoggableType;
import org.littletonrobotics.junction.LogDataReceiver;
import org.littletonrobotics.junction.LogReplaySource;

import edu.wpi.first.util.datalog.DataLogIterator;
import edu.wpi.first.util.datalog.DataLogReader;
import edu.wpi.first.util.datalog.DataLogRecord;
import edu.wpi.first.wpilibj.DriverStation;

/** Replays log values from a WPILOG file. */
public class WPILOGReader implements LogReplaySource {
  private final String filename;
  private boolean isValid;

  private DataLogReader reader;
  private DataLogIterator iterator;

  private Long timestamp;
  private Map<Integer, String> entryIDs;
  private Map<Integer, LoggableType> entryTypes;

  public WPILOGReader(String filename) {
    this.filename = filename;
  }

  public void start() {
    // Open log file
    try {
      reader = new DataLogReader(filename);
    } catch (IOException e) {
      DriverStation.reportError("Failed to open replay log file.", true);
    }

    // Check validity
    if (!reader.isValid()) {
      DriverStation.reportError("The replay log is not a valid WPILOG file.", false);
      isValid = false;
    } else if (!reader.getExtraHeader().equals(WPILOGConstants.extraHeader)) {
      DriverStation.reportError("The replay log was not produced by AdvantageKit.", true);
      isValid = false;
    } else {
      isValid = true;
    }

    // Create iterator and reset
    iterator = reader.iterator();
    timestamp = null;
    entryIDs = new HashMap<>();
    entryTypes = new HashMap<>();
  }

  public boolean updateTable(LogTable table) {
    if (!isValid) {
      return false;
    }

    // Update timestamp (except the first cycle b/c it isn't known yet)
    if (timestamp != null) {
      table.setTimestamp(timestamp);
    }

    // Iterate over log
    while (iterator.hasNext()) {
      DataLogRecord record = iterator.next();

      if (record.isControl()) {
        if (record.isStart()) { // Ignore other control records
          if (record.getStartData().metadata.equals(WPILOGConstants.entryMetadata)) {
            entryIDs.put(record.getStartData().entry, record.getStartData().name);
            entryTypes.put(record.getStartData().entry, LoggableType.fromWPILOGType(record.getStartData().type));
          }
        }

      } else {
        String entry = entryIDs.get(record.getEntry());
        if (entry != null) {
          if (entry.equals(LogDataReceiver.timestampKey)) {
            boolean firstTimestamp = timestamp == null;
            timestamp = record.getInteger();
            if (firstTimestamp) {
              table.setTimestamp(timestamp);
            } else {
              break; // End of cycle
            }

          } else if (timestamp != null && record.getTimestamp() == timestamp) {
            entry = entry.substring(1); // Remove leading slash
            switch (entryTypes.get(record.getEntry())) {
              case Raw:
                table.put(entry, record.getRaw());
                break;
              case Boolean:
                table.put(entry, record.getBoolean());
                break;
              case Integer:
                table.put(entry, record.getInteger());
                break;
              case Float:
                table.put(entry, record.getFloat());
                break;
              case Double:
                table.put(entry, record.getDouble());
                break;
              case String:
                table.put(entry, record.getString());
                break;
              case BooleanArray:
                table.put(entry, record.getBooleanArray());
                break;
              case IntegerArray:
                table.put(entry, record.getIntegerArray());
                break;
              case FloatArray:
                table.put(entry, record.getFloatArray());
                break;
              case DoubleArray:
                table.put(entry, record.getDoubleArray());
                break;
              case StringArray:
                table.put(entry, record.getStringArray());
                break;
            }
          }
        }
      }
    }

    // Continue if there is more data
    return iterator.hasNext();
  }
}
