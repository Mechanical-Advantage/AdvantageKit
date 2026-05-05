// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import org.wpilib.driverstation.internal.DriverStationBackend;
import org.wpilib.framework.IterativeRobotBase;
import org.wpilib.hardware.hal.HAL;
import org.wpilib.hardware.hal.NotifierJNI;
import org.wpilib.system.RobotController;
import org.wpilib.util.WPIUtilJNI;

/**
 * LoggedRobot implements the IterativeRobotBase robot program framework.
 *
 * <p>The LoggedRobot class is intended to be subclassed by a user creating a robot program, and
 * will call all required AdvantageKit periodic methods.
 *
 * <p>periodic() functions from the base class are called on an interval by a Notifier instance.
 */
public class LoggedRobot extends IterativeRobotBase {
  /** Default loop period. */
  public static final double defaultPeriodSecs = 0.02;

  private final int notifier = NotifierJNI.createNotifier();
  private final long periodUs;
  private long nextCycleUs = 0;
  private final GcStatsCollector gcStatsCollector = new GcStatsCollector();

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
    NotifierJNI.setNotifierName(notifier, "LoggedRobot");

    HAL.reportUsage("Framework", "AdvantageKit");
    HAL.reportUsage("LoggingFramework", "AdvantageKit");
  }

  @Override
  public void close() {
    NotifierJNI.destroyNotifier(notifier);
    super.close();
  }

  /** Provide an alternate "main loop" via startCompetition(). */
  @Override
  @SuppressWarnings("UnsafeFinalization")
  public void startCompetition() {
    try {
      // Robot init methods
      long initStart = RobotController.getMonotonicTime();
      if (isSimulation()) {
        simulationInit();
      }
      long initEnd = RobotController.getMonotonicTime(); // Includes Robot constructor and robotInit

      // Register auto logged outputs
      AutoLogOutputManager.addObject(this);

      // Save data from init cycle
      Logger.periodicAfterUser(initEnd - initStart, 0);

      // Tell the DS that the robot is ready to be enabled
      System.out.println("********** Robot program startup complete **********");
      DriverStationBackend.observeUserProgramStarting();

      // Loop forever, calling the appropriate mode-dependent function
      while (true) {
        if (useTiming) {
          long currentTimeUs = RobotController.getMonotonicTime();
          if (nextCycleUs < currentTimeUs) {
            // Loop overrun, start next cycle immediately
            nextCycleUs = currentTimeUs;
          } else {
            // Wait before next cycle
            NotifierJNI.setNotifierAlarm(notifier, nextCycleUs, 0, true, true);

            try {
              WPIUtilJNI.waitForObject(notifier);
            } catch (InterruptedException ex) {
              Logger.end();
              Thread.currentThread().interrupt();
              break;
            }
          }
          nextCycleUs += periodUs;
        }

        long periodicBeforeStart = RobotController.getMonotonicTime();
        Logger.periodicBeforeUser();
        long userCodeStart = RobotController.getMonotonicTime();
        loopFunc();
        long userCodeEnd = RobotController.getMonotonicTime();

        gcStatsCollector.update();
        Logger.periodicAfterUser(userCodeEnd - userCodeStart, userCodeStart - periodicBeforeStart);
      }
    } catch (Exception exception) {
      // Exception thrown, log crash information
      StringWriter stringWriter = new StringWriter();
      exception.printStackTrace(new PrintWriter(stringWriter));
      Logger.periodicAfterUser(0, 0, stringWriter.toString());
      Logger.end();
      throw exception;
    }
  }

  /** Ends the main loop in startCompetition(). */
  @Override
  public void endCompetition() {
    NotifierJNI.destroyNotifier(notifier);
  }

  /**
   * Sets whether to use standard timing or run as fast as possible.
   *
   * @param useTiming If true, use standard timing. If false, run as fast as possible.
   */
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
}
