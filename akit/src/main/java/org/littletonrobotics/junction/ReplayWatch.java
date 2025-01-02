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

package org.littletonrobotics.junction;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class ReplayWatch {
  private static WatchService watcher;
  private static Map<WatchKey, Path> keys;

  private ReplayWatch() {
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static void main(String[] args) throws IOException, InterruptedException {
    // Find input log
    String inputLog = LogFileUtil.findReplayLogEnvVar();
    if (inputLog == null) {
      inputLog = LogFileUtil.findReplayLogAdvantageScope();
    }
    if (inputLog == null) {
      System.out.println(
          "No input log available for replay watch, please provide with the \"" + LogFileUtil.environmentVariable
              + "\" environment variable or through AdvantageScope.");
      System.exit(1);
    }

    // Run initial replay
    launchReplay(inputLog);

    // Create directory watcher
    watcher = FileSystems.getDefault().newWatchService();
    keys = new HashMap<WatchKey, Path>();
    registerAll(Path.of("src").toAbsolutePath());

    // Wait for new events
    long lastReplay = System.currentTimeMillis();
    while (true) {
      // Wait for signal
      boolean isNewUpdate = false;
      WatchKey key = watcher.poll();
      if (key == null) {
        try {
          key = watcher.take();
        } catch (InterruptedException x) {
          return;
        }
        isNewUpdate = true;
      }

      Path dir = keys.get(key);
      if (dir == null) {
        break;
      }

      // Process events
      for (WatchEvent<?> event : key.pollEvents()) {
        WatchEvent.Kind kind = event.kind();
        if (kind == StandardWatchEventKinds.OVERFLOW) {
          break;
        }

        WatchEvent<Path> ev = (WatchEvent<Path>) (event);
        Path name = ev.context();
        Path child = dir.resolve(name);

        // If directory created, register subdirectory
        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
          try {
            if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
              registerAll(child);
            }
          } catch (IOException x) {
          }
        }
      }

      // Directory not accessible, remove
      boolean valid = key.reset();
      if (!valid) {
        keys.remove(key);

        // All directories inaccessible
        if (keys.isEmpty()) {
          break;
        }
      }

      // New update, run replay
      if (isNewUpdate && System.currentTimeMillis() - lastReplay > 250) {
        launchReplay(inputLog);
        lastReplay = System.currentTimeMillis();
      }
    }
  }

  private static void launchReplay(String inputLog) throws IOException, InterruptedException {
    System.out.print("[AdvantageKit] Replay active... (0.0s)\r");

    // Launch Gradle
    boolean isWindows = System.getProperty("os.name").startsWith("Windows");
    var gradleBuilder = new ProcessBuilder(
        isWindows ? "gradlew.bat" : "./gradlew",
        "simulateJava",
        "-x",
        "test",
        "-x",
        "spotlessApply",
        "-x",
        "spotlessCheck");
    gradleBuilder.environment().put(LogFileUtil.environmentVariable, inputLog);
    var gradle = gradleBuilder.start();

    // Print timer
    NumberFormat formatter = new DecimalFormat("#0.0");
    long startTime = System.currentTimeMillis();
    while (gradle.isAlive()) {
      Thread.sleep(100);
      System.out.print("[AdvantageKit] Replay active... ("
          + formatter.format((System.currentTimeMillis() - startTime) * 1.0e-3) + "s)\r");
    }

    // Print result
    if (gradle.exitValue() == 0) {
      System.out.print("[AdvantageKit] Replay finished          \r");
    } else {
      System.out.print("[AdvantageKit] Replay failed             \r");
    }
  }

  /**
   * Register the given directory with the WatchService
   */
  private static void register(Path dir) throws IOException {
    WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY);
    keys.put(key, dir);
  }

  /**
   * Register the given directory, and all its sub-directories, with the
   * WatchService.
   */
  private static void registerAll(final Path start) throws IOException {
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
          throws IOException {
        register(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
