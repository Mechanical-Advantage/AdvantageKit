// Copyright 2021-2024 FRC 6328
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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import edu.wpi.first.hal.DriverStationJNI;
import edu.wpi.first.hal.FRCNetComm.tInstances;
import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.wpilibj.IterativeRobotBase;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.NotifierJNI;
import java.util.PriorityQueue;

/**
 * LoggedRobot implements the IterativeRobotBase robot program framework.
 *
 * <p>
 * The LoggedRobot class is intended to be subclassed by a user creating a robot
 * program, and will call all required AdvantageKit periodic methods.
 *
 * <p>
 * periodic() functions from the base class are called on an interval by a
 * Notifier instance.
 */
public class LoggedRobot extends IterativeRobotBase {
  @SuppressWarnings("MemberName")
  static class Callback implements Comparable<Callback> {
    public Runnable func;
    public long period;
    public long expirationTime;

    /**
     * Construct a callback container.
     *
     * @param func The callback to run.
     * @param startTimeSeconds The common starting point for all callback scheduling in
     *     microseconds.
     * @param periodSeconds The period at which to run the callback in microseconds.
     * @param offsetSeconds The offset from the common starting time in microseconds.
     */
    Callback(Runnable func, long startTimeUs, long periodUs, long offsetUs) {
      this.func = func;
      this.period = periodUs;
      this.expirationTime =
          startTimeUs
              + offsetUs
              + this.period
              + (RobotController.getFPGATime() - startTimeUs) / this.period * this.period;
    }

    @Override
    public boolean equals(Object rhs) {
      return rhs instanceof Callback callback && expirationTime == callback.expirationTime;
    }

    @Override
    public int hashCode() {
      return Double.hashCode(expirationTime);
    }

    @Override
    public int compareTo(Callback rhs) {
      // Elements with sooner expiration times are sorted as lesser. The head of
      // Java's PriorityQueue is the least element.
      return Long.compare(expirationTime, rhs.expirationTime);
    }
  }

  public static final double defaultPeriodSecs = 0.02;
  private final int notifier = NotifierJNI.initializeNotifier();
  private final long periodUs;
  private long nextCycleUs = 0;
  private final GcStatsCollector gcStatsCollector = new GcStatsCollector();
  private final PriorityQueue<Callback> m_callbacks = new PriorityQueue<>();

  private boolean useTiming = true;

  /** Constructor for LoggedRobot. */
  protected LoggedRobot() {
    this(defaultPeriodSecs);
  }

  /**
   * Constructor for LoggedRobot.
   *
   * @param period Period in seconds.
   */
  protected LoggedRobot(double period) {
    super(period);
    this.periodUs = (long) (period * 1000000);
    addPeriodic(this::loopFunc, period);
    NotifierJNI.setNotifierName(notifier, "LoggedRobot");

    HAL.report(tResourceType.kResourceType_Framework, tInstances.kFramework_AdvantageKit);
  }

  @Override
  @SuppressWarnings("NoFinalizer")
  protected void finalize() {
    NotifierJNI.stopNotifier(notifier);
    NotifierJNI.cleanNotifier(notifier);
  }

  /** Provide an alternate "main loop" via startCompetition(). */
  @Override
  @SuppressWarnings("UnsafeFinalization")
  public void startCompetition() {
    // Robot init methods
    long initStart = Logger.getRealTimestamp();
    robotInit();
    if (isSimulation()) {
      simulationInit();
    }
    long initEnd = Logger.getRealTimestamp();

    // Register auto logged outputs
    AutoLogOutputManager.registerFields(this);

    // Save data from init cycle
    Logger.periodicAfterUser(initEnd - initStart, 0);

    // Tell the DS that the robot is ready to be enabled
    System.out.println("********** Robot program startup complete **********");
    DriverStationJNI.observeUserProgramStarting();

    // Loop forever, calling the appropriate mode-dependent function
    while (true) {
      if (useTiming) {
        long currentTimeUs = Logger.getRealTimestamp();
        if (nextCycleUs < currentTimeUs) {
          // Loop overrun, start next cycle immediately
          nextCycleUs = currentTimeUs;
        } else {
          // Wait before next cycle
          NotifierJNI.updateNotifierAlarm(notifier, nextCycleUs);
          if (NotifierJNI.waitForNotifierAlarm(notifier) == 0L) {
            // Break the loop if the notifier was stopped
            Logger.end();
            break;
          }
        }
        nextCycleUs += periodUs;
      }

      // We don't have to check there's an element in the queue first because
      // there's always at least one (the constructor adds one). It's reenqueued
      // at the end of the loop.
      var callback = m_callbacks.poll();

      long periodicBeforeStart = Logger.getRealTimestamp();
      Logger.periodicBeforeUser();
      long userCodeStart = Logger.getRealTimestamp();
      callback.func.run();

     // Process all other callbacks that are ready to run
      while (m_callbacks.peek().expirationTime <= currentTime) {
        callback = m_callbacks.poll();

        callback.func.run();

        // Increment the expiration time by the number of full periods it's behind
        // plus one to avoid rapid repeat fires from a large loop overrun. We
        // assume currentTime ≥ expirationTime rather than checking for it since
        // the callback wouldn't be running otherwise.
        callback.expirationTime +=
            callback.period
                + (currentTime - callback.expirationTime) / callback.period * callback.period;
        m_callbacks.add(callback);
          callback.expirationTime +=
              callback.period
                  + (currentTime - callback.expirationTime) / callback.period * callback.period;
          m_callbacks.add(callback);
      }
      long userCodeEnd = Logger.getRealTimestamp();

      gcStatsCollector.update();
      Logger.periodicAfterUser(userCodeEnd - userCodeStart, userCodeStart - periodicBeforeStart);
    }
  }


