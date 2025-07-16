// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import edu.wpi.first.wpilibj.DriverStation;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

interface ConsoleSource extends AutoCloseable {
  /** Reads all console data that has been produced since the last call to this method. */
  public String getNewData();

  /**
   * Reads console data while running in the simulator. Saves stdout and sterr from Java only (not
   * native code), and only includes lines logged after this class was instantiated.
   */
  class Simulator implements ConsoleSource {
    private final PrintStream originalStdout;
    private final PrintStream originalStderr;
    private final ByteArrayOutputStream customStdout = new ByteArrayOutputStream();
    private final ByteArrayOutputStream customStderr = new ByteArrayOutputStream();
    private int customStdoutPos = 0;
    private int customStderrPos = 0;

    public Simulator() {
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

  /**
   * Reads console data on SystemCore. Saves stdout and sterr from both Java and native code,
   * including lines logged before this class was instantiated.
   */
  class SystemCore implements ConsoleSource {
    private static final String[] command =
        new String[] {
          "/bin/bash",
          "-c",
          "journalctl -f -u robot.service -n all -o cat _SYSTEMD_INVOCATION_ID=$(systemctl show -p InvocationID --value robot.service)"
        };
    private final Thread thread;
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);
    private final List<String> lines = new ArrayList<>();

    public SystemCore() {
      thread = new Thread(this::run, "AdvantageKit_SystemCoreConsoleSource");
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
      Process process;
      try {
        process = Runtime.getRuntime().exec(command);
      } catch (IOException e) {
        DriverStation.reportError(
            "[AdvantageKit] Failed to launch console capture process, disabling.", true);
        return;
      }

      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          try {
            queue.put(line);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      } catch (IOException e) {
        if (!thread.isInterrupted()) {
          DriverStation.reportError(
              "[AdvantageKit] Failed to read from console capture process, disabling.", true);
        }
      } finally {
        process.destroy();
      }
    }
  }
}
