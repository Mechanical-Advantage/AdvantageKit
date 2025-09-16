// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/** Utility functions for managing log file paths. */
public class LogFileUtil {
  static final String environmentVariable = "AKIT_LOG_PATH";
  private static final String advantageScopeFileName = "akit-log-path.txt";

  private LogFileUtil() {}

  /**
   * Adds a suffix to the given path (e.g. "test.wpilog" -> "test_sim.wpilog"). If the input path
   * already contains the suffix, an index will be added instead.
   *
   * @param path The base path, such as "test.wpilog".
   * @param suffix The suffix to add, such as "_sim".
   * @return The new path.
   */
  public static String addPathSuffix(String path, String suffix) {
    int dotIndex = path.lastIndexOf(".");
    if (dotIndex == -1) {
      return path;
    }
    String basename = path.substring(0, dotIndex);
    String extension = path.substring(dotIndex);
    if (basename.endsWith(suffix)) {
      return basename + "_2" + extension;
    } else if (basename.matches(".+" + suffix + "_[0-9]+$")) {
      int splitIndex = basename.lastIndexOf("_");
      int index = Integer.parseInt(basename.substring(splitIndex + 1));
      return basename.substring(0, splitIndex) + "_" + Integer.toString(index + 1) + extension;
    } else {
      return basename + suffix + extension;
    }
  }

  /**
   * Finds the path to a log file for replay, using the following priorities:
   *
   * <p>1. The value of the "AKIT_LOG_PATH" environment variable, if set
   *
   * <p>2. The file currently open in AdvantageScope, if available
   *
   * <p>3. The result of the prompt displayed to the user
   *
   * @return The path to the log file.
   */
  public static String findReplayLog() {
    // Read environment variables
    String envPath = findReplayLogEnvVar();
    if (envPath != null) {
      System.out.println(
          "[AdvantageKit] Replaying log from "
              + environmentVariable
              + " environment variable: \""
              + envPath
              + "\"");
      return envPath;
    }

    // Read file from AdvantageScope
    String advantageScopeLogPath = findReplayLogAdvantageScope();
    if (advantageScopeLogPath != null) {
      System.out.println(
          "[AdvantageKit] Replaying log from AdvantageScope: \"" + advantageScopeLogPath + "\"");
      return advantageScopeLogPath;
    }

    // Prompt on stdin
    System.out.print(
        "No log provided with the "
            + environmentVariable
            + " environment variable or through AdvantageScope. Enter path to file: ");
    String filename = findReplayLogUser();
    if (filename.charAt(0) == '\'' || filename.charAt(0) == '"') {
      filename = filename.substring(1, filename.length() - 1);
    }
    return filename;
  }

  /** Read the replay log from the environment variable. */
  static String findReplayLogEnvVar() {
    return System.getenv(environmentVariable);
  }

  /** Read the replay log from AdvantageScope. */
  static String findReplayLogAdvantageScope() {
    Path advantageScopeTempPath =
        Paths.get(System.getProperty("java.io.tmpdir"), advantageScopeFileName);
    String advantageScopeLogPath = null;
    try (Scanner fileScanner = new Scanner(advantageScopeTempPath)) {
      advantageScopeLogPath = fileScanner.nextLine();
    } catch (IOException e) {
    }
    return advantageScopeLogPath;
  }

  /** Read the replay log from the user. */
  static String findReplayLogUser() {
    Scanner scanner = new Scanner(System.in);
    String filename = scanner.nextLine();
    scanner.close();
    return filename;
  }
}
