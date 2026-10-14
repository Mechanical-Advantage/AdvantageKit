// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.littletonrobotics.junction.LogTable.LogValue;
import org.wpilib.system.Notifier;

/** Captures data from vendor log files into the active AdvantageKit log. */
class VendorLogCapture {
  private static final int maxQueuedBytes = 10 * 1024 * 1024; // 10 MB queue capacity per log
  private static final byte[] emptyByteArray = new byte[0];

  private static Path captureDirectory;
  private static Notifier notifier;
  private static Thread shutdownHook;

  private static final ByteBuffer readBuffer = ByteBuffer.allocateDirect(65536);
  private static final Map<Path, TrackedLog> trackedLogs = new ConcurrentHashMap<>();
  private static final Map<String, Integer> formatCounts = new HashMap<>();
  private static final Set<Path> ignoredLogs = new HashSet<>();

  private VendorLogCapture() {
    throw new RuntimeException("This class cannot be instantiated.");
  }

  /** Starts capturing vendor logs from the specified directory. */
  static synchronized void start(String directory) {
    if (notifier != null) {
      return;
    }

    // Initialize configuration
    captureDirectory = Paths.get(directory);

    // Ensure that the log directory exists
    if (!Files.exists(captureDirectory)) {
      try {
        Files.createDirectories(captureDirectory);
      } catch (IOException e) {
      }
    }

    // Remove any pre-existing capturable logs
    cleanCapturableLogs();

    // Start the capture notifier
    notifier = new Notifier(VendorLogCapture::periodicBackground);
    notifier.setName("AdvantageKit_VendorLogCapture");
    notifier.startPeriodic(0.2);

    // Add shutdown hook (always clear directory when interrupted)
    shutdownHook = new Thread(VendorLogCapture::end, "AdvantageKit_VendorLogCaptureShutdown");
    Runtime.getRuntime().addShutdownHook(shutdownHook);

    // Print status
    System.err.println("[AdvantageKit] Searching for vendor logs in \"" + directory + "\"");
  }

  /** Stop capturing vendor logs. */
  static synchronized void end() {
    if (notifier != null) {
      notifier.close();
      notifier = null;
    }

    if (shutdownHook != null) {
      try {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
      } catch (IllegalStateException ignored) {
      }
      shutdownHook = null;
    }

    for (TrackedLog trackedLog : trackedLogs.values()) {
      try {
        trackedLog.channel.close();
        Files.deleteIfExists(trackedLog.filePath);
        Files.deleteIfExists(
            trackedLog.filePath.resolveSibling(
                "." + trackedLog.filePath.getFileName().toString() + ".captured"));
      } catch (IOException e) {
      }
    }
    trackedLogs.clear();
    formatCounts.clear();
    cleanCapturableLogs();
    ignoredLogs.clear();
  }

