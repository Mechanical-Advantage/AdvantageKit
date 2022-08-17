package org.littletonrobotics.junction.rlog;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogReplaySource;

/** Replays log values from the RLOG format. */
public class RLOGReader implements LogReplaySource {
  private final String filename;

  private FileInputStream file;
  private DataInputStream data;
  private RLOGDecoder decoder;

  public RLOGReader(String filename) {
    this.filename = filename;
  }

  public void start() {
    decoder = new RLOGDecoder();
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
      } catch (IOException e) {
      }
    }
  }

  public boolean updateTable(LogTable table) {
    return decoder.decodeTable(data, table);
  }
}
