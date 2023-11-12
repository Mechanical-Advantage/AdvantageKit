// Copyright 2021-2023 FRC 6328
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

import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.proto.Trajectory;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

class AutoLogOutputManager {
  private static final List<Runnable> callbacks = new ArrayList<>();
  private static final List<Integer> scannedObjectHashes = new ArrayList<>();

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
  static void registerFields(Object root) {
    registerFields(root, root.getClass().getPackageName());
  }

  /**
   * Registers a root object, scanning for loggable fields recursively.
   *
   * @param root        The object to scan recursively.
   * @param packageName The required prefix for the package name.
   */
  private static void registerFields(Object root, String packageName) {
    if (!root.getClass().getPackageName().startsWith(packageName))
      return;
    if (scannedObjectHashes.contains(root.hashCode()))
      return;
    scannedObjectHashes.add(root.hashCode());

    // If array, loop over individual items
    if (root.getClass().isArray()) {
      Object[] rootArray = (Object[]) root;
      for (Object item : rootArray) {
        registerFields(item, packageName);
      }
      return;
    }

    // Loop over declared methods
    getAllMethods(root.getClass()).forEach((method) -> {
      if (!method.trySetAccessible())
        return;

      // If annotated, try to add
      if (method.isAnnotationPresent(AutoLogOutput.class)) {
        // Exit if invalid signature
        if (method.getReturnType().equals(Void.TYPE)
            || method.getParameterCount() > 0
            || method.getExceptionTypes().length > 0) {
          return;
        }

        // Get key
        String keyParameter = method.getAnnotation(AutoLogOutput.class).key();
        String key = makeKey(keyParameter, root, method.getName());

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
            });
      }
    });

    // Loop over declared fields
    getAllFields(root.getClass()).forEach((field) -> {
      if (!field.trySetAccessible())
        return;

      // If annotated, try to add
      if (field.isAnnotationPresent(AutoLogOutput.class)) {
        // Get key
        String keyParameter = field.getAnnotation(AutoLogOutput.class).key();
        String key = makeKey(keyParameter, root, field.getName());

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
            });
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
        registerFields(fieldValue, packageName);
      }
    });
  }

  /**
   * Returns the set of all methods on the class and its superclasses (public and
   * private).
   */
  private static List<Method> getAllMethods(Class<?> type) {
    List<Method> methods = new ArrayList<>();
    while (type != null && type != Object.class) {
      Collections.addAll(methods, type.getDeclaredMethods());
      type = type.getSuperclass();
    }
    return methods;
  }

  /**
   * Returns the set of all fields in the class and its superclasses (public and
   * private).
   */
  private static List<Field> getAllFields(Class<?> type) {
    List<Field> fields = new ArrayList<>();
    while (type != null && type != Object.class) {
      Collections.addAll(fields, type.getDeclaredFields());
      type = type.getSuperclass();
    }
    return fields;
  }

  /**
   * Generates a log key based on the field properties.
   * 
   * @param keyParameter The user-provided key from the annotation
   * @param parent       The parent object
   * @param valueName    The name of the field or method
   */
  private static String makeKey(String keyParameter, Object parent, String valueName) {
    if (keyParameter.length() == 0) {
      // Auto generate from parent and value
      String key = parent.getClass().getSimpleName() + "/";
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
        if (openIndex == -1)
          break; // No more brackets
        int closeIndex = key.indexOf("}", openIndex);
        if (closeIndex == -1)
          break; // No closing bracket
        String fieldName = key.substring(openIndex + 1, closeIndex);

        // Get field value
        String fieldValue = "";
        Field field;
        try {
          field = parent.getClass().getDeclaredField(fieldName);
          field.setAccessible(true);
          fieldValue = field.get(parent).toString();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
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
   * @param key      The string key to use for logging.
   * @param type     The type of object being logged.
   * @param supplier A supplier for the field values.
   */
  private static void registerField(String key, Class<?> type, Supplier<?> supplier) {
    if (type.isArray()) {
      // Array types
      Class<?> componentType = type.getComponentType();
      if (componentType.equals(byte.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (byte[]) value);
            });
      } else if (componentType.equals(boolean.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (boolean[]) value);
            });
      } else if (componentType.equals(int.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (int[]) value);
            });
      } else if (componentType.equals(long.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (long[]) value);
            });
      } else if (componentType.equals(float.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (float[]) value);
            });
      } else if (componentType.equals(double.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (double[]) value);
            });
      } else if (componentType.equals(String.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (String[]) value);
            });
      } else if (componentType.equals(SwerveModuleState.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (SwerveModuleState[]) value);
            });
      } else {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (Object[]) value);
            });
      }
    } else {
      // Single types
      if (type.equals(boolean.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (boolean) value);
            });
      } else if (type.equals(int.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (int) value);
            });
      } else if (type.equals(long.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (long) value);
            });
      } else if (type.equals(float.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (float) value);
            });
      } else if (type.equals(double.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (double) value);
            });
      } else if (type.equals(String.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (String) value);
            });
      } else if (type.equals(Trajectory.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (Trajectory) value);
            });
      } else if (type.equals(Mechanism2d.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (Mechanism2d) value);
            });
      } else {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, value);
            });
      }
    }
  }
}
