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

package org.littletonrobotics.junction.console;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * Reads console data on the RIO. Saves stdout and sterr from both Java and
 * native code, including lines logged before this class was instantiated.
 */
public class RIOConsoleSource implements ConsoleSource {
  private static final String filePath = "/home/lvuser/FRC_UserProgram.log";
  private BufferedReader reader = null;

  private CharBuffer buffer = CharBuffer.allocate(10240);

  public RIOConsoleSource() {
    try {
      reader = new BufferedReader(new FileReader(filePath));
    } catch (FileNotFoundException e) {
      DriverStation.reportError("Failed to open console file \"" + filePath + "\", disabling console capture.", true);
    }
  }

  public String getNewData() {
    if (reader == null) {
      return "";
    }

    // Read new data from console
    while (true) {
      int nextChar = -1;
      try {
        nextChar = reader.read();
      } catch (IOException e) {
        DriverStation.reportError("Failed to read console file \"" + filePath + "\", disabling console capture.", true);
        reader = null;
        return "";
      }
      if (nextChar != -1) {
        try {
          buffer.put((char) nextChar);
        } catch (BufferOverflowException e) {}
      } else {
        break;
      }
    }

    // Read all complete lines
    String output = null;
    for (int i = buffer.position(); i > 0; i--) {
      if (i < buffer.position() && buffer.get(i) == '\n') {
        int originalPosition = buffer.position();
        output = new String(buffer.array(), 0, i);
        buffer.rewind();
        buffer.put(buffer.array(), i + 1, buffer.limit() - i - 1);
        buffer.position(originalPosition - i - 1);
        break;
      }
    }
    if (output == null) output = "";
    return output;
  }

  public void close() throws Exception {
    reader.close();
  }

}
