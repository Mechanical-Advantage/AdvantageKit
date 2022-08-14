package org.littletonrobotics.junction;

/**
 * Provides a stream of log entries to be fed back to the robot code during
 * simulation.
 */
public interface LogReplaySource {

  /**
   * Called before the logging system begins reporting data. This should be used
   * to connect to files, find network devices, start threads, etc.
   */
  public default void start() {
  };

  /**
   * Called when the code shuts down cleanly. Note that this will NOT be called
   * when the robot is powered off.
   */
  public default void end() {
  };

  /**
   * Called every loop cycle to get the next set of data. Return null to end
   * replay.
   */
  public LogTable getEntry(LogTable lastEntry);
}
