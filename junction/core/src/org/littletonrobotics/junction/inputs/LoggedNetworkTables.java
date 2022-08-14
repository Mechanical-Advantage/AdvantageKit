package org.littletonrobotics.junction.inputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LogValue;
import edu.wpi.first.networktables.NetworkTable;

/**
 * Manages logging and replaying the contents of NetworkTables.
 */
public class LoggedNetworkTables implements LoggableInputs {

  private static LoggedNetworkTables instance;
  private static NetworkTableInstance networkTables = NetworkTableInstance.getDefault();

  private List<String> tables = new ArrayList<>();

  private LoggedNetworkTables() {
  }

  public static LoggedNetworkTables getInstance() {
    if (instance == null) {
      instance = new LoggedNetworkTables();
    }
    return instance;
  }

  /**
   * Adds a new table to log and replay ("/SmartDashboard" is included by
   * default). This should be called for any tables used in user code.
   * 
   * @param prefix The name of the table to add
   */
  public void addTable(String prefix) {
    tables.add(prefix);
  }

  public void toLog(LogTable table) {
    for (int tableId = 0; tableId < tables.size(); tableId++) {
      NetworkTableEntry[] entries = networkTables.getEntries(tables.get(tableId), 0);
      for (int entryId = 0; entryId < entries.length; entryId++) {
        NetworkTableEntry entry = entries[entryId];
        String key = entry.getName().substring(1);
        switch (entry.getType()) {
          case kRaw:
            table.put(key, entry.getRaw(new byte[0]));
          case kBoolean:
            table.put(key, entry.getBoolean(false));
            break;
          case kDouble:
            table.put(key, entry.getDouble(0.0));
            break;
          case kString:
            table.put(key, entry.getString(""));
            break;
          case kBooleanArray:
            table.put(key, entry.getBooleanArray(new boolean[0]));
            break;
          case kDoubleArray:
            table.put(key, entry.getDoubleArray(new double[0]));
            break;
          case kStringArray:
            table.put(key, entry.getStringArray(new String[0]));
            break;
          default:
            break;
        }
      }
    }
  }

  public void fromLog(LogTable table) {
    NetworkTable netTable = networkTables.getTable("/");

    for (Map.Entry<String, LogValue> mapEntry : table.getAll(true).entrySet()) {
      NetworkTableEntry tableEntry = netTable.getEntry(mapEntry.getKey());

      switch (mapEntry.getValue().type) {
        case Raw:
          tableEntry.setRaw(mapEntry.getValue().getRaw());
          break;
        case Boolean:
          tableEntry.setBoolean(mapEntry.getValue().getBoolean());
          break;
        case Integer:
          tableEntry.setDouble(mapEntry.getValue().getInteger());
          break;
        case Float:
          tableEntry.setDouble(mapEntry.getValue().getFloat());
          break;
        case Double:
          tableEntry.setDouble(mapEntry.getValue().getDouble());
          break;
        case BooleanArray:
          tableEntry.setBooleanArray(mapEntry.getValue().getBooleanArray());
          break;
        case IntegerArray:
          tableEntry.setDoubleArray(Arrays.stream(mapEntry.getValue().getIntegerArray()).asDoubleStream().toArray());
          break;
        case FloatArray:
          float[] floatArray = mapEntry.getValue().getFloatArray();
          tableEntry.setDoubleArray(IntStream.range(0, floatArray.length).mapToDouble(i -> floatArray[i]).toArray());
          break;
        case DoubleArray:
          tableEntry.setDoubleArray(mapEntry.getValue().getDoubleArray());
          break;
        case String:
          tableEntry.setString(mapEntry.getValue().getString());
          break;
        case StringArray:
          tableEntry.setStringArray(mapEntry.getValue().getStringArray());
          break;
        default:
          break;
      }
    }
  }
}
