// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package first.robot.mechanisms.drive;

import static org.wpilib.units.Units.*;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.swerve.SwerveModuleConstants;
import org.littletonrobotics.junction.Logger;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.kinematics.SwerveModulePosition;
import org.wpilib.math.kinematics.SwerveModuleVelocity;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Distance;
import org.wpilib.units.measure.LinearVelocity;
import org.wpilib.util.Alert;

public class Module {
  private final ModuleIO io;
  private final ModuleIOInputsAutoLogged inputs = new ModuleIOInputsAutoLogged();
  private final int index;
  private final SwerveModuleConstants<
          TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>
      constants;

  private final Alert driveDisconnectedAlert;
  private final Alert turnDisconnectedAlert;
  private final Alert turnEncoderDisconnectedAlert;
  private SwerveModulePosition[] odometryPositions = new SwerveModulePosition[] {};

  public Module(
      ModuleIO io,
      int index,
      SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>
          constants) {
    this.io = io;
    this.index = index;
    this.constants = constants;
    driveDisconnectedAlert =
        new Alert(
            "driveMotor" + index + "Disconnected",
            "Disconnected drive motor on module " + Integer.toString(index) + ".",
            Alert.Level.HIGH);
    turnDisconnectedAlert =
        new Alert(
            "turnMotor" + index + "Disconnected",
            "Disconnected turn motor on module " + Integer.toString(index) + ".",
            Alert.Level.HIGH);
    turnEncoderDisconnectedAlert =
        new Alert(
            "turnEncoder" + index + "Disconnected",
            "Disconnected turn encoder on module " + Integer.toString(index) + ".",
            Alert.Level.HIGH);
  }

  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Drive/Module" + Integer.toString(index), inputs);

    // Calculate positions for odometry
    int sampleCount = inputs.odometryTimestamps.length; // All signals are sampled together
    odometryPositions = new SwerveModulePosition[sampleCount];
    for (int i = 0; i < sampleCount; i++) {
      double positionMeters =
          inputs.odometryDrivePositionsRad[i].in(Radians) * constants.WheelRadius;
      Rotation2d angle = inputs.odometryTurnPositions[i];
      odometryPositions[i] = new SwerveModulePosition(positionMeters, angle);
    }

    // Update alerts
    driveDisconnectedAlert.set(!inputs.driveConnected);
    turnDisconnectedAlert.set(!inputs.turnConnected);
    turnEncoderDisconnectedAlert.set(!inputs.turnEncoderConnected);
  }

  /** Runs the module with the specified setpoint state. Mutates the state to optimize it. */
  public SwerveModuleVelocity runSetpoint(SwerveModuleVelocity state) {
    // Optimize velocity setpoint
    SwerveModuleVelocity optimizedState =
        state.optimize(getAngle()).cosineScale(inputs.turnPosition);

    // Apply setpoints
    io.setDriveVelocity(state.velocity / constants.WheelRadius);
    io.setTurnPosition(state.angle);

    return optimizedState;
  }

  /** Runs the module with the specified output while controlling to zero degrees. */
  public void runCharacterization(double output) {
    io.setDriveOpenLoop(output);
    io.setTurnPosition(Rotation2d.ZERO);
  }

  /** Disables all outputs to motors. */
  public void stop() {
    io.setDriveOpenLoop(0.0);
    io.setTurnOpenLoop(0.0);
  }

  /** Returns the current turn angle of the module. */
  public Rotation2d getAngle() {
    return inputs.turnPosition;
  }

  /** Returns the current drive position of the module. */
  public Distance getPosition() {
    return inputs.drivePosition.div(Radians.of(1)).times(Meters.of(constants.WheelRadius));
  }

  /** Returns the current drive velocity of the module. */
  public LinearVelocity getVelocity() {
    return inputs
        .driveVelocity
        .div(RadiansPerSecond.of(1))
        .times(MetersPerSecond.of(constants.WheelRadius));
  }

  /** Returns the module position (turn angle and drive position). */
  public SwerveModulePosition getModulePosition() {
    return new SwerveModulePosition(getPosition(), getAngle());
  }

  /** Returns the module state (turn angle and drive velocity). */
  public SwerveModuleVelocity getState() {
    return new SwerveModuleVelocity(getVelocity(), getAngle());
  }

  /** Returns the module positions received this cycle. */
  public SwerveModulePosition[] getOdometryPositions() {
    return odometryPositions;
  }

  /** Returns the timestamps of the samples received this cycle. */
  public double[] getOdometryTimestamps() {
    return inputs.odometryTimestamps;
  }

  /** Returns the module position. */
  public Angle getWheelRadiusCharacterizationPosition() {
    return inputs.drivePosition;
  }

  /** Returns the module velocity. */
  public AngularVelocity getFFCharacterizationVelocity() {
    return inputs.driveVelocity;
  }
}
