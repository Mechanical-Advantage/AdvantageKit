package org.littletonrobotics.junction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A table of logged data in allowable types. Can reference another higher level
 * table.
 */
public class LogTable {
  private final String prefix;
  private final SharedTimestamp timestamp;
  private final Map<String, LogValue> data;

  /** Timestamp wrapper to enable passing by reference to subtables. */
  private static class SharedTimestamp {
    public long value;

    public SharedTimestamp(long value) {
      this.value = value;
    }
  }

  /** Creates a new LogTable. */
  private LogTable(String prefix, SharedTimestamp timestamp, Map<String, LogValue> data) {
    this.prefix = prefix;
    this.timestamp = timestamp;
    this.data = data;
  }

  /**
   * Creates a new LogTable, to serve as the root table.
   */
  public LogTable(long timestamp) {
    this("/", new SharedTimestamp(timestamp),
            new HashMap<>());
  }

  /**
   * Creates a new LogTable, to reference a subtable.
   */
  private LogTable(String prefix, LogTable parent) {
    this(prefix, parent.timestamp, parent.data);
  }

  /**
   * Creates a new LogTable, copying data from the given source. The original
   * table can be safely modified without affecting the copy.
   */
  public static LogTable clone(LogTable source) {
    Map<String, LogValue> data = new HashMap<>(source.data);
    return new LogTable(source.prefix, new SharedTimestamp(source.timestamp.value), data);

  }

  /**
   * Updates the timestamp of the table.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp.value = timestamp;
  }

  /**
   * Returns the timestamp of the table.
   */
  public long getTimestamp() {
    return timestamp.value;
  }

  /**
   * Creates a new LogTable for referencing a single subtable. Modifications to
   * the subtable will be reflected in the original object.
   * 
   * @param tableName The name of the subtable. Do not include a trailing slash.
   * @return The subtable object.
   */
  public LogTable getSubtable(String tableName) {
    return new LogTable(prefix + tableName + "/", this);
  }

