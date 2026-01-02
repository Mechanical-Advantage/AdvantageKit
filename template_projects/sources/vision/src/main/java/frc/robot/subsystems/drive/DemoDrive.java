// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.drive;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.estimator.DifferentialDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim.KitbotGearing;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim.KitbotMotor;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim.KitbotWheelSize;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.littletonrobotics.junction.AutoLogOutput;

/**
 * <b>IMPORTANT: This is a simple simulator for a differential drive, and has no support for real
 * hardware or IO implementations. It it intended only for simple testing in simulation.</b>
 *
 * <p>Please reference the other AdvantageKit template projects for more complete examples of drive
 * subsystems, including swerve and differential drive. Any subsystem with equivalent {@link
 * #getPose()}, {@link #getRotation()}, and {@link #addVisionMeasurement(Pose2d, double, Matrix)}
 * methods is compatible with this project's vision code.
 */
public class DemoDrive extends SubsystemBase {
  private final DifferentialDrivetrainSim sim =
      DifferentialDrivetrainSim.createKitbotSim(
          KitbotMotor.kDualCIMPerSide, KitbotGearing.k10p71, KitbotWheelSize.kSixInch, null);
  private final DifferentialDrivePoseEstimator poseEstimator =
      new DifferentialDrivePoseEstimator(
          new DifferentialDriveKinematics(Units.inchesToMeters(26)),
          Rotation2d.kZero,
          0.0,
          0.0,
          Pose2d.kZero);

  @Override
  public void periodic() {
    sim.update(0.02);
    poseEstimator.update(
        sim.getHeading(), sim.getLeftPositionMeters(), sim.getRightPositionMeters());
  }

  /**
   * Drive open loop with percent out.
   *
   * @param xAxis The forward-back axis, where positive is forward.
   * @param zAxis The left-right axis, where positive is left.
   */
  public void run(double xAxis, double zAxis) {
    sim.setInputs((xAxis - zAxis) * 12.0, (xAxis + zAxis) * 12.0);
  }

  /** Returns the latest estimated pose from the pose estimator. */
  @AutoLogOutput(key = "EstimatedPose")
  public Pose2d getPose() {
    return poseEstimator.getEstimatedPosition();
  }

  /** Returns the latest estimated rotation from the pose estimator. */
  public Rotation2d getRotation() {
    return getPose().getRotation();
  }

  /** Adds a new timestamped vision measurement. */
  public void addVisionMeasurement(
      Pose2d visionRobotPoseMeters,
      double timestampSeconds,
      Matrix<N3, N1> visionMeasurementStdDevs) {
    poseEstimator.addVisionMeasurement(
        visionRobotPoseMeters, timestampSeconds, visionMeasurementStdDevs);
  }
}
