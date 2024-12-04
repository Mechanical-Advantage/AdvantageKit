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

package frc.robot.subsystems.launcher;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.util.NoteVisualizer;
import org.littletonrobotics.junction.Logger;

public class Launcher extends SubsystemBase {
  private static final double launchSpeedLauncher = 1.0;
  private static final double launchSpeedFeeder = 1.0;
  private static final double intakeSpeedLauncher = -1.0;
  private static final double intakeSpeedFeeder = -0.2;
  private static final double launchDelay = 1.0;

  private final LauncherIO io;
  private final LauncherIOInputsAutoLogged inputs = new LauncherIOInputsAutoLogged();

  public Launcher(LauncherIO io) {
    this.io = io;
    setDefaultCommand(
        run(
            () -> {
              io.setLaunchVoltage(0.0);
              io.setFeedVoltage(0.0);
            }));
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Launcher", inputs);
  }

  /** Returns a command that intakes a note. */
  public Command intakeCommand() {
    return startEnd(
        () -> {
          io.setLaunchVoltage(intakeSpeedLauncher);
          io.setFeedVoltage(intakeSpeedFeeder);
        },
        () -> {
          io.setLaunchVoltage(0.0);
          io.setFeedVoltage(0.0);
        });
  }

  /** Returns a command that launches a note. */
  public Command launchCommand() {
    return Commands.sequence(
            runOnce(
                () -> {
                  io.setLaunchVoltage(launchSpeedLauncher);
                }),
            Commands.waitSeconds(launchDelay),
            runOnce(
                () -> {
                  io.setFeedVoltage(launchSpeedFeeder);
                }),
            NoteVisualizer.shoot(),
            Commands.idle())
        .finallyDo(
            () -> {
              io.setLaunchVoltage(0.0);
              io.setFeedVoltage(0.0);
            });
  }
}
