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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class LogFileUtil {
  private static final String environmentVariable = "AKIT_LOG_PATH";
  private static final String advantageScopeFileName = "akit-log-path.txt";

  private LogFileUtil() {
  }

  /**
   * Adds a suffix to the given path (e.g. "test.rlog" -> "test_simulated.rlog").
   */
  public static String addPathSuffix(String path, String suffix) {
    String[] tokens = path.split("\\.");
    return tokens[0] + suffix + "." + tokens[1];
  }

  /**
   * Finds the path to a log file for replay, using the following priorities:
   * 
   * 1. The value of the "AKIT_LOG_PATH" environment variable, if set
   * 2. The file currently open in AdvantageScope, if available
   * 3. The result of the prompt displayed to the user
   */
  public static String findReplayLog() {
    // Read environment variables
    String envPath = System.getenv(environmentVariable);
    if (envPath != null) {
      System.out.println("Using log from " + environmentVariable + " environment variable - \"" + envPath + "\"");
      return envPath;
    }

    // Read file from AdvantageScope
    Path advantageScopeTempPath = Paths.get(System.getProperty("java.io.tmpdir"), advantageScopeFileName);
    String advantageScopeLogPath = null;
    try (Scanner fileScanner = new Scanner(advantageScopeTempPath)) {
      advantageScopeLogPath = fileScanner.nextLine();
    } catch (IOException e) {
    }
    if (advantageScopeLogPath != null) {
      System.out.println("Using log from AdvantageScope - \"" + advantageScopeLogPath + "\"");
      return advantageScopeLogPath;
    }

    // Prompt on stdin
    Scanner scanner = new Scanner(System.in);
    System.out.print("No log provided with the " + environmentVariable
        + " environment variable or through AdvantageScope. Enter path to file: ");
    String filename = scanner.nextLine();
    scanner.close();
    if (filename.charAt(0) == '\'' || filename.charAt(0) == '"') {
      filename = filename.substring(1, filename.length() - 1);
    }
    return filename;
  }
}
