// Copyright 2021-2025 FRC 6328
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
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.wpi.first.wpilibj.DriverStation;

/**
 * Reads console data on the RIO. Saves stdout and sterr from both Java and
 * native code, including lines logged before this class was instantiated.
 */
public class RIOConsoleSource implements ConsoleSource {
  private static final String filePath = "/home/lvuser/FRC_UserProgram.log";
  private final Thread thread;
  private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);
  private final List<String> lines = new ArrayList<>();

  public RIOConsoleSource() {
    thread = new Thread(this::run, "AdvantageKit_RIOConsoleSource");
    thread.setDaemon(true);
    thread.start();
  }

  public String getNewData() {
    lines.clear();
    queue.drainTo(lines);
    return String.join("\n", lines);
  }

  public void close() throws Exception {
    thread.interrupt();
  }

  private void run() {
    // Initialize reader
    CharBuffer buffer = CharBuffer.allocate(10240);
    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(filePath));
    } catch (FileNotFoundException e) {
      DriverStation.reportError(
          "[AdvantageKit] Failed to open console file \"" + filePath + "\", disabling console capture.", true);
      return;
    }

    while (true) {
      // Read new data from console
      while (true) {
        int nextChar = -1;
        try {
          nextChar = reader.read();
        } catch (IOException e) {
          DriverStation.reportError(
              "[AdvantageKit] Failed to read console file \"" + filePath + "\", disabling console capture.", true);
          try {
            reader.close();
          } catch (IOException io) {
          }
          return;
        }
        if (nextChar != -1) {
          try {
            buffer.put((char) nextChar);
          } catch (BufferOverflowException e) {
          }
        } else {
          // Break read loop, send complete lines to queue
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
      if (output != null) {
        try {
          queue.put(output);
        } catch (InterruptedException e) {
          try {
            reader.close();
          } catch (IOException io) {
          }
          return;
        }
      }

      // Sleep to avoid spinning needlessly
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        try {
          reader.close();
        } catch (IOException io) {
        }
        return;
      }
    }
  }
}
