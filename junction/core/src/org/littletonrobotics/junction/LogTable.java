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

package org.littletonrobotics.junction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.littletonrobotics.junction.LogTable.LogValue;
import org.littletonrobotics.junction.inputs.LoggableInputs;

import edu.wpi.first.units.mutable.GenericMutableMeasureImpl;
import edu.wpi.first.units.ImmutableMeasure;
import edu.wpi.first.units.MutableMeasure;
import edu.wpi.first.units.Measure;
import edu.wpi.first.units.Unit;
import edu.wpi.first.util.protobuf.Protobuf;
import edu.wpi.first.util.protobuf.ProtobufBuffer;
import edu.wpi.first.util.struct.StructBuffer;
import edu.wpi.first.util.struct.Struct;
import edu.wpi.first.util.struct.StructSerializable;
import edu.wpi.first.util.WPISerializable;
import edu.wpi.first.wpilibj.DriverStation;
import us.hebi.quickbuf.ProtoMessage;

/**
 * A table of logged data in allowable types. Can reference another higher level
 * table.
 */
public class LogTable {
  private static boolean disableProtobufWarning = false;
  private final String prefix;
  private final int depth;
  private final SharedTimestamp timestamp;
  private final Map<String, LogValue> data;
  private final Map<String, StructBuffer<?>> structBuffers;
  private final Map<String, ProtobufBuffer<?, ?>> protoBuffers;
  private final Map<String, Struct<?>> structTypeCache;
  private final Map<String, Protobuf<?, ?>> protoTypeCache;

  /** Timestamp wrapper to enable passing by reference to subtables. */
  private static class SharedTimestamp {
    public long value = 0;

    public SharedTimestamp(long value) {
      this.value = value;
    }
  }

  /** Disables warning message print when logging protobuf values. */
  public static void disableProtobufWarning() {
    disableProtobufWarning = true;
  }

  /** Creates a new LogTable. */
  private LogTable(String prefix, int depth, SharedTimestamp timestamp, Map<String, LogValue> data,
      Map<String, StructBuffer<?>> structBuffers, Map<String, ProtobufBuffer<?, ?>> protoBuffers,
      Map<String, Struct<?>> structTypeCache, Map<String, Protobuf<?, ?>> protoTypeCache) {
    this.prefix = prefix;
    this.depth = depth;
    this.timestamp = timestamp;
    this.data = data;
    this.structBuffers = structBuffers;
    this.protoBuffers = protoBuffers;
    this.structTypeCache = structTypeCache;
    this.protoTypeCache = protoTypeCache;
  }

  /**
   * Creates a new LogTable, to serve as the root table.
   */
  public LogTable(long timestamp) {
    this("/", 0, new SharedTimestamp(timestamp), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(),
        new HashMap<>());
  }

  /**
   * Creates a new LogTable, to reference a subtable.
   */
  private LogTable(String prefix, LogTable parent) {
    this(prefix, parent.depth + 1, parent.timestamp, parent.data, parent.structBuffers, parent.protoBuffers, parent.structTypeCache,
        parent.protoTypeCache);
  }

