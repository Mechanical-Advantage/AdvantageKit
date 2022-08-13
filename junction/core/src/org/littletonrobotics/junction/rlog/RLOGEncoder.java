package org.littletonrobotics.junction.rlog;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LogValue;

/** Converts log tables to the RLOG format. */
public class RLOGEncoder {
  public static final byte logRevision = (byte) 1;

  private ByteBuffer nextOutput;
  private boolean isFirstTable = true;
  private LogTable lastTable = new LogTable(0);
  private Map<String, Short> keyIDs = new HashMap<>();
  private short nextKeyID = 0;

  /** Reads the encoded output of the last encoded table. */
  public ByteBuffer getOutput() {
    return nextOutput;
  }

  /**
   * Encodes a single tables and returns the encoded output. Equivalent to calling
   * "encodeTable()" and then "getOutput()"
   */
  public ByteBuffer getOutput(LogTable table) {
    encodeTable(table);
    return nextOutput;
  }

  /**
   * Returns data required to start a new receiver (full contents of last table +
   * all key IDs).
   */
  public ByteBuffer getNewcomerData() {
    List<ByteBuffer> buffers = new ArrayList<>();

    // Encode log revision
    buffers.add(ByteBuffer.allocate(1).put(logRevision));

    // Encode timestamp
    buffers.add(encodeTimestamp(lastTable.getTimestamp() / 1000000.0));

    // Encode key IDs
    for (Map.Entry<String, Short> keyID : keyIDs.entrySet()) {
      buffers.add(encodeKey(keyID.getValue(), keyID.getKey()));
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
  public void encodeTable(LogTable table) {
    List<ByteBuffer> buffers = new ArrayList<>();

    Map<String, LogValue> newMap = table.getAll(false);
    Map<String, LogValue> oldMap = lastTable.getAll(false);

    // Encode log revision
    if (isFirstTable) {
      buffers.add(ByteBuffer.allocate(1).put(logRevision));
      isFirstTable = false;
    }

    // Encode timestamp
    buffers.add(encodeTimestamp(table.getTimestamp() / 1000000.0));

    // Encode new/changed fields
    for (Map.Entry<String, LogValue> field : newMap.entrySet()) {
      // Check if field has changed
      LogValue newValue = field.getValue();
      if (!newValue.hasChanged(oldMap.get(field.getKey()))) {
        continue;
      }

      // Write new data
      if (!keyIDs.containsKey(field.getKey())) {
        keyIDs.put(field.getKey(), nextKeyID);
        buffers.add(encodeKey(nextKeyID, field.getKey()));
        nextKeyID++;
      }
      buffers.add(encodeValue(keyIDs.get(field.getKey()), newValue));
    }

    // Encode removed fields
    for (Map.Entry<String, LogValue> field : oldMap.entrySet()) {
      if (!newMap.containsKey(field.getKey())) {
        buffers.add(encodeValue(keyIDs.get(field.getKey()), null));
      }
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

  private static ByteBuffer encodeKey(short keyID, String key) {
    try {
      byte[] keyBytes = key.getBytes("UTF-8");
      ByteBuffer buffer = ByteBuffer.allocate(1 + Short.BYTES + Short.BYTES + keyBytes.length);
      buffer.put((byte) 1);
      buffer.putShort(keyID);
      buffer.putShort((short) keyBytes.length);
      buffer.put(keyBytes);
      return buffer;
    } catch (UnsupportedEncodingException e) {
      return ByteBuffer.allocate(0);
    }
  }

  private static ByteBuffer encodeValue(short keyID, LogValue value) {
    try {
      // Generate key and type buffer
      ByteBuffer keyBuffer = ByteBuffer.allocate(1 + Short.BYTES + 1);
      keyBuffer.put((byte) 2);
      keyBuffer.putShort(keyID);
      if (value == null) {
        keyBuffer.put((byte) 0);
      } else {
        keyBuffer.put((byte) (value.type.ordinal() + 1));
      }

      // Generate value buffer
      ByteBuffer valueBuffer;
      if (value == null) {
        valueBuffer = ByteBuffer.allocate(0);
      } else {
        switch (value.type) {
          case Boolean:
            valueBuffer = ByteBuffer.allocate(1).put(value.getBoolean() ? (byte) 1 : (byte) 0);
            break;
          case Byte:
            valueBuffer = ByteBuffer.allocate(1).put(value.getByte());
            break;
          case Integer:
            valueBuffer = ByteBuffer.allocate(Integer.BYTES).putInt(value.getInteger());
            break;
          case Double:
            valueBuffer = ByteBuffer.allocate(Double.BYTES).putDouble(value.getDouble());
            break;
          case String:
            String stringValue = value.getString();
            byte[] stringBytes = stringValue.getBytes("UTF-8");
            valueBuffer = ByteBuffer.allocate(Short.BYTES + stringBytes.length);
            valueBuffer.putShort((short) stringBytes.length);
            valueBuffer.put(stringBytes);
            break;
          case BooleanArray:
            boolean[] booleanArray = value.getBooleanArray();
            valueBuffer = ByteBuffer.allocate(Short.BYTES + booleanArray.length);
            valueBuffer.putShort((short) booleanArray.length);
            for (boolean i : booleanArray) {
              valueBuffer.put(i ? (byte) 1 : (byte) 0);
            }
            break;
          case ByteArray:
            byte[] byteArray = value.getByteArray();
            valueBuffer = ByteBuffer.allocate(Short.BYTES + byteArray.length);
            valueBuffer.putShort((short) byteArray.length);
            valueBuffer.put(byteArray);
            break;
          case IntegerArray:
            int[] intArray = value.getIntegerArray();
            valueBuffer = ByteBuffer.allocate(Short.BYTES + (intArray.length * Integer.BYTES));
            valueBuffer.putShort((short) intArray.length);
            for (int i : intArray) {
              valueBuffer.putInt(i);
            }
            break;
          case DoubleArray:
            double[] doubleArray = value.getDoubleArray();
            valueBuffer = ByteBuffer.allocate(Short.BYTES + (doubleArray.length * Double.BYTES));
            valueBuffer.putShort((short) doubleArray.length);
            for (double i : doubleArray) {
              valueBuffer.putDouble(i);
            }
            break;
          case StringArray:
            String[] stringArray = value.getStringArray();
            int capacity = Short.BYTES;
            for (String i : stringArray) {
              capacity += Short.BYTES + i.getBytes("UTF-8").length;
            }
            valueBuffer = ByteBuffer.allocate(capacity);
            valueBuffer.putShort((short) stringArray.length);
            for (String i : stringArray) {
              byte[] bytes = i.getBytes("UTF-8");
              valueBuffer.putShort((short) bytes.length);
              valueBuffer.put(bytes);
            }
            break;
          default:
            valueBuffer = ByteBuffer.allocate(0);
        }
      }

      return ByteBuffer.allocate(keyBuffer.capacity() + valueBuffer.capacity()).put(keyBuffer.array())
          .put(valueBuffer.array());
    } catch (UnsupportedEncodingException e) {
      return ByteBuffer.allocate(0);
    }
  }
}