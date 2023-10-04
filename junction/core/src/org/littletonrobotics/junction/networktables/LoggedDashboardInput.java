package org.littletonrobotics.junction.networktables;

public interface LoggedDashboardInput {
  String prefix = "DashboardInputs";

  /**
   * Update the current value and save/replay the input. This function should not
   * be called by the user.
   */
  void periodic();
}