  /**
   * Creates a new LogTable, copying data from the given source. The original
   * table can be safely modified without affecting the copy.
   */
  public static LogTable clone(LogTable source) {
    Map<String, LogValue> data = new HashMap<String, LogValue>();
    data.putAll(source.data);
    return new LogTable(source.prefix, source.depth, new SharedTimestamp(source.timestamp.value), data, new HashMap<>(),
        new HashMap<>(), new HashMap<>(), new HashMap<>());

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

  /**
   * Checks whether the field can be updated with the specified type (it doesn't
   * exist or is already the correct type). Sends a warning to the Driver Station
   * if the existing type is different.
   */
  private boolean writeAllowed(String key, LoggableType type) {
    LogValue currentValue = data.get(prefix + key);
    if (currentValue == null) {
      return true;
    }
    if (currentValue.type.equals(type)) {
      return true;
    }
    DriverStation.reportWarning(
        "[AdvantageKit] Failed to write to field \""
            + prefix + key
            + "\" - attempted to write "
            + type
            + " value but expected "
            + currentValue.type,
        true);
    return false;
  }

  /**
   * Writes a new generic value to the table. Skipped if the key already exists
   * as a different type.
   */
  public void put(String key, LogValue value) {
    if (value == null)
      return;
    if (writeAllowed(key, value.type)) {
      data.put(prefix + key, value);
    }
  }

  /**
   * Writes a new Raw value to the table. Skipped if the key already exists
   * as a different type.
   */
  public void put(String key, byte[] value) {
    if (value == null)
      return;
    byte[] valueClone = new byte[value.length];
    System.arraycopy(value, 0, valueClone, 0, value.length);
    put(key, new LogValue(valueClone, null));
  }

  /**
   * Writes a new 2D Raw value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, byte[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), value[i]);
    }
  }

  /**
   * Writes a new Boolean value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, boolean value) {
    put(key, new LogValue(value, null));
  }

  /**
   * Writes a new BooleanArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, boolean[] value) {
    if (value == null)
      return;
    boolean[] valueClone = new boolean[value.length];
    System.arraycopy(value, 0, valueClone, 0, value.length);
    put(key, new LogValue(valueClone, null));
  }

  /**
   * Writes a new 2D BooleanArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, boolean[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), value[i]);
    }
  }

  /**
   * Writes a new Integer value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, int value) {
    put(key, (long) value);
  }

  /**
   * Writes a new IntegerArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, int[] value) {
    if (value == null)
      return;
    long[] valueClone = new long[value.length];
    for (int i = 0; i < value.length; i++) {
      valueClone[i] = value[i];
    }
    put(key, new LogValue(valueClone, null));
  }

  /**
   * Writes a new 2D IntegerArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, int[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), value[i]);
    }
  }

  /**
   * Writes a new Integer value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, long value) {
    put(key, new LogValue(value, null));
  }

  /**
   * Writes a new IntegerArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, long[] value) {
    if (value == null)
      return;
    long[] valueClone = new long[value.length];
    System.arraycopy(value, 0, valueClone, 0, value.length);
    put(key, new LogValue(valueClone, null));
  }

  /**
   * Writes a new 2D IntegerArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, long[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), value[i]);
    }
  }

  /**
   * Writes a new Float value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, float value) {
    put(key, new LogValue(value, null));
  }

  /**
   * Writes a new FloatArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, float[] value) {
    if (value == null)
      return;
    float[] valueClone = new float[value.length];
    System.arraycopy(value, 0, valueClone, 0, value.length);
    put(key, new LogValue(valueClone, null));
  }

  /**
   * Writes a new 2D FloatArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, float[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), value[i]);
    }
  }

  /**
   * Writes a new Double value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, double value) {
    put(key, new LogValue(value, null));
  }

  /**
   * Writes a new DoubleArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, double[] value) {
    if (value == null)
      return;
    double[] valueClone = new double[value.length];
    System.arraycopy(value, 0, valueClone, 0, value.length);
    put(key, new LogValue(valueClone, null));
  }

  /**
   * Writes a new 2D DoubleArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, double[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), value[i]);
    }
  }

  /**
   * Writes a new String value to the table. Skipped if the key already exists as
   * a different type.
   */
  public void put(String key, String value) {
    if (value == null)
      return;
    put(key, new LogValue(value, null));
  }

  /**
   * Writes a new StringArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, String[] value) {
    if (value == null)
      return;
    String[] valueClone = new String[value.length];
    System.arraycopy(value, 0, valueClone, 0, value.length);
    put(key, new LogValue(valueClone, null));
  }

  /**
   * Writes a new 2D StringArray value to the table. Skipped if the key already
   * exists as a different type.
   */
  public void put(String key, String[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), value[i]);
    }
  }

  /**
   * Writes a new enum value to the table. Skipped if the key already exists as
   * a different type.
   */
  public <E extends Enum<E>> void put(String key, E value) {
    if (value == null)
      return;
    put(key, new LogValue(value.name(), null));
  }

  /**
   * Writes a new enum array value to the table. Skipped if the key already exists
   * as a different type.
   */
  public <E extends Enum<E>> void put(String key, E[] value) {
    if (value == null)
      return;
    String[] stringValues = new String[value.length];
    for (int i = 0; i < value.length; i++) {
      stringValues[i] = value[i].name();
    }
    put(key, new LogValue(stringValues, null));
  }

  /**
   * Writes a new 2D enum array value to the table. Skipped if the key already
   * exists as a different type.
   */
  public <E extends Enum<E>> void put(String key, E[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), value[i]);
    }
  }

  /**
   * Writes a new Measure value to the table. Skipped if the key already exists as
   * a different type.
   */
  public <U extends Unit> void put(String key, Measure<U> value) {
    if (value == null)
      return;
    put(key, new LogValue(value.baseUnitMagnitude(), null));
  }

  /**
   * Writes a new LoggableInput subtable to the table.
   */
  public <T extends LoggableInputs> void put(String key, T value) {
    if (value == null) return;
    if (this.depth > 100) {
      DriverStation.reportWarning(
            "[AdvantageKit] Detected recursive table structure when logging value to field \""
                + prefix
                + key
                + "\". using LoggableInputs. Consider revising the table structure or refactoring to avoid recursion.",
            false);
      return;
    }
    value.toLog(getSubtable(key));
  }

  private void addStructSchema(Struct<?> struct, Set<String> seen) {
    String typeString = struct.getTypeString();
    String key = "/.schema/" + typeString;
    if (data.containsKey(key)) {
      return;
    }
    if (!seen.add(typeString)) {
      throw new UnsupportedOperationException(typeString + ": circular reference with " + seen);
    }
    try {
      data.put(key, new LogValue(struct.getSchema().getBytes("UTF-8"), "structschema"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    for (Struct<?> inner : struct.getNested()) {
      addStructSchema(inner, seen);
    }
    seen.remove(typeString);
  }

  /**
   * Writes a new struct value to the table. Skipped if the key already exists
   * as a different type.
   */
  @SuppressWarnings("unchecked")
  public <T> void put(String key, Struct<T> struct, T value) {
    if (value == null)
      return;
    if (writeAllowed(key, LoggableType.Raw)) {
      addStructSchema(struct, new HashSet<>());
      if (!structBuffers.containsKey(struct.getTypeString())) {
        structBuffers.put(struct.getTypeString(), StructBuffer.create(struct));
      }
      StructBuffer<T> buffer = (StructBuffer<T>) structBuffers.get(struct.getTypeString());
      ByteBuffer bb = buffer.write(value);
      byte[] array = new byte[bb.position()];
      bb.position(0);
      bb.get(array);
      put(key, new LogValue(array, struct.getTypeString()));
    }
  }

  /**
   * Writes a new struct array value to the table. Skipped if the key already
   * exists as a different type.
   */
  @SuppressWarnings("unchecked")
  public <T> void put(String key, Struct<T> struct, T... value) {
    if (value == null)
      return;
    if (writeAllowed(key, LoggableType.Raw)) {
      addStructSchema(struct, new HashSet<>());
      if (!structBuffers.containsKey(struct.getTypeString())) {
        structBuffers.put(struct.getTypeString(), StructBuffer.create(struct));
      }
      StructBuffer<T> buffer = (StructBuffer<T>) structBuffers.get(struct.getTypeString());
      ByteBuffer bb = buffer.writeArray(value);
      byte[] array = new byte[bb.position()];
      bb.position(0);
      bb.get(array);
      put(key, new LogValue(array, struct.getTypeString() + "[]"));
    }
  }

  /**
   * Writes a new 2D struct array value to the table. Skipped if the key already
   * exists as a different type.
   */
  @SuppressWarnings("unchecked")
  public <T> void put(String key, Struct<T> struct, T[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), struct, value[i]);
    }
  }

  /**
   * Writes a new protobuf value to the table. Skipped if the key already exists
   * as a different type.
   */
  @SuppressWarnings("unchecked")
  public <T, MessageType extends ProtoMessage<?>> void put(String key, Protobuf<T, MessageType> proto, T value) {
    if (value == null)
      return;
    if (writeAllowed(key, LoggableType.Raw)) {
      proto.forEachDescriptor((name) -> data.containsKey("/.schema/" + name), (typeString, schema) -> data
          .put("/.schema/" + typeString, new LogValue(schema, "proto:FileDescriptorProto")));
      if (!protoBuffers.containsKey(proto.getTypeString())) {
        protoBuffers.put(proto.getTypeString(), ProtobufBuffer.create(proto));
      }
      ProtobufBuffer<T, MessageType> buffer = (ProtobufBuffer<T, MessageType>) protoBuffers.get(proto.getTypeString());
      ByteBuffer bb;
      try {
        bb = buffer.write(value);
        byte[] array = new byte[bb.position()];
        bb.position(0);
        bb.get(array);
        put(key, new LogValue(array, proto.getTypeString()));
      } catch (IOException e) {
        e.printStackTrace();
      }

      // Driver Station alert
      if (!disableProtobufWarning) {
        DriverStation.reportWarning(
            "[AdvantageKit] Logging value to field \""
                + prefix
                + key
                + "\" using protobuf encoding. This may cause high loop overruns, please monitor performance or save the value in a different format. Call \"LogTable.disableProtobufWarning()\" to disable this message.",
            false);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Struct<?> findStructType(Class<?> classObj) {
    if (!structTypeCache.containsKey(classObj.getName())) {
      structTypeCache.put(classObj.getName(), null);
      Field field = null;
      try {
        field = classObj.getDeclaredField("struct");
      } catch (NoSuchFieldException | SecurityException e) {
      }
      if (field != null) {
        try {
          structTypeCache.put(classObj.getName(), (Struct<?>) field.get(null));
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
      }
    }
    return structTypeCache.get(classObj.getName());
  }

  @SuppressWarnings("unchecked")
  private Protobuf<?, ?> findProtoType(Class<?> classObj) {
    if (!protoTypeCache.containsKey(classObj.getName())) {
      protoTypeCache.put(classObj.getName(), null);
      Field field = null;
      try {
        field = classObj.getDeclaredField("proto");
      } catch (NoSuchFieldException | SecurityException e) {
      }
      if (field != null) {
        try {
          protoTypeCache.put(classObj.getName(), (Protobuf<?, ?>) field.get(null));
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
      }
    }
    return protoTypeCache.get(classObj.getName());
  }

  /**
   * Writes a new auto serialized value to the table. Skipped if the key already
   * exists as a different type.
   */
  @SuppressWarnings("unchecked")
  public <T extends WPISerializable> void put(String key, T value) {
    if (value == null)
      return;
    // If struct is supported, write as struct
    Struct<T> struct = (Struct<T>) findStructType(value.getClass());
    if (struct != null) {
      put(key, struct, value);
    } else {
      // If protobuf is supported, write as proto
      Protobuf<T, ?> proto = (Protobuf<T, ?>) findProtoType(value.getClass());
      if (proto != null) {
        put(key, proto, value);
      } else {
        DriverStation.reportError(
            "[AdvantageKit] Auto serialization is not supported for type " + value.getClass().getSimpleName(),
            false);
      }
    }
  }

  /**
   * Writes a new auto serialized array value to the table. Skipped if the key
   * already exists as a different type.
   */
  @SuppressWarnings("unchecked")
  public <T extends StructSerializable> void put(String key, T... value) {
    if (value == null)
      return;
    // If struct is supported, write as struct
    Struct<T> struct = (Struct<T>) findStructType(value.getClass().getComponentType());
    if (struct != null) {
      put(key, struct, value);
    } else {
      DriverStation.reportError(
          "[AdvantageKit] Auto serialization is not supported for array type "
              + value.getClass().getComponentType().getSimpleName(),
          false);
    }
  }

  /**
   * Writes a new auto serialized 2D array value to the table. Skipped if the key
   * already exists as a different type.
   */
  @SuppressWarnings("unchecked")
  public <T extends StructSerializable> void put(String key, T[][] value) {
    if (value == null)
      return;
    put(key + "/length", value.length);
    for (int i = 0; i < value.length; i++) {
      put(key + "/" + Integer.toString(i), value[i]);
    }
  }

  /** Reads a generic value from the table. */
  public LogValue get(String key) {
    return data.get(prefix + key);
  }

  /** Reads a Raw value from the table. */
  public byte[] get(String key, byte[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getRaw(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a 2D Raw value from the table. */
  public byte[][] get(String key, byte[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      byte[][] value = new byte[get(key + "/length", 0)][];
      for (int i = 0; i < value.length; i++) {
        value[i] = get(key + "/" + Integer.toString(i), new byte[0]);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Reads a Boolean value from the table. */
  public boolean get(String key, boolean defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getBoolean(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a BooleanArray value from the table. */
  public boolean[] get(String key, boolean[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getBooleanArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a 2D BooleanArray value from the table. */
  public boolean[][] get(String key, boolean[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      boolean[][] value = new boolean[get(key + "/length", 0)][];
      for (int i = 0; i < value.length; i++) {
        value[i] = get(key + "/" + Integer.toString(i), new boolean[0]);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Reads an Integer value from the table. */
  public int get(String key, int defaultValue) {
    if (data.containsKey(prefix + key)) {
      return (int) get(key).getInteger(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads an IntegerArray value from the table. */
  public int[] get(String key, int[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      long[] defaultValueLong = new long[defaultValue.length];
      for (int i = 0; i < defaultValue.length; i++) {
        defaultValueLong[i] = defaultValue[i];
      }
      long[] valueLong = get(key).getIntegerArray(defaultValueLong);
      int[] valueInt = new int[valueLong.length];
      for (int i = 0; i < valueLong.length; i++) {
        valueInt[i] = (int) valueLong[i];
      }
      return valueInt;
    } else {
      return defaultValue;
    }
  }

  /** Reads a 2D IntegerArray value from the table. */
  public int[][] get(String key, int[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      int[][] value = new int[get(key + "/length", 0)][];
      for (int i = 0; i < value.length; i++) {
        value[i] = get(key + "/" + Integer.toString(i), new int[0]);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Reads an Integer value from the table. */
  public long get(String key, long defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getInteger(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads an IntegerArray value from the table. */
  public long[] get(String key, long[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getIntegerArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a 2D IntegerArray value from the table. */
  public long[][] get(String key, long[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      long[][] value = new long[get(key + "/length", 0)][];
      for (int i = 0; i < value.length; i++) {
        value[i] = get(key + "/" + Integer.toString(i), new long[0]);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Reads a Float value from the table. */
  public float get(String key, float defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getFloat(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a FloatArray value from the table. */
  public float[] get(String key, float[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getFloatArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a 2D FloatArray value from the table. */
  public float[][] get(String key, float[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      float[][] value = new float[get(key + "/length", 0)][];
      for (int i = 0; i < value.length; i++) {
        value[i] = get(key + "/" + Integer.toString(i), new float[0]);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Reads a Double value from the table. */
  public double get(String key, double defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getDouble(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a DoubleArray value from the table. */
  public double[] get(String key, double[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getDoubleArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a 2D DoubleArray value from the table. */
  public double[][] get(String key, double[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      double[][] value = new double[get(key + "/length", 0)][];
      for (int i = 0; i < value.length; i++) {
        value[i] = get(key + "/" + Integer.toString(i), new double[0]);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Reads a String value from the table. */
  public String get(String key, String defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getString(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a StringArray value from the table. */
  public String[] get(String key, String[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      return get(key).getStringArray(defaultValue);
    } else {
      return defaultValue;
    }
  }

  /** Reads a 2D StringArray value from the table. */
  public String[][] get(String key, String[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      String[][] value = new String[get(key + "/length", 0)][];
      for (int i = 0; i < value.length; i++) {
        value[i] = get(key + "/" + Integer.toString(i), new String[0]);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Reads an enum value from the table. */
  public <E extends Enum<E>> E get(String key, E defaultValue) {
    if (data.containsKey(prefix + key)) {
      String name = get(key).getString(defaultValue.name());
      return (E) Enum.valueOf(defaultValue.getClass(), name);
    } else {
      return defaultValue;
    }
  }

  /** Reads an enum array value from the table. */
  public <E extends Enum<E>> E[] get(String key, E[] defaultValue) {
    if (data.containsKey(prefix + key)) {
      String[] names = get(key).getStringArray(null);
      if (names == null)
        return defaultValue;
      Class<? extends Enum> enumClass = (Class<? extends Enum>) defaultValue.getClass().getComponentType();
      E[] values = (E[]) Array.newInstance(enumClass, names.length);
      for (int i = 0; i < names.length; i++) {
        values[i] = (E) Enum.valueOf(enumClass, names[i]);
      }
      return values;
    } else {
      return defaultValue;
    }
  }

  /** Reads a 2D enum array value from the table. */
  public <E extends Enum<E>> E[][] get(String key, E[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      int length = get(key + "/length", 0);
      E[][] value = (E[][]) Array.newInstance(defaultValue.getClass().getComponentType(), length);
      for (int i = 0; i < length; i++) {
        E[] defaultItemValue = (E[]) Array.newInstance(defaultValue.getClass().getComponentType().getComponentType(),
            0);
        value[i] = get(key + "/" + Integer.toString(i), defaultItemValue);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Reads a Measure value from the table. */
  public <U extends Unit, M extends Measure<U>> M get(String key, M defaultValue) {
    if (data.containsKey(prefix + key)) {
      double value = get(key).getDouble(defaultValue.baseUnitMagnitude());
      return (M) defaultValue.unit().ofBaseUnits(value);
    } else {
      return defaultValue;
    }
  }

  /** Reads a MutableMeasure value from the table. */
  public <U extends Unit, Base extends Measure<U>, M extends MutableMeasure<U, Base, M>> M get(
      String key, M defaultValue) {
    if (data.containsKey(prefix + key)) {
      double baseValue = get(key).getDouble(defaultValue.baseUnitMagnitude());
      double relativeValue = defaultValue.unit().fromBaseUnits(baseValue);
      return (M) new GenericMutableMeasureImpl<>(relativeValue, baseValue, defaultValue.unit());
    } else {
      return defaultValue;
    }
  }

  /** Reads a LoggableInput subtable from the table. */
  public <T extends LoggableInputs> T get(String key, T defaultValue) {
    if (defaultValue == null) return null;
    defaultValue.fromLog(getSubtable(key));
    return defaultValue;
  }

  /** Reads a struct value from the table. */
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Struct<T> struct, T defaultValue) {
    if (data.containsKey(prefix + key)) {
      if (!structBuffers.containsKey(struct.getTypeString())) {
        structBuffers.put(struct.getTypeString(), StructBuffer.create(struct));
      }
      StructBuffer<T> buffer = (StructBuffer<T>) structBuffers.get(struct.getTypeString());
      return buffer.read(get(key).getRaw());
    } else {
      return defaultValue;
    }
  }

  /** Reads a struct array value from the table. */
  @SuppressWarnings("unchecked")
  public <T> T[] get(String key, Struct<T> struct, T... defaultValue) {
    if (data.containsKey(prefix + key)) {
      if (!structBuffers.containsKey(struct.getTypeString())) {
        structBuffers.put(struct.getTypeString(), StructBuffer.create(struct));
      }
      StructBuffer<T> buffer = (StructBuffer<T>) structBuffers.get(struct.getTypeString());
      return buffer.readArray(get(key).getRaw());
    } else {
      return defaultValue;
    }
  }

  /** Reads a 2D struct array value from the table. */
  @SuppressWarnings("unchecked")
  public <T> T[][] get(String key, Struct<T> struct, T[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      int length = get(key + "/length", 0);
      T[][] value = (T[][]) Array.newInstance(defaultValue.getClass().getComponentType(), length);
      for (int i = 0; i < length; i++) {
        T[] defaultItemValue = (T[]) Array.newInstance(defaultValue.getClass().getComponentType().getComponentType(),
            0);
        value[i] = get(key + "/" + Integer.toString(i), struct, defaultItemValue);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Reads a protobuf value from the table. */
  @SuppressWarnings("unchecked")
  public <T, MessageType extends ProtoMessage<?>> T get(String key, Protobuf<T, MessageType> proto, T defaultValue) {
    if (data.containsKey(prefix + key)) {
      if (!protoBuffers.containsKey(proto.getTypeString())) {
        protoBuffers.put(proto.getTypeString(), ProtobufBuffer.create(proto));
      }
      ProtobufBuffer<T, MessageType> buffer = (ProtobufBuffer<T, MessageType>) protoBuffers.get(proto.getTypeString());
      try {
        return buffer.read(get(key).getRaw());
      } catch (IOException e) {
        e.printStackTrace();
        return defaultValue;
      }
    } else {
      return defaultValue;
    }
  }

  /** Reads a serialized (struct/protobuf) value from the table. */
  @SuppressWarnings("unchecked")
  public <T extends WPISerializable> T get(String key, T defaultValue) {
    if (data.containsKey(prefix + key)) {
      String typeString = data.get(prefix + key).customTypeStr;
      if (typeString.startsWith("struct:")) {
        Struct<T> struct = (Struct<T>) findStructType(defaultValue.getClass());
        if (struct != null) {
          return get(key, struct, defaultValue);
        }
      }
      if (typeString.startsWith("proto:")) {
        Protobuf<T, ?> proto = (Protobuf<T, ?>) findProtoType(defaultValue.getClass());
        if (proto != null) {
          return get(key, proto, defaultValue);
        }
      }
    }
    return defaultValue;
  }

  /** Reads a serialized (struct) array value from the table. */
  @SuppressWarnings("unchecked")
  public <T extends StructSerializable> T[] get(String key, T... defaultValue) {
    if (data.containsKey(prefix + key)) {
      String typeString = data.get(prefix + key).customTypeStr;
      if (typeString.startsWith("struct:")) {
        Struct<T> struct = (Struct<T>) findStructType(defaultValue.getClass().getComponentType());
        if (struct != null) {
          return get(key, struct, defaultValue);
        }
      }
    }
    return defaultValue;
  }

  /** Reads a serialized (struct) array value from the table. */
  @SuppressWarnings("unchecked")
  public <T extends StructSerializable> T[][] get(String key, T[][] defaultValue) {
    if (data.containsKey(prefix + key + "/length")) {
      int length = get(key + "/length", 0);
      T[][] value = (T[][]) Array.newInstance(defaultValue.getClass().getComponentType(), length);
      for (int i = 0; i < length; i++) {
        T[] defaultItemValue = (T[]) Array.newInstance(defaultValue.getClass().getComponentType().getComponentType(),
            0);
        value[i] = get(key + "/" + Integer.toString(i), defaultItemValue);
      }
      return value;
    } else {
      return defaultValue;
    }
  }

  /** Returns a string representation of the table. */
  public String toString() {
    String output = "Timestamp=" + Long.toString(timestamp.value) + "\n";
    output += "Prefix=\"" + prefix + "\"\n";
    output += "{\n";
    for (Map.Entry<String, LogValue> field : getAll(true).entrySet()) {
      output += "\t" + field.getKey() + "[" + field.getValue().type.toString();
      if (field.getValue().customTypeStr != null) {
        output += ","
            + field.getValue().customTypeStr.toString();
      }
      output += "]=";
      LogValue value = field.getValue();
      switch (value.type) {
        case Raw:
          output += Arrays.toString(value.getRaw());
          break;
        case Boolean:
          output += value.getBoolean() ? "true" : "false";
          break;
        case Integer:
          output += Long.toString(value.getInteger());
          break;
        case Float:
          output += Float.toString(value.getFloat());
          break;
        case Double:
          output += Double.toString(value.getDouble());
          break;
        case String:
          output += "\"" + value.getString() + "\"";
          break;
        case BooleanArray:
          output += Arrays.toString(value.getBooleanArray());
          break;
        case IntegerArray:
          output += Arrays.toString(value.getIntegerArray());
          break;
        case FloatArray:
          output += Arrays.toString(value.getFloatArray());
          break;
        case DoubleArray:
          output += Arrays.toString(value.getDoubleArray());
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
      }
      output += "\n";
    }
    output += "}";
    return output;
  }

  /**
   * Represents a value stored in a LogTable, including type and value.
   */
  public static class LogValue {
    public final LoggableType type;
    public final String customTypeStr;
    private final Object value;

    public LogValue(byte[] value, String typeStr) {
      type = LoggableType.Raw;
      customTypeStr = typeStr;
      this.value = value;
    }

    public LogValue(boolean value, String typeStr) {
      type = LoggableType.Boolean;
      customTypeStr = typeStr;
      this.value = value;
    }

    public LogValue(long value, String typeStr) {
      type = LoggableType.Integer;
      customTypeStr = typeStr;
      this.value = value;
    }

    public LogValue(float value, String typeStr) {
      type = LoggableType.Float;
      customTypeStr = typeStr;
      this.value = value;
    }

    public LogValue(double value, String typeStr) {
      type = LoggableType.Double;
      customTypeStr = typeStr;
      this.value = value;
    }

    public LogValue(String value, String typeStr) {
      type = LoggableType.String;
      customTypeStr = typeStr;
      if (value != null) {
        this.value = value;
      } else {
        this.value = "";
      }
    }

    public LogValue(boolean[] value, String typeStr) {
      type = LoggableType.BooleanArray;
      customTypeStr = typeStr;
      this.value = value;
    }

    public LogValue(long[] value, String typeStr) {
      type = LoggableType.IntegerArray;
      customTypeStr = typeStr;
      this.value = value;
    }

    public LogValue(float[] value, String typeStr) {
      type = LoggableType.FloatArray;
      customTypeStr = typeStr;
      this.value = value;
    }

    public LogValue(double[] value, String typeStr) {
      type = LoggableType.DoubleArray;
      customTypeStr = typeStr;
      this.value = value;
    }

    public LogValue(String[] value, String typeStr) {
      type = LoggableType.StringArray;
      customTypeStr = typeStr;
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

    /**
     * Returns the standard string type for WPILOGs. Returns the custom type string
     * if not null.
     */
    public String getWPILOGType() {
      if (customTypeStr == null) {
        return type.getWPILOGType();
      } else {
        return customTypeStr;
      }
    }

    /**
     * Returns the standard string type for NT4. Returns the custom type string if
     * not null.
     */
    public String getNT4Type() {
      if (customTypeStr == null) {
        return type.getNT4Type();
      } else {
        return customTypeStr;
      }
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
      return Objects.hash(type, customTypeStr, value);
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
        return LoggableType.Raw;
      }
    }

    /**
     * Returns the type based on a standard string type for NT4.
     */
    public static LoggableType fromNT4Type(String type) {
      if (nt4Types.contains(type)) {
        return LoggableType.values()[nt4Types.indexOf(type)];
      } else {
        return LoggableType.Raw;
      }
    }
  }
}