package org.littletonrobotics.junction.console;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * Reads console data on the RIO. Saves stdout and sterr from both Java and
 * native code, including lines logged before this class was instantiated.
 */
public class RIOConsoleSource implements ConsoleSource {
  private static final String filePath = "/home/lvuser/FRC_UserProgram.log";
  private BufferedReader reader = null;
  private String data = "";

  public RIOConsoleSource() {
    try {
      reader = new BufferedReader(new FileReader(filePath));
    } catch (FileNotFoundException e) {
      DriverStation.reportError("Failed to open console file \"" + filePath + "\"", true);
    }
  }

  public String getNewData() {
    if (reader == null) {
      return null;
    }

    // Read new data from console
    while (true) {
      int nextChar = -1;
      try {
        nextChar = reader.read();
      } catch (IOException e) {
        DriverStation.reportError("Failed to read console file \"" + filePath + "\"", true);
      }
      if (nextChar == -1) {
        break;
      } else {
        data += new String(new byte[] { (byte) nextChar });
      }
    }

    // Read all complete lines
    int lastNewline = data.lastIndexOf("\n");
    String completeLines = "";
    if (lastNewline != -1) {
      completeLines = data.substring(0, lastNewline);
      data = data.substring(lastNewline + 1);
    }
    return completeLines;
  }

  public void close() throws Exception {
    reader.close();
  }

}
