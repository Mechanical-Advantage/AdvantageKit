// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import edu.wpi.first.units.Measure;
import edu.wpi.first.util.WPISerializable;
import edu.wpi.first.util.struct.StructSerializable;
import edu.wpi.first.wpilibj.DriverStation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;

/**
 * Manages objects and packages for annotation logging of outputs with {@link
 * org.littletonrobotics.junction.AutoLogOutput AutoLogOutput}.
 */
public class AutoLogOutputManager {
  private static final List<Runnable> callbacks = new ArrayList<>();
  private static final List<Integer> scannedObjectHashes = new ArrayList<>();
  private static final Set<String> allowedPackages = new HashSet<>();

  private AutoLogOutputManager() {}

  /**
   * Adds a new allowed package to use when scanning for annotations. By default, the parent class
   * where {@code @AutoLogOutput} is used must be within the same package as {@code Robot} (or a
   * subpackage). Calling this method registers a new allowed package, such as a "lib" package
   * outside of normal robot code.
   *
   * <p>This method must be called within the constructor of {@code Robot}.
   *
   * @param packageName The new allowed package name (e.g. "frc.lib")
   */
  public static void addPackage(String packageName) {
    allowedPackages.add(packageName);
  }

  /** Records values from all registered fields. */
  static void periodic() {
    for (Runnable callback : callbacks) {
      callback.run();
    }
  }

  /**
   * Registers a root object, scanning for loggable fields recursively.
   *
   * @param root The object to scan recursively.
   */
  public static void addObject(Object root) {
    allowedPackages.add(root.getClass().getPackageName());
    addObjectImpl(root);
  }

