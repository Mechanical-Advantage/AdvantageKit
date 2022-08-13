package org.littletonrobotics.junction;

/**
 * Receives entries from the logging system during real operation or simulation.
 */
public interface LogDataReceiver {

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
   * Called every loop cycle when a new entry is complete. This data can be
   * processed immediately or queued for later.
   */
  public void putEntry(LogTable entry);
}
