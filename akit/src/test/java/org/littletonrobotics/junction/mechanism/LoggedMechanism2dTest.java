package org.littletonrobotics.junction.mechanism;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

// import static org.junit.jupiter.api.Assertions.assertTrue;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Meters;

import org.junit.jupiter.api.Test;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;

public class LoggedMechanism2dTest {
  private double DELTA = 0.001; // 0.1% or 1mm

  @Test
  public void TestSimpleArmFK() {
    // Simple arm setup, testing ForwardKinematics, unit test so ignore close
    @SuppressWarnings("resource")
    LoggedMechanism2d mech2d = new LoggedMechanism2d(0, 0); // don't care, values don't do anything

    // leave the root at the robot origin
    LoggedMechanismRoot2d mechRoot = mech2d.getRoot("root", 0, 0);
    // straight up 0.5 meters
    LoggedMechanismLigament2d ligamentBase = mechRoot.append(
      new LoggedMechanismLigament2d("base", Meters.of(0.5), Degrees.of(90))
    );
    LoggedMechanismLigament2d ligamentArm = ligamentBase.append(
      new LoggedMechanismLigament2d("arm", Meters.of(0.5), Degrees.of(0))
    );

    ArrayList<Pose3d> poses = mech2d.generate3dMechanism();

    assertEquals(poses.size(), 2);
    assertEquals(0, poses.get(0).getTranslation().getDistance(new Translation3d()), DELTA);
    assertEquals(0, poses.get(1).getTranslation().getDistance(new Translation3d(0,0,0.5)), DELTA);

    // Test EndEffector location
    Pose3d lastPose = poses.get(poses.size()-1);
    Pose3d endeff = lastPose.transformBy(
      new Transform3d(ligamentArm.getLength(), 0, 0, Rotation3d.kZero)
    );
    assertEquals(0, endeff.getTranslation().getDistance(new Translation3d(0, 0, 1.0)), DELTA);
    // I have no idea which way WPILib will calculate the axis, and the y term might flip so check both directions
    // This is an issue with euler angles and WPILib's limited BLAS implementation
    var rot_axis = endeff.getRotation().getAxis();
    if (rot_axis.get(1) > 0) { // rotation axis is correct
      assertEquals(-90, endeff.getRotation().getMeasureAngle().in(Degrees), DELTA);
    } else {
      assertEquals(90, endeff.getRotation().getMeasureAngle().in(Degrees), DELTA);
    }
  }

  @Test
  public void TestSimpleBentArmFK() {
    // Simple arm setup, testing ForwardKinematics, unit test so ignore close
    @SuppressWarnings("resource")
    LoggedMechanism2d mech2d = new LoggedMechanism2d(0, 0); // don't care, values don't do anything

    // leave the root at the robot origin
    LoggedMechanismRoot2d mechRoot = mech2d.getRoot("root", 0, 0);
    // straight up 0.5 meters
    LoggedMechanismLigament2d ligamentBase = mechRoot.append(
      new LoggedMechanismLigament2d("base", Meters.of(0.5), Degrees.of(90))
    );
    LoggedMechanismLigament2d ligamentArm = ligamentBase.append(
      new LoggedMechanismLigament2d("arm", Meters.of(0.5), Degrees.of(-90))
    );

    ArrayList<Pose3d> poses = mech2d.generate3dMechanism();

    assertEquals(poses.size(), 2);
    assertEquals(0, poses.get(0).getTranslation().getDistance(new Translation3d()), DELTA);
    assertEquals(0, poses.get(1).getTranslation().getDistance(new Translation3d(0,0,0.5)), DELTA);

    // Test EndEffector location
    Pose3d lastPose = poses.get(poses.size()-1);
    Pose3d endeff = lastPose.transformBy(
      new Transform3d(ligamentArm.getLength(), 0, 0, Rotation3d.kZero)
    );
    assertEquals(0, endeff.getTranslation().getDistance(new Translation3d(0.5, 0, 0.5)), DELTA);
    // We probably don't need to care about axis/angle here because it should return to the neutral position
    // ... probably.
    assertEquals(0, endeff.getRotation().getMeasureAngle().in(Degrees), DELTA);
  }

