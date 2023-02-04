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

  private static final int bufferSize = 10240;
  private int writePosition = 0;
  private byte[] data = new byte[bufferSize];

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
      if (nextChar != -1) {
        data[writePosition] = (byte) nextChar;
        writePosition++;
        if (writePosition >= bufferSize) {
          // Too much data, save any full lines and continue on the next cycle
          break;
        }
      } else {
        break;
      }
    }

    // Read all complete lines
    String dataStr = new String(data);
    int lastNewline = dataStr.lastIndexOf("\n");
    String completeLines;
    if (lastNewline != -1) {
      completeLines = dataStr.substring(0, lastNewline);
      byte[] trimmedData = new byte[bufferSize];
      if (lastNewline < bufferSize - 1) {
        System.arraycopy(data, lastNewline + 1, trimmedData, 0, bufferSize - lastNewline - 1);
      }
      data = trimmedData;
      writePosition -= lastNewline + 1;
    } else {
      completeLines = "";
    }
    return completeLines;
  }

  public void close() throws Exception {
    reader.close();
  }

}
