package org.littletonrobotics.junction;

import edu.wpi.first.hal.DriverStationJNI;
import edu.wpi.first.hal.FRCNetComm.tInstances;
import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.wpilibj.IterativeRobotBase;
import edu.wpi.first.hal.HAL;
import edu.wpi.first.hal.NotifierJNI;
import edu.wpi.first.wpilibj.Timer;
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
    public double period;
    public double expirationTime;

    /**
     * Construct a callback container.
     *
     * @param func The callback to run.
     * @param startTimeSeconds The common starting point for all callback scheduling in seconds.
     * @param periodSeconds The period at which to run the callback in seconds.
     * @param offsetSeconds The offset from the common starting time in seconds.
     */
    Callback(Runnable func, double startTimeSeconds, double periodSeconds, double offsetSeconds) {
      this.func = func;
      this.period = periodSeconds;
      this.expirationTime =
              startTimeSeconds
                      + offsetSeconds
                      + Math.floor((Timer.getFPGATimestamp() - startTimeSeconds) / this.period)
                      * this.period
                      + this.period;
    }

    @Override
    public boolean equals(Object rhs) {
      if (rhs instanceof Callback) {
        return Double.compare(expirationTime, ((Callback) rhs).expirationTime) == 0;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Double.hashCode(expirationTime);
    }

    @Override
    public int compareTo(Callback rhs) {
      // Elements with sooner expiration times are sorted as lesser. The head of
      // Java's PriorityQueue is the least element.
      return Double.compare(expirationTime, rhs.expirationTime);
    }
  }

  public static final double defaultPeriodSecs = 0.02;
  private final int notifier = NotifierJNI.initializeNotifier();
  private double m_startTime;
  private final PriorityQueue<Callback> m_callbacks = new PriorityQueue<>();
  private final long periodUs;
  private long nextCycleUs = 0;

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

    HAL.report(tResourceType.kResourceType_Framework, tInstances.kFramework_Timed);
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
    robotInit();

    if (isSimulation()) {
      simulationInit();
    }

    // Save data from init cycle
    Logger.getInstance().periodicAfterUser();

    // Tell the DS that the robot is ready to be enabled
    System.out.println("********** Robot program startup complete **********");
    DriverStationJNI.observeUserProgramStarting();

    // Loop forever, calling the appropriate mode-dependent function
    while (true) {
      if (useTiming) {
        long currentTimeUs = Logger.getInstance().getRealTimestamp();
        if (nextCycleUs < currentTimeUs) {
          // Loop overrun, start next cycle immediately
          nextCycleUs = currentTimeUs;
        } else {
          // Wait before next cycle
          NotifierJNI.updateNotifierAlarm(notifier, nextCycleUs);
          NotifierJNI.waitForNotifierAlarm(notifier);
        }
        nextCycleUs += periodUs;
      }

      long loopCycleStart = Logger.getInstance().getRealTimestamp();
      Logger.getInstance().periodicBeforeUser();
      long userCodeStart = Logger.getInstance().getRealTimestamp();
      var callback = m_callbacks.poll();

      NotifierJNI.updateNotifierAlarm(notifier, (long) (callback.expirationTime * 1e6));

      long curTime = NotifierJNI.waitForNotifierAlarm(notifier);
      if (curTime == 0) {
        break;
      }

      callback.func.run();

      callback.expirationTime += callback.period;
      m_callbacks.add(callback);

      // Process all other callbacks that are ready to run
      while ((long) (m_callbacks.peek().expirationTime * 1e6) <= curTime) {
        callback = m_callbacks.poll();

        callback.func.run();

        callback.expirationTime += callback.period;
        m_callbacks.add(callback);
      }
      long loopCycleEnd = Logger.getInstance().getRealTimestamp();
      Logger.getInstance().recordOutput("LoggedRobot/FullCycleMS", (loopCycleEnd - loopCycleStart) / 1000.0);
      Logger.getInstance().recordOutput("LoggedRobot/LogPeriodicMS", (userCodeStart - loopCycleStart) / 1000.0);
      Logger.getInstance().recordOutput("LoggedRobot/UserCodeMS", (loopCycleEnd - userCodeStart) / 1000.0);

      Logger.getInstance().periodicAfterUser(); // Save data
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

  /**
   * Add a callback to run at a specific period.
   *
   * <p>This is scheduled on TimedRobot's Notifier, so TimedRobot and the callback run
   * synchronously. Interactions between them are thread-safe.
   *
   * @param callback The callback to run.
   * @param periodSeconds The period at which to run the callback in seconds.
   */
  public void addPeriodic(Runnable callback, double periodSeconds) {
    m_callbacks.add(new Callback(callback, m_startTime, periodSeconds, 0.0));
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
  public void addPeriodic(Runnable callback, double periodSeconds, double offsetSeconds) {
    m_callbacks.add(new Callback(callback, m_startTime, periodSeconds, offsetSeconds));
  }
}
