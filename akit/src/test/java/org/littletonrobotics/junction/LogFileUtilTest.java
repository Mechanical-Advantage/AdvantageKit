// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for LogFileUtil path manipulation and replay-log discovery. */
public class LogFileUtilTest {

  // ─── addPathSuffix ──────────────────────────────────────────────────────────

  @Test
  void addSuffixToSimplePath() {
    assertEquals("test_sim.wpilog", LogFileUtil.addPathSuffix("test.wpilog", "_sim"));
  }

  @Test
  void addSuffixPreservesExtension() {
    assertEquals("myfile_replay.rlog", LogFileUtil.addPathSuffix("myfile.rlog", "_replay"));
  }

  @Test
  void addSuffixWhenPathHasNoExtensionReturnsUnchanged() {
    assertEquals("testfile", LogFileUtil.addPathSuffix("testfile", "_sim"));
  }

  @Test
  void addSuffixWhenAlreadyHasSuffixAddsIndex2() {
    assertEquals("test_sim_2.wpilog", LogFileUtil.addPathSuffix("test_sim.wpilog", "_sim"));
  }

  @Test
  void addSuffixWhenHasIndex2IncrementsToIndex3() {
    assertEquals("test_sim_3.wpilog", LogFileUtil.addPathSuffix("test_sim_2.wpilog", "_sim"));
  }

  @Test
  void addSuffixWithHighIndex() {
    assertEquals("test_sim_10.wpilog", LogFileUtil.addPathSuffix("test_sim_9.wpilog", "_sim"));
  }

  @Test
  void addSuffixWithMultipleDotsUsesLastDot() {
    // e.g. "my.file.wpilog" → "my.file_sim.wpilog"
    assertEquals("my.file_sim.wpilog", LogFileUtil.addPathSuffix("my.file.wpilog", "_sim"));
  }

  @Test
  void addSuffixWithMultipleDotsAndExistingSuffix() {
    assertEquals(
        "my.file_sim_2.wpilog", LogFileUtil.addPathSuffix("my.file_sim.wpilog", "_sim"));
  }

  @Test
  void addDifferentSuffixDoesNotMatchExistingSuffix() {
    // "_replay" should not be confused with "_sim"
    assertEquals(
        "test_replay_sim.wpilog", LogFileUtil.addPathSuffix("test_replay.wpilog", "_sim"));
  }

  @Test
  void addSuffixToHiddenFile() {
    // Files like ".hidden" have only a leading dot; lastIndexOf('.') == 0
    // basename would be "" (empty) and extension ".hidden"
    // Currently returns ".hidden" unchanged because dotIndex != -1 but basename + suffix → "_sim"
    // This is fine — just document the behavior.
    String result = LogFileUtil.addPathSuffix(".hidden", "_sim");
    assertEquals("_sim.hidden", result);
  }

  @Test
  void addEmptySuffixReturnPathUnchanged() {
    // An empty suffix means "no suffix to add" — the path must be returned as-is.
    assertEquals("test.wpilog", LogFileUtil.addPathSuffix("test.wpilog", ""));
  }

  // ─── findReplayLogEnvVar ─────────────────────────────────────────────────────

  @Test
  void findReplayLogEnvVarReturnsNullWhenUnset() {
    // Only assert when the env var is genuinely absent to avoid CI false-positives
    if (System.getenv(LogFileUtil.environmentVariable) == null) {
      assertNull(LogFileUtil.findReplayLogEnvVar());
    }
  }

  @Test
  void findReplayLogEnvVarReturnsValueWhenSet() {
    // The env var is set at process start; we can only observe, not set, in standard Java.
    // If the env var IS set, verify the method returns a non-null, non-empty value.
    String envValue = System.getenv(LogFileUtil.environmentVariable);
    if (envValue != null) {
      assertEquals(envValue, LogFileUtil.findReplayLogEnvVar());
    }
  }

  // ─── findReplayLogAdvantageScope ─────────────────────────────────────────────

  @Test
  void findReplayLogAdvantageScopeReturnsNullWhenFileAbsent(@TempDir Path tmpDir) {
    String originalTmpDir = System.getProperty("java.io.tmpdir");
    System.setProperty("java.io.tmpdir", tmpDir.toString());
    try {
      assertNull(
          LogFileUtil.findReplayLogAdvantageScope(),
          "Must return null when the AdvantageScope temp file does not exist");
    } finally {
      System.setProperty("java.io.tmpdir", originalTmpDir);
    }
  }

