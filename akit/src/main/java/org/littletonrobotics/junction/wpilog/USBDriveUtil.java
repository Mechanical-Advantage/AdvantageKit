// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.wpilog;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.wpilib.framework.RobotBase;

/** Utility class for USB drive handling. */
class USBDriveUtil {
  private static final String[] usbDrivePrefixes = {"/U", "/V", "/W", "/X", "/Y", "/Z"};

  /**
   * Checks if the given path is on a Systemcore USB drive and returns the prefix if it is.
   *
   * @param path The path to check.
   * @return The uppercase USB drive prefix if the path is on a Systemcore USB drive, empty
   *     otherwise.
   */
  public static Optional<String> getUSBDrivePrefix(String path) {
    if (RobotBase.isReal() && path != null) {
      String upperPath = path.toUpperCase();
      for (String prefix : usbDrivePrefixes) {
        if (upperPath.equals(prefix) || upperPath.startsWith(prefix + "/")) {
          return Optional.of(prefix);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Normalizes a path on a Systemcore USB drive to use the uppercase (non-symlink) prefix.
   *
   * @param path The path to normalize.
   * @return The normalized path if on a Systemcore USB drive, or the original path otherwise.
   */
  public static String normalizeUSBPath(String path) {
    Optional<String> prefix = getUSBDrivePrefix(path);
    if (prefix.isPresent()) {
      return prefix.get() + path.substring(prefix.get().length());
    }
    return path;
  }

  /**
   * Checks if a USB drive at the given prefix is mounted.
   *
   * @param prefix The USB drive prefix to check.
   * @return True if the USB drive is mounted, false otherwise.
   */
  public static boolean isUSBMounted(String prefix) {
    File dir = new File(prefix);
    if (!dir.isDirectory() || !dir.canWrite()) {
      return false;
    }

    try (var lines = Files.lines(Path.of("/proc/mounts"))) {
      return lines.anyMatch(
          line -> {
            String[] parts = line.split("\\s+");
            return parts.length >= 2 && parts[1].equalsIgnoreCase(prefix);
          });
    } catch (Exception e) {
      return false;
    }
  }
}
