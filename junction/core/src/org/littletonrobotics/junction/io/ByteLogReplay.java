package org.littletonrobotics.junction.io;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.LogTable;

/** Replays log values from a custom binary format. */
public class ByteLogReplay implements LogReplaySource {

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
      DriverStation.reportError("Failed to open replay log file for.", true);
    }
  }

  public void end() {
    if (file != null) {
      try {
        file.close();
        file = null;
      } catch (IOException e) {
        DriverStation.reportError("Failed to close replay log file.", true);
      }
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

  /** Prompts the user to enter a path and returns the result. */
  public static String promptForPath() {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter path to log file: ");
    String filename = scanner.nextLine();
    scanner.close();
    if (filename.charAt(0) == '\'' || filename.charAt(0) == '"') {
      filename = filename.substring(1, filename.length() - 1);
    }
    return filename;
  }
}