  /** Ends the main loop in startCompetition(). */
  @Override
  public void endCompetition() {
    NotifierJNI.stopNotifier(notifier);
  }

  /** Sets whether to use standard timing or run as fast as possible. */
  public void setUseTiming(boolean useTiming) {
    this.useTiming = useTiming;
  }

  private static final class GcStatsCollector {
    private List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private final long[] lastTimes = new long[gcBeans.size()];
    private final long[] lastCounts = new long[gcBeans.size()];

    public void update() {
      long accumTime = 0;
      long accumCounts = 0;
      for (int i = 0; i < gcBeans.size(); i++) {
        long gcTime = gcBeans.get(i).getCollectionTime();
        long gcCount = gcBeans.get(i).getCollectionCount();
        accumTime += gcTime - lastTimes[i];
        accumCounts += gcCount - lastCounts[i];

        lastTimes[i] = gcTime;
        lastCounts[i] = gcCount;
      }

      Logger.recordOutput("LoggedRobot/GCTimeMS", (double) accumTime);
      Logger.recordOutput("LoggedRobot/GCCounts", (double) accumCounts);
    }
  }


  /**
   * Add a callback to run at a specific period.
   *
   * <p>This is scheduled on TimedRobot's Notifier, so TimedRobot and the callback run
   * synchronously. Interactions between them are thread-safe.
   *
   * @param callback The callback to run.
   * @param periodSeconds The period at which to run the callback in seconds.
   */
  public final void addPeriodic(Runnable callback, double periodSeconds) {
    m_callbacks.add(new Callback(callback, m_startTimeUs, (long) (periodSeconds * 1e6), 0));
  }

  /**
   * Add a callback to run at a specific period with a starting time offset.
   *
   * <p>This is scheduled on TimedRobot's Notifier, so TimedRobot and the callback run
   * synchronously. Interactions between them are thread-safe.
   *
   * @param callback The callback to run.
   * @param periodSeconds The period at which to run the callback in seconds.
   * @param offsetSeconds The offset from the common starting time in seconds. This is useful for
   *     scheduling a callback in a different timeslot relative to TimedRobot.
   */
  public final void addPeriodic(Runnable callback, double periodSeconds, double offsetSeconds) {
    m_callbacks.add(
        new Callback(
            callback, m_startTimeUs, (long) (periodSeconds * 1e6), (long) (offsetSeconds * 1e6)));
  }

  /**
   * Add a callback to run at a specific period.
   *
   * <p>This is scheduled on TimedRobot's Notifier, so TimedRobot and the callback run
   * synchronously. Interactions between them are thread-safe.
   *
   * @param callback The callback to run.
   * @param period The period at which to run the callback.
   */
  public final void addPeriodic(Runnable callback, Time period) {
    addPeriodic(callback, period.in(Seconds));
  }

  /**
   * Add a callback to run at a specific period with a starting time offset.
   *
   * <p>This is scheduled on TimedRobot's Notifier, so TimedRobot and the callback run
   * synchronously. Interactions between them are thread-safe.
   *
   * @param callback The callback to run.
   * @param period The period at which to run the callback.
   * @param offset The offset from the common starting time. This is useful for scheduling a
   *     callback in a different timeslot relative to TimedRobot.
   */
  public final void addPeriodic(Runnable callback, Time period, Time offset) {
    addPeriodic(callback, period.in(Seconds), offset.in(Seconds));
  }
}
