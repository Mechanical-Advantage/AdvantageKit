// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import edu.wpi.first.util.struct.Struct;
import edu.wpi.first.util.struct.StructSerializable;
import edu.wpi.first.wpilibj.DriverStation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
class RecordStruct implements Struct {
  private final Class<?> recordClass;
  private Constructor<?> recordConstructor = null;
  private final String typeName;
  private final int size;
  private final String schema;
  private final Struct<?>[] nestedStructs;

  private final List<BiConsumer<ByteBuffer, Object>> packFunctions = new ArrayList<>();
  private final List<Function<ByteBuffer, Object>> unpackFunctions = new ArrayList<>();

  @SuppressWarnings({"unchecked", "unused"})
  public RecordStruct(Class<?> recordClass) {
    this.recordClass = recordClass;
    typeName = recordClass.getSimpleName();

    int size = 0;
    var schema = new StringBuilder();
    var components = recordClass.getRecordComponents();
    List<Struct<?>> nestedStructs = new LinkedList<>();
    Class<?>[] parameterTypes = new Class[components.length];
    for (int i = 0; i < components.length; i++) {
      RecordComponent component = components[i];

      // Add to parameter types
      parameterTypes[i] = component.getType();

      // Get value accessor
      Method accessor = component.getAccessor();
      accessor.setAccessible(true);

      // Add functions based on type
      if (component.getType().equals(boolean.class)) {
        size += 1;
        schema.append("bool ");
        schema.append(component.getName());
        schema.append(";");
        packFunctions.add(
            (ByteBuffer bb, Object record) -> {
              try {
                bb.put(((boolean) accessor.invoke(record)) ? (byte) 1 : (byte) 0);
              } catch (IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException e) {
                e.printStackTrace();
              }
            });
        unpackFunctions.add((ByteBuffer bb) -> bb.get() != 0);

      } else if (component.getType().equals(short.class)) {
        size += 2;
        schema.append("int16 ");
        schema.append(component.getName());
        schema.append(";");
        packFunctions.add(
            (ByteBuffer bb, Object record) -> {
              try {
                bb.putShort((short) accessor.invoke(record));
              } catch (IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException e) {
                e.printStackTrace();
              }
            });
        unpackFunctions.add((ByteBuffer bb) -> bb.getShort());

      } else if (component.getType().equals(int.class)) {
        size += 4;
        schema.append("int32 ");
        schema.append(component.getName());
        schema.append(";");
        packFunctions.add(
            (ByteBuffer bb, Object record) -> {
              try {
                bb.putInt((int) accessor.invoke(record));
              } catch (IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException e) {
                e.printStackTrace();
              }
            });
        unpackFunctions.add((ByteBuffer bb) -> bb.getInt());

      } else if (component.getType().equals(long.class)) {
        size += 8;
        schema.append("int64 ");
        schema.append(component.getName());
        schema.append(";");
        packFunctions.add(
            (ByteBuffer bb, Object record) -> {
              try {
                bb.putLong((long) accessor.invoke(record));
              } catch (IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException e) {
                e.printStackTrace();
              }
            });
        unpackFunctions.add((ByteBuffer bb) -> bb.getLong());

      } else if (component.getType().equals(float.class)) {
        size += 4;
        schema.append("float ");
        schema.append(component.getName());
        schema.append(";");
        packFunctions.add(
            (ByteBuffer bb, Object record) -> {
              try {
                bb.putFloat((float) accessor.invoke(record));
              } catch (IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException e) {
                e.printStackTrace();
              }
            });
        unpackFunctions.add((ByteBuffer bb) -> bb.getFloat());

      } else if (component.getType().equals(double.class)) {
        size += 8;
        schema.append("double ");
        schema.append(component.getName());
        schema.append(";");
        packFunctions.add(
            (ByteBuffer bb, Object record) -> {
              try {
                bb.putDouble((double) accessor.invoke(record));
              } catch (IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException e) {
                e.printStackTrace();
              }
            });
        unpackFunctions.add((ByteBuffer bb) -> bb.getDouble());

      } else if (component.getType().isEnum()) {
        size += 4;
        Enum[] enumValues = (Enum[]) component.getType().getEnumConstants();
        schema.append("enum {");
        for (int j = 0; j < enumValues.length; j++) {
          schema.append(enumValues[j].name());
          schema.append("=");
          schema.append(enumValues[j].ordinal());
          if (j < enumValues.length - 1) {
            schema.append(", ");
          }
        }
        schema.append("} int32 ");
        schema.append(component.getName());
        schema.append(";");
        packFunctions.add(
            (ByteBuffer bb, Object record) -> {
              try {
                bb.putInt(((Enum) accessor.invoke(record)).ordinal());
              } catch (IllegalAccessException
                  | IllegalArgumentException
                  | InvocationTargetException e) {
                e.printStackTrace();
              }
            });
        unpackFunctions.add((ByteBuffer bb) -> enumValues[bb.getInt()]);

      } else if (component.getType().isRecord()
          || StructSerializable.class.isAssignableFrom(component.getType())) {
        Struct<?> struct = null;
        if (component.getType().isRecord()) {
          struct = new RecordStruct(component.getType());
        } else {
          try {
            struct = (Struct) component.getType().getDeclaredField("struct").get(null);
          } catch (IllegalArgumentException
              | IllegalAccessException
              | NoSuchFieldException
              | SecurityException e) {
            e.printStackTrace();
          }
        }

        if (struct == null) {
          DriverStation.reportError(
              "[AdvantageKit] Failed to load nested struct \""
                  + component.getName()
                  + "\" for record type \""
                  + recordClass.getSimpleName()
                  + "\"",
              true);
          packFunctions.add((ByteBuffer bb, Object record) -> {});
          unpackFunctions.add((ByteBuffer bb) -> null);
        } else {
          size += struct.getSize();
          schema.append(struct.getTypeName());
          schema.append(" ");
          schema.append(component.getName());
          schema.append(";");
          nestedStructs.add(struct);
          Struct rawStruct = struct;
          packFunctions.add(
              (ByteBuffer bb, Object record) -> {
                try {
                  rawStruct.pack(bb, accessor.invoke(record));
                } catch (IllegalAccessException
                    | IllegalArgumentException
                    | InvocationTargetException e) {
                  e.printStackTrace();
                }
              });
          unpackFunctions.add((ByteBuffer bb) -> rawStruct.unpack(bb));
        }

      } else {
        DriverStation.reportError(
            "[AdvantageKit] Field \""
                + component.getName()
                + "\" for record type \""
                + recordClass.getSimpleName()
                + "\" uses an unsupported type and will not be logged. Check the AdvantageKit documentation for details on record logging.",
            true);
        packFunctions.add((ByteBuffer bb, Object record) -> {});
        unpackFunctions.add((ByteBuffer bb) -> null);
      }
    }

    // Save schema
    this.size = size;
    this.schema = schema.toString();

    // Save nested structs
    this.nestedStructs = new Struct[nestedStructs.size()];
    for (int i = 0; i < nestedStructs.size(); i++) {
      this.nestedStructs[i] = nestedStructs.get(i);
    }

    // Get constructor
    try {
      this.recordConstructor = recordClass.getDeclaredConstructor(parameterTypes);
      this.recordConstructor.setAccessible(true);
    } catch (NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Class<?> getTypeClass() {
    return recordClass;
  }

  @Override
  public String getTypeName() {
    return typeName;
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public String getSchema() {
    return schema;
  }

  @Override
  public Struct<?>[] getNested() {
    return nestedStructs;
  }

  @Override
  public Object unpack(ByteBuffer bb) {
    // Exit if no constructor available
    if (recordConstructor == null) {
      return null;
    }

    // Unpack elements
    Object[] elements = new Object[unpackFunctions.size()];
    for (int i = 0; i < unpackFunctions.size(); i++) {
      elements[i] = unpackFunctions.get(i).apply(bb);
    }

    // Construct record
    Object output = null;
    try {
      output = recordConstructor.newInstance(elements);
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException e) {
      e.printStackTrace();
    }
    return output;
  }

  @Override
  public void pack(ByteBuffer bb, Object value) {
    for (var function : packFunctions) {
      function.accept(bb, value);
    }
  }

  @Override
  public boolean isImmutable() {
    return true;
  }
}
