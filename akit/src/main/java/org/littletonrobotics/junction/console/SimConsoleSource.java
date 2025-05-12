// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.console;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Reads console data while running in the simulator. Saves stdout and sterr from Java only (not
 * native code), and only includes lines logged after this class was instantiated.
 */
public class SimConsoleSource implements ConsoleSource {
  private final PrintStream originalStdout;
  private final PrintStream originalStderr;
  private final ByteArrayOutputStream customStdout = new ByteArrayOutputStream();
  private final ByteArrayOutputStream customStderr = new ByteArrayOutputStream();
  private int customStdoutPos = 0;
  private int customStderrPos = 0;

  public SimConsoleSource() {
    originalStdout = System.out;
    originalStderr = System.err;
    System.setOut(new PrintStream(new SplitStream(originalStdout, customStdout)));
    System.setErr(new PrintStream(new SplitStream(originalStderr, customStderr)));
  }

  @Override
  public String getNewData() {
    String fullStdoutStr = customStdout.toString();
    String newStdoutStr = fullStdoutStr.substring(customStdoutPos);
    customStdoutPos = fullStdoutStr.length();

    String fullStderrStr = customStderr.toString();
    String newStderrStr = fullStderrStr.substring(customStderrPos);
    customStderrPos = fullStderrStr.length();

    return newStdoutStr + newStderrStr;
  }

  @Override
  public void close() throws Exception {
    System.setOut(originalStdout);
    System.setOut(originalStderr);
  }

  private class SplitStream extends OutputStream {
    private final OutputStream[] streams;

    public SplitStream(OutputStream... streams) {
      this.streams = streams;
    }

    @Override
    public void write(int b) throws IOException {
      for (int i = 0; i < streams.length; i++) {
        streams[i].write(b);
      }
    }
  }
}
