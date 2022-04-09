package org.littletonrobotics.junction.io;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.LogTable;

/** Replays log values from a custom binary format. */
public class ByteLogReplay implements LogReplaySource {
  private static final String advantageScopeFileName = "akit-log-path.txt";

  private final String filename;

  private FileInputStream file;
  private DataInputStream data;
  private ByteDecoder decoder;

  public ByteLogReplay(String filename) {
    this.filename = filename;
  }

  public void start() {
    decoder = new ByteDecoder();
    try {
      file = new FileInputStream(filename);
      data = new DataInputStream(file);
    } catch (FileNotFoundException e) {
      DriverStation.reportError("Failed to open replay log file.", true);
    }
  }

  public void end() {
    if (file != null) {
      try {
        file.close();
        file = null;
      } catch (IOException e) {}
    }
  }

  public LogTable getEntry() {
    if (file != null) {
      LogTable table = decoder.decodeTable(data);
      if (table == null) {
        System.out.println("Replay of log has ended.");
      }
      return table;
    } else {
      return null;
    }
  }

  /**
   * Prompts the user to enter a path and returns the result.
   *
   * Uses the following priorities for path selection:
   * 1. The value of the 'AKIT_LOG_PATH' environment variable, if set
   * 2. The file last opened in Advantage Scope, if available
   * 3. The result of the prompt displayed to the user
   */
  public static String promptForPath() {
    // Read environment variables
    String envPath = System.getenv("AKIT_LOG_PATH");
    if (envPath != null) {
      return envPath;
    }

    // Read file from Advantage Scope
    Path advantageScopeTempPath = Paths.get(System.getProperty("java.io.tmpdir"), advantageScopeFileName);
    String advantageScopeLogPath = null;
    try (Scanner fileScanner = new Scanner(advantageScopeTempPath)) {
      advantageScopeLogPath = fileScanner.nextLine();
    } catch (IOException e) {
    }
    if (advantageScopeLogPath != null) {
      System.out.println("Using log from Advantage Scope - \"" + advantageScopeLogPath + "\"");
      return advantageScopeLogPath;
    }

    // Prompt on stdin
    Scanner scanner = new Scanner(System.in);
    System.out.print("No log open in Advantage Scope. Enter path to file: ");
    String filename = scanner.nextLine();
    scanner.close();
    if (filename.charAt(0) == '\'' || filename.charAt(0) == '"') {
      filename = filename.substring(1, filename.length() - 1);
    }
    return filename;
  }
}
