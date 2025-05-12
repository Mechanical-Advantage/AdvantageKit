// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.rlog;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LogValue;

/**
 * Converts log tables to the RLOG format. Based on RLOG R2 with support for custom type strings.
 */
class RLOGEncoder {
  public static final byte logRevision = (byte) 2;

  private ByteBuffer nextOutput;
  private boolean isFirstTable = true;
  private LogTable lastTable = new LogTable(0);
  private Map<String, Short> keyIDs = new HashMap<>();
  private Map<String, String> keyTypes = new HashMap<>();
  private short nextKeyID = 0;

  /** Reads the encoded output of the last encoded table. */
  public ByteBuffer getOutput() {
    return nextOutput;
  }

  /** Returns data required to start a new receiver (full contents of last table + all key IDs). */
  public ByteBuffer getNewcomerData() {
    List<ByteBuffer> buffers = new ArrayList<>();

    // Encode log revision
    buffers.add(ByteBuffer.allocate(1).put(logRevision));

    // Encode timestamp
    buffers.add(encodeTimestamp(lastTable.getTimestamp() / 1000000.0));

    // Encode key IDs
    for (Map.Entry<String, Short> keyID : keyIDs.entrySet()) {
      buffers.add(encodeKey(keyID.getValue(), keyID.getKey(), keyTypes.get(keyID.getKey())));
    }

    // Encode fields
    for (Map.Entry<String, LogValue> field : lastTable.getAll(false).entrySet()) {
      buffers.add(encodeValue(keyIDs.get(field.getKey()), field.getValue()));
    }

    // Combine buffers
    int capacity = 0;
    for (ByteBuffer buffer : buffers) {
      capacity += buffer.capacity();
    }
    ByteBuffer output = ByteBuffer.allocate(capacity);
    for (ByteBuffer buffer : buffers) {
      output.put(buffer.array());
    }
    return output;
  }

  /** Encodes a single table and stores the result. */
  public void encodeTable(LogTable table, boolean includeRevision) {
    List<ByteBuffer> buffers = new ArrayList<>();

    Map<String, LogValue> newMap = table.getAll(false);
    Map<String, LogValue> oldMap = lastTable.getAll(false);

    // Encode log revision
    if (isFirstTable && includeRevision) {
      buffers.add(ByteBuffer.allocate(1).put(logRevision));
      isFirstTable = false;
    }

    // Encode timestamp
    buffers.add(encodeTimestamp(table.getTimestamp() / 1000000.0));

    // Encode new/changed fields
    for (Map.Entry<String, LogValue> field : newMap.entrySet()) {
      // Check if field has changed
      LogValue newValue = field.getValue();
      if (newValue.equals(oldMap.get(field.getKey()))) {
        continue;
      }

      // Write new data
      if (!keyIDs.containsKey(field.getKey())) {
        keyIDs.put(field.getKey(), nextKeyID);
        keyTypes.put(field.getKey(), field.getValue().getWPILOGType());
        buffers.add(encodeKey(nextKeyID, field.getKey(), field.getValue().getWPILOGType()));
        nextKeyID++;
      }
      buffers.add(encodeValue(keyIDs.get(field.getKey()), newValue));
    }

    // Update last table
    lastTable = table;

    // Combine buffers
    int capacity = 0;
    for (ByteBuffer buffer : buffers) {
      capacity += buffer.capacity();
    }
    nextOutput = ByteBuffer.allocate(capacity);
    for (ByteBuffer buffer : buffers) {
      nextOutput.put(buffer.array());
    }
  }

  private static ByteBuffer encodeTimestamp(double timestamp) {
    ByteBuffer buffer = ByteBuffer.allocate(1 + Double.BYTES);
    buffer.put((byte) 0);
    buffer.putDouble(timestamp);
    return buffer;
  }

