// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj;

/**
 * @Deprecated The main robot class should inherit from LoggedRobot instead of
 *             TimedRobot when using AdvantageKit's WPILib shims.
 */
@Deprecated
public final class TimedRobot {
  static class Callback implements Comparable<Callback> {
    public Runnable func;
    public double period;
    public double expirationTime;

    Callback(Runnable func, double startTimeSeconds, double periodSeconds, double offsetSeconds) {
    }

    @Override
    public boolean equals(Object rhs) {
      return false;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public int compareTo(Callback rhs) {
      return 0;
    }
  }

  public static final double kDefaultPeriod = 0.02;

  protected TimedRobot() {
  }

  protected TimedRobot(double period) {
  }

  public void close() {
  }

  public void startCompetition() {
  }

  public void endCompetition() {
  }

  public void addPeriodic(Runnable callback, double periodSeconds) {
  }

  public void addPeriodic(Runnable callback, double periodSeconds, double offsetSeconds) {
  }
}