  /**
   * Registers a root object, scanning for loggable fields recursively.
   *
   * @param root The object to scan recursively.
   */
  private static void addObjectImpl(Object root) {
    // Check if package name is valid
    String packageName = root.getClass().getPackageName();
    boolean packageNameValid = false;
    for (String allowedPackage : allowedPackages) {
      if (packageName.startsWith(allowedPackage)) {
        packageNameValid = true;
        break;
      }
    }
    if (!packageNameValid) return;

    // Check if object has already been scanned
    if (scannedObjectHashes.contains(root.hashCode())) return;
    scannedObjectHashes.add(root.hashCode());

    // If array, loop over individual items
    if (root.getClass().isArray()) {
      Object[] rootArray = (Object[]) root;
      for (Object item : rootArray) {
        if (item != null) {
          addObjectImpl(item);
        }
      }
      return;
    }

    // Loop over declared methods
    getAllMethods(root.getClass())
        .forEach(
            (methodAndDeclaringClass) -> {
              Method method = methodAndDeclaringClass.method;
              Class<?> declaringClass = methodAndDeclaringClass.declaringClass;
              if (!method.trySetAccessible()) return;

              // If annotated, try to add
              if (method.isAnnotationPresent(AutoLogOutput.class)) {
                // Exit if invalid signature
                if (method.getReturnType().equals(Void.TYPE)
                    || method.getParameterCount() > 0
                    || method.getExceptionTypes().length > 0) {
                  return;
                }

                // Get parameters
                AutoLogOutput annotation = method.getAnnotation(AutoLogOutput.class);
                String key = makeKey(annotation.key(), method.getName(), declaringClass, root);
                boolean forceSerializable = annotation.forceSerializable();
                String unit = annotation.unit();

                // Register method
                registerField(
                    key,
                    method.getReturnType(),
                    () -> {
                      try {
                        return method.invoke(root);
                      } catch (IllegalAccessException
                          | IllegalArgumentException
                          | InvocationTargetException e) {
                        e.printStackTrace();
                        return null;
                      }
                    },
                    forceSerializable,
                    unit);
              }
            });

    // Loop over declared fields
    getAllFields(root.getClass())
        .forEach(
            (fieldAndDeclaringClass) -> {
              Field field = fieldAndDeclaringClass.field;
              Class<?> declaringClass = fieldAndDeclaringClass.declaringClass;
              if (!field.trySetAccessible()) return;

              // If annotated, try to add
              if (field.isAnnotationPresent(AutoLogOutput.class)) {
                // Get parameters
                AutoLogOutput annotation = field.getAnnotation(AutoLogOutput.class);
                String key = makeKey(annotation.key(), field.getName(), declaringClass, root);
                boolean forceSerializable = annotation.forceSerializable();
                String unit = annotation.unit();

                // Register field
                registerField(
                    key,
                    field.getType(),
                    () -> {
                      try {
                        return field.get(root);
                      } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                      }
                    },
                    forceSerializable,
                    unit);
                return;
              }

              // Scan field value
              Object fieldValue = null;
              try {
                fieldValue = field.get(root);
              } catch (IllegalArgumentException | IllegalAccessException e) {
                return;
              }
              if (fieldValue != null) {
                addObjectImpl(fieldValue);
              }
            });
  }

  /** Returns the set of all methods on the class and its superclasses (public and private). */
  private static List<MethodAndDeclaringClass> getAllMethods(Class<?> type) {
    List<MethodAndDeclaringClass> methods = new ArrayList<>();
    while (type != null && type != Object.class) {
      for (Method method : type.getDeclaredMethods()) {
        methods.add(new MethodAndDeclaringClass(method, type));
      }
      type = type.getSuperclass();
    }
    return methods;
  }

  private static class MethodAndDeclaringClass {
    public final Method method;
    public final Class<?> declaringClass;

    public MethodAndDeclaringClass(Method method, Class<?> declaringClass) {
      this.method = method;
      this.declaringClass = declaringClass;
    }
  }
  ;

  /** Returns the set of all fields in the class and its superclasses (public and private). */
  private static List<FieldAndDeclaringClass> getAllFields(Class<?> type) {
    List<FieldAndDeclaringClass> fields = new ArrayList<>();
    while (type != null && type != Object.class) {
      for (Field field : type.getDeclaredFields()) {
        fields.add(new FieldAndDeclaringClass(field, type));
      }
      type = type.getSuperclass();
    }
    return fields;
  }

  private static class FieldAndDeclaringClass {
    public final Field field;
    public final Class<?> declaringClass;

    public FieldAndDeclaringClass(Field field, Class<?> declaringClass) {
      this.field = field;
      this.declaringClass = declaringClass;
    }
  }

  /**
   * Finds the field in the provided class and its superclasses (must be public or protected in
   * superclasses). Returns null if the field cannot be found.
   */
  private static Field findField(Class<?> type, String fieldName) {
    try {
      return type.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      // Not in original class, check superclasses
      type = type.getSuperclass();
      while (type != null && type != Object.class) {
        try {
          Field field = type.getDeclaredField(fieldName);
          if (Modifier.isPublic(field.getModifiers())
              || Modifier.isProtected(field.getModifiers())) {
            return field;
          }
        } catch (NoSuchFieldException | SecurityException e1) {
        }
        type = type.getSuperclass();
      }
      return null;
    } catch (SecurityException e) {
      return null;
    }
  }

  /**
   * Generates a log key based on the field properties.
   *
   * @param keyParameter The user-provided key from the annotation
   * @param valueName The name of the field or method
   * @param declaringClass The class where this fields or method is declared
   * @param parent The parent object to read data from
   */
  private static String makeKey(
      String keyParameter, String valueName, Class<?> declaringClass, Object parent) {
    if (keyParameter.length() == 0) {
      // Auto generate from parent and value
      String key = declaringClass.getSimpleName() + "/";
      if (valueName.startsWith("get") && valueName.length() > 3) {
        valueName = valueName.substring(3);
      }
      key += valueName.substring(0, 1).toUpperCase() + valueName.substring(1);
      return key;
    } else {
      // Fill in field values
      String key = keyParameter;
      while (true) {
        // Find field name
        int openIndex = key.indexOf("{");
        if (openIndex == -1) break; // No more brackets
        int closeIndex = key.indexOf("}", openIndex);
        if (closeIndex == -1) break; // No closing bracket
        String fieldName = key.substring(openIndex + 1, closeIndex);

        // Get field value
        String fieldValue = "";
        Field field;
        try {
          field = findField(declaringClass, fieldName);
          field.setAccessible(true);
          fieldValue = field.get(parent).toString();
        } catch (SecurityException
            | IllegalArgumentException
            | IllegalAccessException
            | NullPointerException e) {
          // Use default field value
        }

        // Replace in key
        key = key.substring(0, openIndex) + fieldValue + key.substring(closeIndex + 1);
      }
      return key;
    }
  }

  /**
   * Registers the periodic callback for a single field.
   *
   * @param key The string key to use for logging.
   * @param type The type of object being logged.
   * @param supplier A supplier for the field values.
   * @param forceSerializable Whether or not to always use a serialized data method.
   * @param unit The unit metadata.
   */
  private static void registerField(
      String key, Class<?> type, Supplier<?> supplier, boolean forceSerializable, String unit) {
    if (forceSerializable) {
      callbacks.add(
          () -> {
            Object value = supplier.get();
            if (value != null)
              try {
                Logger.recordOutput(key, (WPISerializable) value);
              } catch (ClassCastException e) {
                DriverStation.reportError(
                    "[AdvantageKit] Auto serialization is not supported for type "
                        + type.getSimpleName(),
                    false);
              }
          });
      return;
    }

    if (!type.isArray()) {
      // Single types
      if (type.equals(boolean.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (boolean) value);
            });
      } else if (type.equals(int.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (int) value);
            });
      } else if (type.equals(long.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (long) value);
            });
      } else if (type.equals(float.class)) {
        if (unit.length() > 0) {
          callbacks.add(
              () -> {
                Object value = supplier.get();
                if (value != null) Logger.recordOutput(key, (float) value, unit);
              });
        } else {
          callbacks.add(
              () -> {
                Object value = supplier.get();
                if (value != null) Logger.recordOutput(key, (float) value);
              });
        }

      } else if (type.equals(double.class)) {
        if (unit.length() > 0) {
          callbacks.add(
              () -> {
                Object value = supplier.get();
                if (value != null) Logger.recordOutput(key, (double) value, unit);
              });
        } else {
          callbacks.add(
              () -> {
                Object value = supplier.get();
                if (value != null) Logger.recordOutput(key, (double) value);
              });
        }
      } else if (type.equals(String.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (String) value);
            });
      } else if (type.isEnum()) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                // Cannot cast to enum subclass, log the name directly
                Logger.recordOutput(key, ((Enum<?>) value).name());
            });
      } else if (BooleanSupplier.class.isAssignableFrom(type)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (BooleanSupplier) value);
            });
      } else if (IntSupplier.class.isAssignableFrom(type)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (IntSupplier) value);
            });
      } else if (LongSupplier.class.isAssignableFrom(type)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (LongSupplier) value);
            });
      } else if (DoubleSupplier.class.isAssignableFrom(type)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (DoubleSupplier) value);
            });
      } else if (Measure.class.isAssignableFrom(type)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (Measure<?>) value);
            });
      } else if (type.equals(LoggedMechanism2d.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (LoggedMechanism2d) value);
            });
      } else if (type.isRecord()) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (Record) value);
            });
      } else {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                try {
                  Logger.recordOutput(key, (WPISerializable) value);
                } catch (ClassCastException e) {
                  DriverStation.reportError(
                      "[AdvantageKit] Auto serialization is not supported for type "
                          + type.getSimpleName(),
                      false);
                }
            });
      }
    } else if (!type.getComponentType().isArray()) {
      // Array types
      Class<?> componentType = type.getComponentType();
      if (componentType.equals(byte.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (byte[]) value);
            });
      } else if (componentType.equals(boolean.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (boolean[]) value);
            });
      } else if (componentType.equals(int.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (int[]) value);
            });
      } else if (componentType.equals(long.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (long[]) value);
            });
      } else if (componentType.equals(float.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (float[]) value);
            });
      } else if (componentType.equals(double.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (double[]) value);
            });
      } else if (componentType.equals(String.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (String[]) value);
            });
      } else if (componentType.isEnum()) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) {
                // Cannot cast to enum subclass, log the names directly
                Enum<?>[] enumValue = (Enum<?>[]) value;
                String[] names = new String[enumValue.length];
                for (int i = 0; i < enumValue.length; i++) {
                  names[i] = enumValue[i].name();
                }
                Logger.recordOutput(key, names);
              }
            });
      } else if (componentType.isRecord()) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (Record[]) value);
            });
      } else {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) {
                try {
                  Logger.recordOutput(key, (StructSerializable[]) value);
                } catch (ClassCastException e) {
                  DriverStation.reportError(
                      "[AdvantageKit] Auto serialization is not supported for array type "
                          + componentType.getSimpleName(),
                      false);
                }
              }
            });
      }
    } else {
      // 2D array types
      Class<?> componentType = type.getComponentType().getComponentType();
      if (componentType.equals(byte.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (byte[][]) value);
            });
      } else if (componentType.equals(boolean.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (boolean[][]) value);
            });
      } else if (componentType.equals(int.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (int[][]) value);
            });
      } else if (componentType.equals(long.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (long[][]) value);
            });
      } else if (componentType.equals(float.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (float[][]) value);
            });
      } else if (componentType.equals(double.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (double[][]) value);
            });
      } else if (componentType.equals(String.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (String[][]) value);
            });
      } else if (componentType.isEnum()) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) {
                // Cannot cast to enum subclass, log the names directly
                Enum<?>[][] enumValue = (Enum<?>[][]) value;
                String[][] names = new String[enumValue.length][];
                for (int row = 0; row < enumValue.length; row++) {
                  Enum<?>[] rowValue = enumValue[row];
                  names[row] = new String[rowValue.length];
                  for (int column = 0; column < rowValue.length; column++) {
                    names[row][column] = rowValue[column].name();
                  }
                }
                Logger.recordOutput(key, names);
              }
            });
      } else if (componentType.isRecord()) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) Logger.recordOutput(key, (Record[][]) value);
            });
      } else {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null) {
                try {
                  Logger.recordOutput(key, (StructSerializable[][]) value);
                } catch (ClassCastException e) {
                  DriverStation.reportError(
                      "[AdvantageKit] Auto serialization is not supported for 2D array type "
                          + componentType.getSimpleName(),
                      false);
                }
              }
            });
      }
    }
  }
}
