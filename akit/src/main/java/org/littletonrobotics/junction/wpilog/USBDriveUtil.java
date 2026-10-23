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
  private static final String[] usbDrivePrefixes = {"/u", "/v", "/w", "/x", "/y", "/z"};

  /**
   * Checks if the given path is on a Systemcore USB drive and returns the prefix if it is.
   *
   * @param path The path to check.
   * @return The USB drive prefix if the path is on a Systemcore USB drive, empty otherwise.
   */
  public static Optional<String> getUSBDrivePrefix(String path) {
    if (RobotBase.isReal() && path != null) {
      String lowerPath = path.toLowerCase();
      for (String prefix : usbDrivePrefixes) {
        if (lowerPath.equals(prefix) || lowerPath.startsWith(prefix + "/")) {
          return Optional.of(prefix);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Checks if a USB drive at the given prefix is mounted.
   *
   * @param prefix The prefix to check.
   * @return True if the USB drive is mounted, false otherwise.
   */
  public static boolean isUSBMounted(String prefix) {
    File dir = new File(prefix.toUpperCase());
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
