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

import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.units.Measure;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class AutoLogOutputManager {
  private static final List<Runnable> callbacks = new ArrayList<>();
  private static final List<Integer> scannedObjectHashes = new ArrayList<>();
  private static final Set<String> allowedPackages = new HashSet<>();

  /**
   * Adds a new allowed package to use when scanning for annotations. By default,
   * the parent class where {@code @AutoLogOutput} is used must be within the same
   * package as {@code Robot} (or a subpackage). Calling this method registers a
   * new allowed package, such as a "lib" package outside of normal robot code.
   * 
   * <p>
   * This method must be called within {@code robotInit}.
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
  static void registerFields(Object root) {
    allowedPackages.add(root.getClass().getPackageName());
    registerFieldsImpl(root);
  }

  /**
   * Registers a root object, scanning for loggable fields recursively.
   *
   * @param root The object to scan recursively.
   */
  private static void registerFieldsImpl(Object root) {
    // Check if package name is valid
    String packageName = root.getClass().getPackageName();
    boolean packageNameValid = false;
    for (String allowedPackage : allowedPackages) {
      if (packageName.startsWith(allowedPackage)) {
        packageNameValid = true;
        break;
      }
    }
    if (!packageNameValid)
      return;

    // Check if object has already been scanned
    if (scannedObjectHashes.contains(root.hashCode()))
      return;
    scannedObjectHashes.add(root.hashCode());

    // If array, loop over individual items
    if (root.getClass().isArray()) {
      Object[] rootArray = (Object[]) root;
      for (Object item : rootArray) {
        registerFieldsImpl(item);
      }
      return;
    }

    // Loop over declared methods
    getAllMethods(root.getClass()).forEach((methodAndDeclaringClass) -> {
      Method method = methodAndDeclaringClass.method;
      Class<?> declaringClass = methodAndDeclaringClass.declaringClass;
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
        String key = makeKey(keyParameter, method.getName(), declaringClass, root);

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
    getAllFields(root.getClass()).forEach((fieldAndDeclaringClass) -> {
      Field field = fieldAndDeclaringClass.field;
      Class<?> declaringClass = fieldAndDeclaringClass.declaringClass;
      if (!field.trySetAccessible())
        return;

      // If annotated, try to add
      if (field.isAnnotationPresent(AutoLogOutput.class)) {
        // Get key
        String keyParameter = field.getAnnotation(AutoLogOutput.class).key();
        String key = makeKey(keyParameter, field.getName(), declaringClass, root);

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
        registerFieldsImpl(fieldValue);
      }
    });
  }

  /**
   * Returns the set of all methods on the class and its superclasses (public and
   * private).
   */
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
  };

  /**
   * Returns the set of all fields in the class and its superclasses (public and
   * private).
   */
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
  };

  /**
   * Finds the field in the provided class and its superclasses (must be publicor
   * protected in superclasses). Returns null if the field cannot be found.
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
          if (Modifier.isPublic(field.getModifiers()) || Modifier.isProtected(field.getModifiers())) {
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
   * @param keyParameter   The user-provided key from the annotation
   * @param valueName      The name of the field or method
   * @param declaringClass The class where this fields or method is declared
   * @param parent         The parent object to read data from
   */
  private static String makeKey(String keyParameter, String valueName, Class<?> declaringClass, Object parent) {
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
          field = findField(declaringClass, fieldName);
          field.setAccessible(true);
          fieldValue = field.get(parent).toString();
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NullPointerException e) {
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
      } else if (type.isEnum()) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                // Cannot cast to enum subclass, log the name directly
                Logger.recordOutput(key, ((Enum<?>) value).name());
            });
      } else if (type.equals(Measure.class)) {
        callbacks.add(
            () -> {
              Object value = supplier.get();
              if (value != null)
                Logger.recordOutput(key, (Measure<?>) value);
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
