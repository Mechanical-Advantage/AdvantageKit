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

package org.littletonrobotics.junction.networktables;

import java.util.HashMap;
import java.util.Map;

import org.littletonrobotics.junction.LogDataReceiver;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LogValue;

import edu.wpi.first.networktables.*;

/** Publishes log data using NT4. */
public class NT4Publisher implements LogDataReceiver {
  private final NetworkTable akitTable;
  private LogTable lastTable = new LogTable(0);
  private final IntegerPublisher timestampPublisher;
  private final Map<String, GenericPublisher> publishers = new HashMap<>();

  public NT4Publisher() {
    akitTable = NetworkTableInstance.getDefault().getTable("/AdvantageKit");
    timestampPublisher = akitTable.getIntegerTopic(timestampKey.substring(1)).publish(PubSubOption.sendAll(true));
  }

  public void putTable(LogTable table) {
    // Send timestamp
    timestampPublisher.set(table.getTimestamp(), table.getTimestamp());

    // Get old and new data
    Map<String, LogValue> newMap = table.getAll(false);
    Map<String, LogValue> oldMap = lastTable.getAll(false);

    // Encode new/changed fields
    for (Map.Entry<String, LogValue> field : newMap.entrySet()) {
      // Check if field has changed
      LogValue newValue = field.getValue();
      if (newValue.equals(oldMap.get(field.getKey()))) {
        continue;
      }

      // Create publisher if necessary
      String key = field.getKey().substring(1);
      GenericPublisher publisher = publishers.get(key);
      if (publisher == null) {
        publisher = akitTable.getTopic(key).genericPublish(field.getValue().getNT4Type(), PubSubOption.sendAll(true));
        publishers.put(key, publisher);
      }

      // Write new data
      switch (field.getValue().type) {
        case Raw:
          publisher.setRaw(field.getValue().getRaw(), table.getTimestamp());
          break;
        case Boolean:
          publisher.setBoolean(field.getValue().getBoolean(), table.getTimestamp());
          break;
        case BooleanArray:
          publisher.setBooleanArray(field.getValue().getBooleanArray(), table.getTimestamp());
          break;
        case Integer:
          publisher.setInteger(field.getValue().getInteger(), table.getTimestamp());
          break;
        case IntegerArray:
          publisher.setIntegerArray(field.getValue().getIntegerArray(), table.getTimestamp());
          break;
        case Float:
          publisher.setFloat(field.getValue().getFloat(), table.getTimestamp());
          break;
        case FloatArray:
          publisher.setFloatArray(field.getValue().getFloatArray(), table.getTimestamp());
          break;
        case Double:
          publisher.setDouble(field.getValue().getDouble(), table.getTimestamp());
          break;
        case DoubleArray:
          publisher.setDoubleArray(field.getValue().getDoubleArray(), table.getTimestamp());
          break;
        case String:
          publisher.setString(field.getValue().getString(), table.getTimestamp());
          break;
        case StringArray:
          publisher.setStringArray(field.getValue().getStringArray(), table.getTimestamp());
          break;
      }
    }

    // Update last table
    lastTable = table;
  }
}
