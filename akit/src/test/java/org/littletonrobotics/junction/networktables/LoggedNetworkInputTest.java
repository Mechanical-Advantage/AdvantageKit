// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.networktables;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LoggedNetworkInput#removeSlash(String)}.
 *
 * <p>The concrete subclasses ({@code LoggedNetworkNumber}, etc.) all rely on this utility to
 * normalise NT keys before logging them to the robot log, so correctness here is important.
 */
public class LoggedNetworkInputTest {

  // ─── Minimal concrete subclass (no NT interaction needed) ───────────────────

  /** Thin wrapper exposing the protected static helper under test. */
  private static String strip(String key) {
    return TestInput.removeSlash(key);
  }

  private static final class TestInput extends LoggedNetworkInput {
    @Override
    public void periodic() {}
  }

  // ─── removeSlash() ──────────────────────────────────────────────────────────

  @Test
  void leadingSlashIsRemoved() {
    assertEquals("key", strip("/key"), "A leading slash must be stripped");
  }

  @Test
  void noLeadingSlashPassesThrough() {
    assertEquals("key", strip("key"), "A key without a leading slash must be returned unchanged");
  }

  @Test
  void onlyLeadingSlashIsRemoved() {
    // "/a/b" → "a/b": only the first character is stripped, not every slash
    assertEquals("a/b", strip("/a/b"), "Only the leading slash must be removed, not internal slashes");
  }

  @Test
  void emptyStringReturnsEmpty() {
    assertEquals("", strip(""), "An empty key must be returned as an empty string");
  }

  @Test
  void singleSlashBecomesEmpty() {
    // A bare "/" has a leading slash, so the result is ""
    assertEquals("", strip("/"), "A bare '/' must become an empty string after removing the leading slash");
  }

  @Test
  void keyWithNoSlashesIsUnchanged() {
    assertEquals("drivetrain", strip("drivetrain"));
  }

  @Test
  void deeplyNestedKeyStripsOnlyLeadingSlash() {
    assertEquals("a/b/c/d", strip("/a/b/c/d"));
  }

  // ─── prefix constant ────────────────────────────────────────────────────────

  @Test
  void prefixConstantIsNetworkInputs() {
    // The logging prefix is used by all concrete subclasses when calling processInputs().
    // A change here would silently break the log schema for every dashboard input.
    assertEquals(
        "NetworkInputs",
        LoggedNetworkInput.prefix,
        "The LoggedNetworkInput.prefix must remain \"NetworkInputs\"");
  }
}