  private static ByteBuffer encodeKey(short keyID, String key, String type) {
    try {
      byte[] keyBytes = key.getBytes("UTF-8");
      byte[] typeBytes = type.getBytes("UTF-8");
      ByteBuffer buffer =
          ByteBuffer.allocate(
              1 + Short.BYTES + Short.BYTES + keyBytes.length + Short.BYTES + typeBytes.length);
      buffer.put((byte) 1);
      buffer.putShort(keyID);
      buffer.putShort((short) keyBytes.length);
      buffer.put(keyBytes);
      buffer.putShort((short) typeBytes.length);
      buffer.put(typeBytes);
      return buffer;
    } catch (UnsupportedEncodingException e) {
      return ByteBuffer.allocate(0);
    }
  }

  private static ByteBuffer encodeValue(short keyID, LogValue value) {
    try {
      // Generate key and length buffer
      ByteBuffer keyBuffer = ByteBuffer.allocate(1 + Short.BYTES + Short.BYTES);
      keyBuffer.put((byte) 2);
      keyBuffer.putShort(keyID);

      // Generate value buffer (and type for key)
      ByteBuffer valueBuffer;
      switch (value.type) {
        case Raw:
          byte[] byteArray = value.getRaw();
          valueBuffer = ByteBuffer.allocate(byteArray.length);
          valueBuffer.put(byteArray);
          break;
        case Boolean:
          valueBuffer = ByteBuffer.allocate(1).put(value.getBoolean() ? (byte) 1 : (byte) 0);
          break;
        case Integer:
          valueBuffer = ByteBuffer.allocate(Long.BYTES).putLong(value.getInteger());
          break;
        case Float:
          valueBuffer = ByteBuffer.allocate(Float.BYTES).putFloat(value.getFloat());
          break;
        case Double:
          valueBuffer = ByteBuffer.allocate(Double.BYTES).putDouble(value.getDouble());
          break;
        case String:
          String stringValue = value.getString();
          byte[] stringBytes = stringValue.getBytes("UTF-8");
          valueBuffer = ByteBuffer.allocate(stringBytes.length);
          valueBuffer.put(stringBytes);
          break;
        case BooleanArray:
          boolean[] booleanArray = value.getBooleanArray();
          valueBuffer = ByteBuffer.allocate(booleanArray.length);
          for (boolean i : booleanArray) {
            valueBuffer.put(i ? (byte) 1 : (byte) 0);
          }
          break;
        case IntegerArray:
          long[] intArray = value.getIntegerArray();
          valueBuffer = ByteBuffer.allocate(intArray.length * Long.BYTES);
          for (long i : intArray) {
            valueBuffer.putLong(i);
          }
          break;
        case FloatArray:
          float[] floatArray = value.getFloatArray();
          valueBuffer = ByteBuffer.allocate(floatArray.length * Float.BYTES);
          for (float i : floatArray) {
            valueBuffer.putFloat(i);
          }
          break;
        case DoubleArray:
          double[] doubleArray = value.getDoubleArray();
          valueBuffer = ByteBuffer.allocate(doubleArray.length * Double.BYTES);
          for (double i : doubleArray) {
            valueBuffer.putDouble(i);
          }
          break;
        case StringArray:
          String[] stringArray = value.getStringArray();
          int capacity = Integer.BYTES;
          for (String i : stringArray) {
            capacity += Integer.BYTES + i.getBytes("UTF-8").length;
          }
          valueBuffer = ByteBuffer.allocate(capacity);
          valueBuffer.putInt(stringArray.length);
          for (String i : stringArray) {
            byte[] bytes = i.getBytes("UTF-8");
            valueBuffer.putInt(bytes.length);
            valueBuffer.put(bytes);
          }
          break;
        default:
          valueBuffer = ByteBuffer.allocate(0);
      }

      keyBuffer.putShort((short) valueBuffer.capacity());
      return ByteBuffer.allocate(keyBuffer.capacity() + valueBuffer.capacity())
          .put(keyBuffer.array())
          .put(valueBuffer.array());
    } catch (UnsupportedEncodingException e) {
      return ByteBuffer.allocate(0);
    }
  }
}
