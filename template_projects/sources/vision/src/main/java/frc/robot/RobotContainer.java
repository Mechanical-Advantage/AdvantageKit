// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import static frc.robot.subsystems.vision.VisionConstants.*;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandGenericHID;
import frc.robot.subsystems.drive.DemoDrive;
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionIO;
import frc.robot.subsystems.vision.VisionIOLimelight;
import frc.robot.subsystems.vision.VisionIOPhotonVisionSim;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  private final Vision vision;

  private final DemoDrive drive = new DemoDrive(); // Demo drive subsystem, sim only
  private final CommandGenericHID keyboard = new CommandGenericHID(0); // Keyboard 0 on port 0

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    switch (Constants.currentMode) {
      case REAL:
        // Real robot, instantiate hardware IO implementations
        vision =
            new Vision(
                drive::addVisionMeasurement,
                new VisionIOLimelight(camera0Name, drive::getRotation),
                new VisionIOLimelight(camera1Name, drive::getRotation));
        // vision =
        // new Vision(
        // demoDrive::addVisionMeasurement,
        // new VisionIOPhotonVision(camera0Name, robotToCamera0),
        // new VisionIOPhotonVision(camera1Name, robotToCamera1));
        break;

      case SIM:
        // Sim robot, instantiate physics sim IO implementations
        vision =
            new Vision(
                drive::addVisionMeasurement,
                new VisionIOPhotonVisionSim(camera0Name, robotToCamera0, drive::getPose),
                new VisionIOPhotonVisionSim(camera1Name, robotToCamera1, drive::getPose));
        break;

      default:
        // Replayed robot, disable IO implementations
        // (Use same number of dummy implementations as the real robot)
        vision = new Vision(drive::addVisionMeasurement, new VisionIO() {}, new VisionIO() {});
        break;
    }

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
    // Joystick drive command
    drive.setDefaultCommand(
        Commands.run(
            () -> {
              drive.run(-keyboard.getRawAxis(1), -keyboard.getRawAxis(0));
            },
            drive));

    // Auto aim command example
    @SuppressWarnings("resource")
    PIDController aimController = new PIDController(0.2, 0.0, 0.0);
    aimController.enableContinuousInput(-Math.PI, Math.PI);
    keyboard
        .button(1)
        .whileTrue(
            Commands.startRun(
                () -> {
                  aimController.reset();
                },
                () -> {
                  drive.run(0.0, aimController.calculate(vision.getTargetX(0).getRadians()));
                },
                drive));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return Commands.none();
  }
}
