package org.littletonrobotics.junction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A table of logged data in allowable types. Can reference another higher level
 * table.
 */
public class LogTable {
  private final double timestamp;
  private final String prefix;
  private final Map<String, LogValue> data;

  /**
   * Creates a new LogTable, to serve as the root table.
   */
  public LogTable(double timestamp) {
    this.timestamp = timestamp;
    prefix = "/";
    data = new HashMap<String, LogValue>();
  }

  /**
   * Creates a new LogTable, copying data from the given source.
   */
  public LogTable(double timestamp, LogTable source) {
    this.timestamp = timestamp;
    prefix = source.prefix;
    data = new HashMap<String, LogValue>();
    data.putAll(source.data);
  }

  /**
   * Creates a new LogTable, to reference a subtable.
   */
  private LogTable(double timestamp, String prefix, Map<String, LogValue> data) {
    this.timestamp = timestamp;
    this.prefix = prefix;
    this.data = data;
  }

  /**
   * Returns the timestamp of the table.
   */
  public double getTimestamp() {
    return timestamp;
  }

  /**
   * Creates a new LogTable for referencing a single subtable. Modifications to
   * the subtable will be reflected in the original object.
   * 
   * @param tableName The name of the subtable. Do not include a trailing slash.
   * @return The subtable object.
   */
  public LogTable getSubtable(String tableName) {
    return new LogTable(timestamp, prefix + tableName + "/", data);
  }

  /**
   * Returns all values from the table.
   * 
   * @param subtableOnly If true, include only values in the subtable (no prefix).
   *                     If false, include all values.
   * @return Map of the requested data.
   */
  public Map<String, LogValue> getAll(boolean subtableOnly) {
    if (subtableOnly) {
      Map<String, LogValue> result = new HashMap<String, LogValue>();
      for (Map.Entry<String, LogValue> field : data.entrySet()) {
        if (field.getKey().startsWith(prefix)) {
          result.put(field.getKey().substring(prefix.length()), field.getValue());
        }
      }
      return result;
    } else {
      return data;
    }
  }

  /** Removes a field from the table. */
  public void remove(String key) {
    data.remove(prefix + key);
  }