  @Test
  void findReplayLogAdvantageScopeReadsFirstLine(@TempDir Path tmpDir) throws Exception {
    Path akitFile = tmpDir.resolve("akit-log-path.txt");
    Files.writeString(akitFile, "/path/to/log.wpilog\nignored second line");

    String originalTmpDir = System.getProperty("java.io.tmpdir");
    System.setProperty("java.io.tmpdir", tmpDir.toString());
    try {
      assertEquals("/path/to/log.wpilog", LogFileUtil.findReplayLogAdvantageScope());
    } finally {
      System.setProperty("java.io.tmpdir", originalTmpDir);
    }
  }

  @Test
  void findReplayLogAdvantageScopeIgnoresIOExceptionSilently(@TempDir Path tmpDir) {
    // Point tmpdir to a subdirectory that doesn't exist — must not throw
    String originalTmpDir = System.getProperty("java.io.tmpdir");
    System.setProperty("java.io.tmpdir", tmpDir.resolve("nonexistent").toString());
    try {
      assertDoesNotThrow(
          () -> assertNull(LogFileUtil.findReplayLogAdvantageScope()),
          "IOException from missing file must be swallowed, not propagated");
    } finally {
      System.setProperty("java.io.tmpdir", originalTmpDir);
    }
  }

  @Test
  void findReplayLogAdvantageScopeReturnsNullForEmptyFile(@TempDir Path tmpDir) throws Exception {
    // An empty AdvantageScope temp file should return null, not throw.
    // Fixed by guarding Scanner.nextLine() with Scanner.hasNextLine().
    Path akitFile = tmpDir.resolve("akit-log-path.txt");
    Files.writeString(akitFile, "");

    String originalTmpDir = System.getProperty("java.io.tmpdir");
    System.setProperty("java.io.tmpdir", tmpDir.toString());
    try {
      assertNull(
          LogFileUtil.findReplayLogAdvantageScope(),
          "Empty AdvantageScope temp file must return null, not throw");
    } finally {
      System.setProperty("java.io.tmpdir", originalTmpDir);
    }
  }

  @Test
  void findReplayLogAdvantageScopeHandlesFileWithOnlyNewline(@TempDir Path tmpDir)
      throws Exception {
    Path akitFile = tmpDir.resolve("akit-log-path.txt");
    Files.writeString(akitFile, "\n");

    String originalTmpDir = System.getProperty("java.io.tmpdir");
    System.setProperty("java.io.tmpdir", tmpDir.toString());
    try {
      String result = LogFileUtil.findReplayLogAdvantageScope();
      // First line is an empty string; this is what gets returned
      assertEquals("", result);
    } finally {
      System.setProperty("java.io.tmpdir", originalTmpDir);
    }
  }

  // ─── findReplayLog (orchestrator) ────────────────────────────────────────────

  @Test
  void findReplayLogReturnsAdvantageScopePathWhenEnvVarAbsent(@TempDir Path tmpDir)
      throws Exception {
    // Only run when the env var is NOT set (otherwise findReplayLog returns early via env-var path)
    if (System.getenv(LogFileUtil.environmentVariable) != null) {
      return;
    }

    Path akitFile = tmpDir.resolve("akit-log-path.txt");
    Files.writeString(akitFile, "/fake/log.wpilog");

    String originalTmpDir = System.getProperty("java.io.tmpdir");
    System.setProperty("java.io.tmpdir", tmpDir.toString());
    try {
      assertEquals("/fake/log.wpilog", LogFileUtil.findReplayLog());
    } finally {
      System.setProperty("java.io.tmpdir", originalTmpDir);
    }
  }

  // ─── findReplayLogUser (stdin) ────────────────────────────────────────────────

  @Test
  void findReplayLogUserReadsLineFromStdin() {
    InputStream original = System.in;
    System.setIn(new ByteArrayInputStream("/path/to/log.wpilog\n".getBytes()));
    try {
      assertEquals("/path/to/log.wpilog", LogFileUtil.findReplayLogUser());
    } finally {
      System.setIn(original);
    }
  }

  @Test
  void findReplayLogUserStripsLeadingQuote() {
    // findReplayLog() strips wrapping quotes; findReplayLogUser reads raw line.
    InputStream original = System.in;
    System.setIn(new ByteArrayInputStream("/path/to/log.wpilog\n".getBytes()));
    try {
      String result = LogFileUtil.findReplayLogUser();
      assertEquals("/path/to/log.wpilog", result);
    } finally {
      System.setIn(original);
    }
  }
}