  @Test
  public void TestThreeSegmentFK() {
    // Simple arm setup, testing ForwardKinematics, unit test so ignore close
    @SuppressWarnings("resource")
    LoggedMechanism2d mech2d = new LoggedMechanism2d(0, 0); // don't care, values don't do anything

    // leave the root at the robot origin
    LoggedMechanismRoot2d mechRoot = mech2d.getRoot("root", 0, 0);
    // straight up 0.5 meters
    LoggedMechanismLigament2d ligamentBase = mechRoot.append(
      new LoggedMechanismLigament2d("base", Meters.of(0.5), Degrees.of(90))
    );
    // back forwards parallel to the ground
    LoggedMechanismLigament2d ligamentArm = ligamentBase.append(
      new LoggedMechanismLigament2d("arm", Meters.of(0.5), Degrees.of(-90))
    );
    // and again back upwards
    LoggedMechanismLigament2d ligamentGripper = ligamentArm.append(
      new LoggedMechanismLigament2d("gripper", Meters.of(0.2), Degrees.of(90))
    );

    ArrayList<Pose3d> poses = mech2d.generate3dMechanism();

    assertEquals(poses.size(), 3);
    assertEquals(0, poses.get(0).getTranslation().getDistance(new Translation3d()), DELTA);
    assertEquals(0, poses.get(1).getTranslation().getDistance(new Translation3d(0,0,0.5)), DELTA);
    assertEquals(0, poses.get(2).getTranslation().getDistance(new Translation3d(0.5,0,0.5)), DELTA);

    // Test EndEffector location
    Pose3d lastPose = poses.get(poses.size()-1);
    Pose3d endeff = lastPose.transformBy(
      new Transform3d(ligamentGripper.getLength(), 0, 0, Rotation3d.kZero)
    );
    assertEquals(0, endeff.getTranslation().getDistance(new Translation3d(0.5, 0, 0.7)), DELTA);
    var rot_axis = endeff.getRotation().getAxis();
    if (rot_axis.get(1) > 0) { // rotation axis is correct
      assertEquals(-90, endeff.getRotation().getMeasureAngle().in(Degrees), DELTA);
    } else {
      assertEquals(90, endeff.getRotation().getMeasureAngle().in(Degrees), DELTA);
    }
  }

  @Test
  public void TestSplitRoot() {
    // Simple arm setup, testing ForwardKinematics, unit test so ignore close
    @SuppressWarnings("resource")
    LoggedMechanism2d mech2d = new LoggedMechanism2d(0, 0); // don't care, values don't do anything

    // leave the root at the robot origin
    LoggedMechanismRoot2d mechRoot = mech2d.getRoot("root", 0, 0);
    // straight up 0.5 meters
    LoggedMechanismLigament2d ligamentBase = mechRoot.append(
      new LoggedMechanismLigament2d("base", Meters.of(0.5), Degrees.of(90))
    );
    // back forwards parallel to the ground
    LoggedMechanismLigament2d ligamentArm = ligamentBase.append(
      new LoggedMechanismLigament2d("arm", Meters.of(0.5), Degrees.of(-90))
    );
    // and again back upwards
    LoggedMechanismLigament2d ligamentGripper = ligamentArm.append(
      new LoggedMechanismLigament2d("gripper", Meters.of(0.2), Degrees.of(90))
    );
    // back forwards parallel to the ground
    LoggedMechanismLigament2d ligamentArm2 = ligamentBase.append(
      new LoggedMechanismLigament2d("arm2", Meters.of(0.5), Degrees.of(-45))
    );
    // and again back upwards
    LoggedMechanismLigament2d ligamentGripper2 = ligamentArm2.append(
      new LoggedMechanismLigament2d("gripper2", Meters.of(0.2), Degrees.of(45))
    );

    ArrayList<Pose3d> poses = mech2d.generate3dMechanism();

    assertEquals(5, poses.size());
    assertEquals(0, poses.get(0).getTranslation().getDistance(new Translation3d()), DELTA);
    assertEquals(0, poses.get(1).getTranslation().getDistance(new Translation3d(0,0,0.5)), DELTA);
    assertEquals(0, poses.get(2).getTranslation().getDistance(new Translation3d(0.5,0,0.5)), DELTA);
    assertEquals(0, poses.get(3).getTranslation().getDistance(new Translation3d(0.0,0,0.5)), DELTA);
    assertEquals(0, poses.get(4).getTranslation().getDistance(new Translation3d(0.35355339059,0,0.5+0.35355339059)), DELTA);

    // Test EndEffector (1) location
    Pose3d lastPose = poses.get(2);
    Pose3d endeff = lastPose.transformBy(
      new Transform3d(ligamentGripper.getLength(), 0, 0, Rotation3d.kZero)
    );
    assertEquals(0, endeff.getTranslation().getDistance(new Translation3d(0.5, 0, 0.7)), DELTA);
    var rot_axis = endeff.getRotation().getAxis();
    if (rot_axis.get(1) > 0) { // rotation axis is correct
      assertEquals(-90, endeff.getRotation().getMeasureAngle().in(Degrees), DELTA);
    } else {
      assertEquals(90, endeff.getRotation().getMeasureAngle().in(Degrees), DELTA);
    }
    // Test EndEffector (2) location
    lastPose = poses.get(poses.size()-1);
    endeff = lastPose.transformBy(
      new Transform3d(ligamentGripper2.getLength(), 0, 0, Rotation3d.kZero)
    );
    assertEquals(0, endeff.getTranslation().getDistance(new Translation3d(0.35355339059, 0, 0.7+0.35355339059)), DELTA);
    rot_axis = endeff.getRotation().getAxis();
    if (rot_axis.get(1) > 0) { // rotation axis is correct
      assertEquals(-90, endeff.getRotation().getMeasureAngle().in(Degrees), DELTA);
    } else {
      assertEquals(90, endeff.getRotation().getMeasureAngle().in(Degrees), DELTA);
    }
  }
}
