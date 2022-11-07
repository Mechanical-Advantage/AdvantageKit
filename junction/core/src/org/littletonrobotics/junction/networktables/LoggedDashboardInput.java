package org.littletonrobotics.junction.networktables;

public interface LoggedDashboardInput {
  public static final String prefix = "DashboardInputs";

  /**
   * Update the current value and save/replay the input. This function should not
   * be called by the user.
   */
  public void periodic();
}
