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

package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Direction;
import frc.robot.subsystems.drive.Drive;
import frc.robot.subsystems.drive.DriveIO;
import frc.robot.subsystems.drive.DriveIOSim;
import frc.robot.subsystems.drive.DriveIOSparkMax;
import frc.robot.subsystems.launcher.Launcher;
import frc.robot.subsystems.launcher.LauncherIO;
import frc.robot.subsystems.launcher.LauncherIOSim;
import frc.robot.subsystems.launcher.LauncherIOSparkMax;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // Subsystems
  private final Drive drive;
  private final Launcher launcher;

  // Controller
  private final CommandXboxController controller = new CommandXboxController(0);

  // Dashboard inputs
  private final LoggedDashboardChooser<Command> autoChooser;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    switch (Constants.currentMode) {
      case REAL:
        // Real robot, instantiate hardware IO implementations
        drive = new Drive(new DriveIOSparkMax());
        // drive = new Drive(new DriveIOTalonFX());
        launcher = new Launcher(new LauncherIOSparkMax());
        // launcher = new Launcher(new LauncherIOTalonFX());
        break;

      case SIM:
        // Sim robot, instantiate physics sim IO implementations
        drive = new Drive(new DriveIOSim());
        launcher = new Launcher(new LauncherIOSim());
        break;

      default:
        // Replayed robot, disable IO implementations
        drive = new Drive(new DriveIO() {});
        launcher = new Launcher(new LauncherIO() {});
        break;
    }

    // Set up auto routines
    NamedCommands.registerCommand("Launch", launcher.launchCommand().withTimeout(3.0));
    autoChooser = new LoggedDashboardChooser<>("Auto Choices", AutoBuilder.buildAutoChooser());

    // Set up SysId
    var sysIdRoutine =
        new SysIdRoutine(
            new SysIdRoutine.Config(
                null, null, null, (state) -> Logger.recordOutput("SysIdState", state)),
            new SysIdRoutine.Mechanism(
                (volts) -> drive.driveVolts(volts.in(Units.Volts), volts.in(Units.Volts)),
                null, // Inputs logged by AdvantageKit
                drive));
    autoChooser.addOption(
        "Drive SysId (Quasistatic Forward)", sysIdRoutine.quasistatic(Direction.kForward));
    autoChooser.addOption(
        "Drive SysId (Quasistatic Reverse)", sysIdRoutine.quasistatic(Direction.kReverse));
    autoChooser.addOption(
        "Drive SysId (Dynamic Forward)", sysIdRoutine.dynamic(Direction.kForward));
    autoChooser.addOption(
        "Drive SysId (Dynamic Reverse)", sysIdRoutine.dynamic(Direction.kReverse));

    // Configure the button bindings
    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    drive.setDefaultCommand(
        Commands.run(
            () -> drive.driveArcade(-controller.getLeftY(), controller.getLeftX()), drive));
    controller.leftBumper().whileTrue(launcher.intakeCommand());
    controller.rightBumper().whileTrue(launcher.launchCommand());
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autoChooser.get();
  }
}
