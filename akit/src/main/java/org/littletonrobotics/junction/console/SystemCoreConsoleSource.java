// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.console;

import edu.wpi.first.wpilibj.DriverStation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Reads console data on SystemCore. Saves stdout and sterr from both Java and native code,
 * including lines logged before this class was instantiated.
 */
public class SystemCoreConsoleSource implements ConsoleSource {
  private static final String[] command =
      new String[] {
        "/bin/bash",
        "-c",
        "journalctl -f -u robot.service -n all -o cat _SYSTEMD_INVOCATION_ID=$(systemctl show -p InvocationID --value robot.service)"
      };
  private final Thread thread;
  private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);
  private final List<String> lines = new ArrayList<>();

  public SystemCoreConsoleSource() {
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