  /**
   * Returns a set of all values from the table. If reading a single subtable, the
   * data will be a copy. Otherwise, it will be a reference.
   * 
   * @param subtableOnly If true, include only values in the subtable (no prefix).
   *                     If false, include all values.
   * @return Map of the requested data.
   */
  public Map<String, LogValue> getAll(boolean subtableOnly) {
    if (subtableOnly) {
      Map<String, LogValue> result = new HashMap<>();
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

  /**
   * Checks whether the field can be updated with the specified type (it doesn't
   * exist or is already the correct type).
   */
  private boolean writeAllowed(String key, LoggableType type) {
    LogValue currentValue = data.get(key);
    if (currentValue == null) {
      return true;
    }
    return currentValue.type.equals(type);
  }

  /**
   * Writes a new Raw value to the table. Skipped if the key already exists
   * as a different type.
   */
  public void put(String key, byte[] value) {
    if (writeAllowed(key, LoggableType.Raw)) {
      byte[] valueClone = new byte[value.length];
      System.arraycopy(value, 0, valueClone, 0, value.length);
      data.put(prefix + key,
              new LogValue(valueClone)
      );
    }
  }

  /**
   * Writes a new Boolean value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, boolean value) {
    if (writeAllowed(key, LoggableType.Boolean)) {
      data.put(prefix + key,
              new LogValue(value)
      );
    }
  }

  /**
   * Writes a new Integer value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, long value) {
    if (writeAllowed(key, LoggableType.Integer)) {
      data.put(prefix + key,
              new LogValue(value)
      );
    }
  }

  /**
   * Writes a new Float value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, float value) {
    if (writeAllowed(key, LoggableType.Float)) {
      data.put(prefix + key,
              new LogValue(value)
      );
    }
  }

  /**
   * Writes a new Double value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, double value) {
    if (writeAllowed(key, LoggableType.Double)) {
      data.put(prefix + key,
              new LogValue(value)
      );
    }
  }

  /**
   * Writes a new String value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, String value) {
    if (writeAllowed(key, LoggableType.String)) {
      data.put(prefix + key,
              new LogValue(value)
      );
    }
  }

  /**
   * Writes a new BooleanArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, boolean[] value) {
    if (writeAllowed(key, LoggableType.BooleanArray)) {
      boolean[] valueClone = new boolean[value.length];
      System.arraycopy(value, 0, valueClone, 0, value.length);
      data.put(prefix + key,
              new LogValue(valueClone)
      );
    }
  }

  /**
   * Writes a new IntegerArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, long[] value) {
    if (writeAllowed(key, LoggableType.IntegerArray)) {
      long[] valueClone = new long[value.length];
      System.arraycopy(value, 0, valueClone, 0, value.length);
      data.put(prefix + key,
              new LogValue(valueClone)
      );
    }
  }

  /**
   * Writes a new FloatArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, float[] value) {
    if (writeAllowed(key, LoggableType.FloatArray)) {
      float[] valueClone = new float[value.length];
      System.arraycopy(value, 0, valueClone, 0, value.length);
      data.put(prefix + key,
              new LogValue(valueClone)
      );
    }
  }

  /**
   * Writes a new DoubleArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, double[] value) {
    if (writeAllowed(key, LoggableType.DoubleArray)) {
      double[] valueClone = new double[value.length];
      System.arraycopy(value, 0, valueClone, 0, value.length);
      data.put(prefix + key,
              new LogValue(valueClone)
      );
    }
  }

  /**
   * Writes a new StringArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, String[] value) {
    if (writeAllowed(key, LoggableType.StringArray)) {
      String[] valueClone = new String[value.length];
      System.arraycopy(value, 0, valueClone, 0, value.length);
      data.put(prefix + key,
              new LogValue(valueClone)
      );
    }
  }

  /** Reads a generic value from the table. */
  public LogValue get(String key) {
    return data.get(prefix + key);
  }

  /** Reads a Raw value from the table. */
  public byte[] getRaw(String key, byte[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getRaw(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a Boolean value from the table. */
  public boolean getBoolean(String key, boolean defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getBoolean(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads an Integer value from the table. */
  public long getInteger(String key, long defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getInteger(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a Float value from the table. */
  public float getFloat(String key, float defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getFloat(defaultValue);
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

  /** Reads a String value from the table. */
  public String getString(String key, String defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getString(defaultValue);
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

  /** Reads a IntegerArray value from the table. */
  public long[] getIntegerArray(String key, long[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getIntegerArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a FloatArray value from the table. */
  public float[] getFloatArray(String key, float[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getFloatArray(defaultValue);
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

  /** Reads a StringArray value from the table. */
  public String[] getStringArray(String key, String[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getStringArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Returns a string representation of the table. */
  public String toString() {
    StringBuilder output = new StringBuilder("Timestamp=" + timestamp.value + "\n");
    output.append("Prefix=\"").append(prefix).append("\"\n");
    output.append("{\n");
    for (Map.Entry<String, LogValue> field : getAll(true).entrySet()) {
      output.append("\t").append(field.getKey()).append("=");
      LogValue value = field.getValue();
      switch (value.type) {
        case Raw:
          output.append(Arrays.toString(value.getRaw()));
          break;
        case Boolean:
          output.append(value.getBoolean() ? "true" : "false");
          break;
        case Integer:
          output.append(value.getInteger());
          break;
        case Float:
          output.append(value.getFloat());
          break;
        case Double:
          output.append(value.getDouble());
          break;
        case String:
          output.append("\"").append(value.getString()).append("\"");
          break;
        case BooleanArray:
          output.append(Arrays.toString(value.getBooleanArray()));
          break;
        case IntegerArray:
          output.append(Arrays.toString(value.getIntegerArray()));
          break;
        case FloatArray:
          output.append(Arrays.toString(value.getFloatArray()));
          break;
        case DoubleArray:
          output.append(Arrays.toString(value.getDoubleArray()));
          break;
        case StringArray:
          output.append("[");
          String[] stringArray = value.getStringArray();
          for (int i = 0; i < stringArray.length; i++) {
            output.append("\"").append(stringArray[i]).append("\"");
            output.append(i < stringArray.length - 1 ? "," : "");
          }
          output.append("]");
          break;
      }
      output.append("\n");
    }
    output.append("}");
    return output.toString();
  }

  /**
   * Represents a value stored in a LogTable, including type and value.
   */
  public static class LogValue {
    public final LoggableType type;
    private final Object value;

    LogValue(byte[] value) {
      type = LoggableType.Raw;
      this.value = value;
    }

    LogValue(boolean value) {
      type = LoggableType.Boolean;
      this.value = value;
    }

    LogValue(long value) {
      type = LoggableType.Integer;
      this.value = value;
    }

    LogValue(float value) {
      type = LoggableType.Float;
      this.value = value;
    }

    LogValue(double value) {
      type = LoggableType.Double;
      this.value = value;
    }

    LogValue(String value) {
      type = LoggableType.String;
      this.value = Objects.requireNonNullElse(
              value,
              ""
      );
    }

    LogValue(boolean[] value) {
      type = LoggableType.BooleanArray;
      this.value = value;
    }

    LogValue(long[] value) {
      type = LoggableType.IntegerArray;
      this.value = value;
    }

    LogValue(float[] value) {
      type = LoggableType.FloatArray;
      this.value = value;
    }

    LogValue(double[] value) {
      type = LoggableType.DoubleArray;
      this.value = value;
    }

    LogValue(String[] value) {
      type = LoggableType.StringArray;
      this.value = value;
    }

    public byte[] getRaw() {
      return getRaw(new byte[] {});
    }

    public boolean getBoolean() {
      return getBoolean(false);
    }

    public long getInteger() {
      return getInteger(0);
    }

    public float getFloat() {
      return getFloat(0.0f);
    }

    public double getDouble() {
      return getDouble(0.0);
    }

    public String getString() {
      return getString("");
    }

    public boolean[] getBooleanArray() {
      return getBooleanArray(new boolean[] {});
    }

    public long[] getIntegerArray() {
      return getIntegerArray(new long[] {});
    }

    public float[] getFloatArray() {
      return getFloatArray(new float[] {});
    }

    public double[] getDoubleArray() {
      return getDoubleArray(new double[] {});
    }

    public String[] getStringArray() {
      return getStringArray(new String[] {});
    }

    public byte[] getRaw(byte[] defaultValue) {
      return type == LoggableType.Raw ? (byte[]) value : defaultValue;
    }

    public boolean getBoolean(boolean defaultValue) {
      return type == LoggableType.Boolean ? (boolean) value : defaultValue;
    }

    public long getInteger(long defaultValue) {
      return type == LoggableType.Integer ? (long) value : defaultValue;
    }

    public float getFloat(float defaultValue) {
      return type == LoggableType.Float ? (float) value : defaultValue;
    }

    public double getDouble(double defaultValue) {
      return type == LoggableType.Double ? (double) value : defaultValue;
    }

    public String getString(String defaultValue) {
      return type == LoggableType.String ? (String) value : defaultValue;
    }

    public boolean[] getBooleanArray(boolean[] defaultValue) {
      return type == LoggableType.BooleanArray ? (boolean[]) value : defaultValue;
    }

    public long[] getIntegerArray(long[] defaultValue) {
      return type == LoggableType.IntegerArray ? (long[]) value : defaultValue;
    }

    public float[] getFloatArray(float[] defaultValue) {
      return type == LoggableType.FloatArray ? (float[]) value : defaultValue;
    }

    public double[] getDoubleArray(double[] defaultValue) {
      return type == LoggableType.DoubleArray ? (double[]) value : defaultValue;
    }

    public String[] getStringArray(String[] defaultValue) {
      return type == LoggableType.StringArray ? (String[]) value : defaultValue;
    }

    @Override
    public boolean equals(Object other) {
      if (other instanceof LogValue) {
        LogValue otherValue = (LogValue) other;
        if (otherValue.type.equals(type)) {
          switch (type) {
            case Raw:
              return Arrays.equals(getRaw(), otherValue.getRaw());
            case Boolean:
            case Integer:
            case Float:
            case Double:
            case String:
              return value.equals(otherValue.value);
            case BooleanArray:
              return Arrays.equals(getBooleanArray(), otherValue.getBooleanArray());
            case IntegerArray:
              return Arrays.equals(getIntegerArray(), otherValue.getIntegerArray());
            case FloatArray:
              return Arrays.equals(getFloatArray(), otherValue.getFloatArray());
            case DoubleArray:
              return Arrays.equals(getDoubleArray(), otherValue.getDoubleArray());
            case StringArray:
              return Arrays.equals(getStringArray(), otherValue.getStringArray());
          }
        }
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, value);
    }
  }

  /**
   * Represents all possible data types that can be logged.
   */
  public enum LoggableType {
    Raw, Boolean, Integer, Float, Double, String, BooleanArray, IntegerArray, FloatArray, DoubleArray, StringArray;

    // https://github.com/wpilibsuite/allwpilib/blob/main/wpiutil/doc/datalog.adoc#data-types
    private static final List<String> wpilogTypes = List.of("raw", "boolean", "int64", "float", "double", "string",
        "boolean[]", "int64[]", "float[]", "double[]", "string[]");

    // https://github.com/wpilibsuite/allwpilib/blob/main/ntcore/doc/networktables4.adoc#supported-data-types
    private static final List<String> nt4Types = List.of("raw", "boolean", "int", "float", "double", "string",
        "boolean[]", "int[]", "float[]", "double[]", "string[]");

    /**
     * Returns the standard string type for WPILOGs.
     */
    public String getWPILOGType() {
      return wpilogTypes.get(this.ordinal());
    }

    /**
     * Returns the standard string type for NT4.
     */
    public String getNT4Type() {
      return nt4Types.get(this.ordinal());
    }

    /**
     * Returns the type based on a standard string type for WPILOGs.
     */
    public static LoggableType fromWPILOGType(String type) {
      if (wpilogTypes.contains(type)) {
        return LoggableType.values()[wpilogTypes.indexOf(type)];
      } else {
        return null;
      }
    }

    /**
     * Returns the type based on a standard string type for NT4.
     */
    public static LoggableType fromNT4Type(String type) {
      if (nt4Types.contains(type)) {
        return LoggableType.values()[nt4Types.indexOf(type)];
      } else {
        return null;
      }
    }
  }
}