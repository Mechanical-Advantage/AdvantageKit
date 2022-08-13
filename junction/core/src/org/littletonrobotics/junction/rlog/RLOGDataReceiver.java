package org.littletonrobotics.junction.rlog;

/**
 * Receives entries (encoded as bytes in the RLOG format) from the logging
 * system during real operation or simulation.
 */
public interface RLOGDataReceiver {

  /**
   * Called before the logging system begins reporting data. This should be used
   * to connect to files, find network devices, start threads, etc. The encoder
   * object provided here will be updated as records change.
   */
  public default void start(RLOGEncoder encoder) {
  };

  /**
   * Called when the code shuts down cleanly. Note that this will NOT be called
   * when the robot is powered off.
   */
  public default void end() {
  };

  /**
   * Called every loop cycle when a new entry is complete. Call "getOutput()" on
   * the encoder to retrieve the encoded values. The data can be processed
   * immediately or queued for later.
   */
  public void processEntry();
}
