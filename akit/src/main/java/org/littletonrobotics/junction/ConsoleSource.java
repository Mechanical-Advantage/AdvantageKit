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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/** Console logging source. Users should not interact with this class directly. */
public interface ConsoleSource extends AutoCloseable {
  /**
   * Reads all console data that has been produced since the last call to this method.
   *
   * @return The console data
   */
  public String getNewData();

  /**
   * Reads console data while running in the simulator. Saves stdout and sterr from Java only (not
   * native code), and only includes lines logged after this class was instantiated.
   */
  public class Simulator implements ConsoleSource {
    private final PrintStream originalStdout;
    private final PrintStream originalStderr;
    private final ByteArrayOutputStream customStdout = new ByteArrayOutputStream();
    private final ByteArrayOutputStream customStderr = new ByteArrayOutputStream();
    private int customStdoutPos = 0;
    private int customStderrPos = 0;

    /** Create simulator console source. */
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
   * Reads console data on the RIO. Saves stdout and sterr from both Java and native code, including
   * lines logged before this class was instantiated.
   */
  public class RoboRIO implements ConsoleSource {
    private final Thread thread;
    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);
    private final List<String> lines = new ArrayList<>();

    /**
     * Returns the file path to use for logging.
     *
     * @return The file path
     */
    protected String getFilePath() {
      return "/home/lvuser/FRC_UserProgram.log";
    }

    /** Create roboRIO console source. */
    public RoboRIO() {
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
        reader = new BufferedReader(new FileReader(getFilePath()));
      } catch (FileNotFoundException e) {
        DriverStation.reportError(
            "[AdvantageKit] Failed to open console file \""
                + getFilePath()
                + "\", disabling console capture.",
            true);
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
                "[AdvantageKit] Failed to read console file \""
                    + getFilePath()
                    + "\", disabling console capture.",
                true);
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
}
