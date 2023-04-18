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
  default void start() {
  }

    /**
   * Called when the code shuts down cleanly. Note that this will NOT be called
   * when the robot is powered off.
   */
  default void end() {
  }

    /**
   * Called every loop cycle to get the next set of data.
   * 
   * @param table A reference to the current data table, to be updated with new
   *              data (including a timestamp).
   * @return A boolean indicating whether the replay should continue.
   */
    boolean updateTable(LogTable table);
}