  /** Writes a new Boolean value to the table. */
  public void put(String key, boolean value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Writes a new BooleanArray value to the table. */
  public void put(String key, boolean[] value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Writes a new Integer value to the table. */
  public void put(String key, int value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Writes a new IntegerArray value to the table. */
  public void put(String key, int[] value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Writes a new Double value to the table. */
  public void put(String key, double value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Writes a new DoubleArray value to the table. */
  public void put(String key, double[] value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Writes a new String value to the table. */
  public void put(String key, String value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Writes a new StringArray value to the table. */
  public void put(String key, String[] value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Writes a new Byte value to the table. */
  public void put(String key, byte value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Writes a new ByteArray value to the table. */
  public void put(String key, byte[] value) {
    data.put(prefix + key, new LogValue(value));
  }

  /** Reads a generic value from the table. */
  public LogValue get(String key) {
    return data.get(prefix + key);
  }

  /** Reads a Boolean value from the table. */
  public boolean getBoolean(String key, boolean defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getBoolean(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a BooleanArray value from the table. */
  public boolean[] getBooleanArray(String key, boolean[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getBooleanArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a Integer value from the table. */
  public int getInteger(String key, int defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getInteger(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a IntegerArray value from the table. */
  public int[] getIntegerArray(String key, int[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getIntegerArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a Double value from the table. */
  public double getDouble(String key, double defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getDouble(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a DoubleArray value from the table. */
  public double[] getDoubleArray(String key, double[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getDoubleArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a String value from the table. */
  public String getString(String key, String defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getString(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a StringArray value from the table. */
  public String[] getStringArray(String key, String[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getStringArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a Byte value from the table. */
  public byte getByte(String key, byte defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getByte(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a ByteArray value from the table. */
  public byte[] getByteArray(String key, byte[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getByteArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Returns a string representation of the table. */
  public String toString() {
    String output = "Timestamp=" + Double.toString(timestamp) + "\n";
    output += "Prefix=\"" + prefix + "\"\n";
    output += "{\n";
    for (Map.Entry<String, LogValue> field : getAll(true).entrySet()) {
      output += "\t" + field.getKey() + "=";
      LogValue value = field.getValue();
      switch (value.type) {
        case Boolean:
          output += value.getBoolean() ? "true" : "false";
          break;
        case BooleanArray:
          output += Arrays.toString(value.getBooleanArray());
          break;
        case Integer:
          output += Integer.toString(value.getInteger());
          break;
        case IntegerArray:
          output += Arrays.toString(value.getIntegerArray());
          break;
        case Double:
          output += Double.toString(value.getDouble());
          break;
        case DoubleArray:
          output += Arrays.toString(value.getDoubleArray());
          break;
        case String:
          output += "\"" + value.getString() + "\"";
          break;
        case StringArray:
          output += "[";
          String[] stringArray = value.getStringArray();
          for (int i = 0; i < stringArray.length; i++) {
            output += "\"" + stringArray[i] + "\"";
            output += i < stringArray.length - 1 ? "," : "";
          }
          output += "]";
          break;
        case Byte:
          output += Byte.toString(value.getByte());
          break;
        case ByteArray:
          output += Arrays.toString(value.getByteArray());
          break;
      }
      output += "\n";
    }
    output += "}";
    return output;
  }

  /**
   * Represents a value stored in a LogTable, including type and value.
   */
  public class LogValue {
    public final LoggableType type;
    private final Object value;

    LogValue(boolean value) {
      type = LoggableType.Boolean;
      this.value = value;
    }

    public boolean getBoolean() {
      return getBoolean(false);
    }

    public boolean getBoolean(boolean defaultValue) {
      return type == LoggableType.Boolean ? (boolean) value : defaultValue;
    }

    LogValue(boolean[] value) {
      type = LoggableType.BooleanArray;
      this.value = value;
    }

    public boolean[] getBooleanArray() {
      return getBooleanArray(new boolean[] {});
    }

    public boolean[] getBooleanArray(boolean[] defaultValue) {
      return type == LoggableType.BooleanArray ? (boolean[]) value : defaultValue;
    }

    LogValue(int value) {
      type = LoggableType.Integer;
      this.value = value;
    }

    public int getInteger() {
      return getInteger(0);
    }

    public int getInteger(int defaultValue) {
      return type == LoggableType.Integer ? (int) value : defaultValue;
    }

    LogValue(int[] value) {
      type = LoggableType.IntegerArray;
      this.value = value;
    }

    public int[] getIntegerArray() {
      return getIntegerArray(new int[] {});
    }

    public int[] getIntegerArray(int[] defaultValue) {
      return type == LoggableType.IntegerArray ? (int[]) value : defaultValue;
    }

    LogValue(double value) {
      type = LoggableType.Double;
      this.value = value;
    }

    public double getDouble() {
      return getDouble(0.0);
    }

    public double getDouble(double defaultValue) {
      return type == LoggableType.Double ? (double) value : defaultValue;
    }

    LogValue(double[] value) {
      type = LoggableType.DoubleArray;
      this.value = value;
    }

    public double[] getDoubleArray() {
      return getDoubleArray(new double[] {});
    }

    public double[] getDoubleArray(double[] defaultValue) {
      return type == LoggableType.DoubleArray ? (double[]) value : defaultValue;
    }

    LogValue(String value) {
      type = LoggableType.String;
      this.value = value;
    }

    public String getString() {
      return getString("");
    }

    public String getString(String defaultValue) {
      return type == LoggableType.String ? (String) value : defaultValue;
    }

    LogValue(String[] value) {
      type = LoggableType.StringArray;
      this.value = value;
    }

    public String[] getStringArray() {
      return getStringArray(new String[] {});
    }

    public String[] getStringArray(String[] defaultValue) {
      return type == LoggableType.StringArray ? (String[]) value : defaultValue;
    }

    LogValue(byte value) {
      type = LoggableType.Byte;
      this.value = value;
    }

    public byte getByte() {
      return getByte((byte) 0);
    }

    public byte getByte(byte defaultValue) {
      return type == LoggableType.Byte ? (byte) value : defaultValue;
    }

    LogValue(byte[] value) {
      type = LoggableType.ByteArray;
      this.value = value;
    }

    public byte[] getByteArray() {
      return getByteArray(new byte[] {});
    }

    public byte[] getByteArray(byte[] defaultValue) {
      return type == LoggableType.ByteArray ? (byte[]) value : defaultValue;
    }

    public boolean hasChanged(LogValue oldValue) {
      if (oldValue == null) {
        return true;
      }

      if (oldValue.type != type) {
        return true;
      } else {
        switch (type) {
          case Boolean:
          case Integer:
          case Double:
          case String:
          case Byte:
            if (value.equals(oldValue.value)) {
              return false;
            }
            break;
          case BooleanArray:
            if (Arrays.equals(getBooleanArray(), oldValue.getBooleanArray())) {
              return false;
            }
            break;
          case IntegerArray:
            if (Arrays.equals(getIntegerArray(), oldValue.getIntegerArray())) {
              return false;
            }
            break;
          case DoubleArray:
            if (Arrays.equals(getDoubleArray(), oldValue.getDoubleArray())) {
              return false;
            }
            break;
          case StringArray:
            if (Arrays.equals(getStringArray(), oldValue.getStringArray())) {
              return false;
            }
            break;
          case ByteArray:
            if (Arrays.equals(getByteArray(), oldValue.getByteArray())) {
              return false;
            }
            break;
        }
        return true;
      }
    }
  }

  /**
   * Represents all possible data types that can be logged.
   */
  public enum LoggableType {
    Boolean, BooleanArray, Integer, IntegerArray, Double, DoubleArray, String, StringArray, Byte, ByteArray;
  }
}