  /** Called on the background thread to read new data from vendor log files. */
  private static void periodicBackground() {
    // Check for new files in the directory tree.
    try {
      if (!Files.exists(captureDirectory)) {
        return;
      }
      Files.walkFileTree(
          captureDirectory,
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              if (isCapturable(file)
                  && !trackedLogs.containsKey(file)
                  && !ignoredLogs.contains(file)) {
                String extension = getExtension(file);
                if (extension.isEmpty()) {
                  return FileVisitResult.CONTINUE;
                }

                // Generate entry name
                String entryBaseName =
                    extension.equalsIgnoreCase("hoot") ? "Hoot" : extension.toUpperCase();
                int currentCount = formatCounts.getOrDefault(entryBaseName, 0);
                formatCounts.put(entryBaseName, currentCount + 1);
                String fieldName =
                    entryBaseName + (currentCount == 0 ? "" : String.valueOf(currentCount));

                // Initialize capture
                try {
                  FileChannel channel = FileChannel.open(file, StandardOpenOption.READ);
                  trackedLogs.put(
                      file,
                      new TrackedLog(channel, fieldName, "log:" + extension.toLowerCase(), file));

                  // Create marker file
                  Path markerPath =
                      file.resolveSibling("." + file.getFileName().toString() + ".captured");
                  try {
                    Files.createFile(markerPath);
                  } catch (IOException e) {
                  }
                } catch (IOException e) {
                  System.err.println("[AdvantageKit] Could not open vendor log \"" + file + "\"");
                }

                // Print status
                System.err.println(
                    "[AdvantageKit] Capturing vendor log \""
                        + file
                        + "\" to field \"/"
                        + fieldName
                        + "\"");
              }
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
    }

    // Capture new data from each log
    for (TrackedLog log : trackedLogs.values()) {
      if (log.queuedBytes.get() >= maxQueuedBytes) {
        continue; // Queue limit reached, pause reading until main thread drains
      }

      try {
        ByteArrayOutputStream accumulated = null;
        byte[] singleChunk = null;
        while (log.queuedBytes.get() < maxQueuedBytes) {
          readBuffer.clear();
          int bytesRead = log.channel.read(readBuffer, log.currentOffset);

          if (bytesRead <= 0) {
            break; // Reached current EOF
          }

          readBuffer.flip();
          byte[] chunk = new byte[bytesRead];
          readBuffer.get(chunk);
          log.currentOffset += bytesRead;

          if (singleChunk == null && accumulated == null) {
            singleChunk = chunk;
          } else {
            if (accumulated == null) {
              accumulated = new ByteArrayOutputStream();
              accumulated.write(singleChunk);
            }
            accumulated.write(chunk);
          }
        }

        byte[] finalChunk = null;
        if (accumulated != null) {
          finalChunk = accumulated.toByteArray();
        } else if (singleChunk != null) {
          finalChunk = singleChunk;
        }

        if (finalChunk != null) {
          log.queue.add(finalChunk);
          log.queuedBytes.addAndGet(finalChunk.length);
        }
      } catch (IOException e) {
        // File may have been temporarily locked; retry next cycle
      }
    }
  }

  /**
   * Called on the main thread every cycle to log queued vendor log data.
   *
   * @param entry The active log table entry.
   */
  static void periodic(LogTable entry) {
    for (TrackedLog log : trackedLogs.values()) {
      byte[] data = log.queue.poll();
      if (data == null) {
        // No new data from the background thread in this cycle
        entry.put(log.fieldName, new LogValue(emptyByteArray, log.fieldType));
      } else {
        int drainedBytes = data.length;
        if (log.queue.isEmpty()) {
          log.queuedBytes.addAndGet(-drainedBytes);
          entry.put(log.fieldName, new LogValue(data, log.fieldType));
        } else {
          // Multiple chunks were queued, combine them
          ByteArrayOutputStream combined = new ByteArrayOutputStream();
          try {
            combined.write(data);
            byte[] next;
            while ((next = log.queue.poll()) != null) {
              drainedBytes += next.length;
              combined.write(next);
            }
            log.queuedBytes.addAndGet(-drainedBytes);
            entry.put(log.fieldName, new LogValue(combined.toByteArray(), log.fieldType));
          } catch (IOException e) {
            log.queuedBytes.addAndGet(-drainedBytes);
            entry.put(log.fieldName, new LogValue(data, log.fieldType));
          }
        }
      }
    }
  }

  /** Checks if a file is a capturable log file. Excludes WPILOG and hidden files. */
  private static boolean isCapturable(Path file) {
    String filename = file.getFileName().toString();
    return !filename.endsWith(".wpilog") && !filename.startsWith(".");
  }

  /** Extracts the file extension from a path. */
  private static String getExtension(Path file) {
    String fileName = file.getFileName().toString();
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
      return fileName.substring(dotIndex + 1);
    }
    return "";
  }

  /** Cleans any capturable log files and subdirectories. */
  private static void cleanCapturableLogs() {
    try {
      if (!Files.exists(captureDirectory)) {
        return;
      }

      Files.walkFileTree(
          captureDirectory,
          new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
              String filename = file.getFileName().toString();
              if (isCapturable(file)) {
                Path markerPath = file.resolveSibling("." + filename + ".captured");
                if (Files.exists(markerPath)) {
                  Files.deleteIfExists(file);
                  Files.deleteIfExists(markerPath);
                } else {
                  ignoredLogs.add(file);
                }
              } else if (filename.startsWith(".") && filename.endsWith(".captured")) {
                String originalName = filename.substring(1, filename.length() - 9);
                if (!Files.exists(file.resolveSibling(originalName))) {
                  Files.deleteIfExists(file);
                }
              }
              return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
              if (!Files.isSameFile(dir, captureDirectory)) {
                try {
                  Files.delete(dir);
                } catch (DirectoryNotEmptyException | NoSuchFileException ignored) {
                } catch (IOException e) {
                }
              }
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
    }
  }

  private static class TrackedLog {
    final FileChannel channel;
    final String fieldName;
    final String fieldType;
    final Path filePath;
    long currentOffset;
    final Queue<byte[]> queue = new ConcurrentLinkedQueue<>();
    final AtomicInteger queuedBytes = new AtomicInteger(0);

    TrackedLog(FileChannel channel, String fieldName, String fieldType, Path filePath) {
      this.channel = channel;
      this.fieldName = fieldName;
      this.fieldType = fieldType;
      this.filePath = filePath;
      this.currentOffset = 0;
    }
  }
}
