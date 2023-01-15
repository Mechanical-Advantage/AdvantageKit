// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.wpilibj;

/**
 * @Deprecated The main robot class should inherit from LoggedRobot instead of
 *             TimesliceRobot when using AdvantageKit's WPILib shims.
 */
@Deprecated
public final class TimesliceRobot {
    public TimesliceRobot(double robotPeriodicAllocation, double controllerPeriod) {
    }

    public void schedule(Runnable func, double allocation) {
    }
}