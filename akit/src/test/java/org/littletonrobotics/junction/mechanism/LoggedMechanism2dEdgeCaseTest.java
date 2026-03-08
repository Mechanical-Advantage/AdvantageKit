// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.mechanism;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Meters;
import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/**
 * Edge-case tests for LoggedMechanism2d forward kinematics: non-zero root offsets, zero-length
 * ligaments, horizontal orientation, and negative / large angles.
 */
public class LoggedMechanism2dEdgeCaseTest {

  private static final double DELTA = 0.001;

  // ─── Non-zero root offset ───────────────────────────────────────────────────

  @Test
  void rootOffsetIsReflectedInJointPose() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);

    // Root placed at (1.0, 2.0) in 2D — maps to (X=1.0, Y=0, Z=2.0) in 3D
    LoggedMechanismRoot2d root = mech.getRoot("root", 1.0, 2.0);
    root.append(new LoggedMechanismLigament2d("seg", Meters.of(0.5), Degrees.of(90)));

    ArrayList<Pose3d> poses = mech.generate3dMechanism();
    assertEquals(1, poses.size());

    // The joint pose (base of the ligament) must be at the root offset
    Translation3d jointOrigin = poses.get(0).getTranslation();
    assertEquals(1.0, jointOrigin.getX(), DELTA, "Root X offset must be preserved");
    assertEquals(2.0, jointOrigin.getZ(), DELTA, "Root Z (Y2D) offset must be preserved");
  }

  @Test
  void rootOffsetAndSegmentCombineCorrectly() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);

    LoggedMechanismRoot2d root = mech.getRoot("root", 0.5, 0.0);
    LoggedMechanismLigament2d seg =
        root.append(new LoggedMechanismLigament2d("seg", Meters.of(1.0), Degrees.of(0)));

    ArrayList<Pose3d> poses = mech.generate3dMechanism();
    assertEquals(1, poses.size());

    // End effector = joint pose (X=0.5) + 1.0 along horizontal (X direction)
    Pose3d joint = poses.get(0);
    Translation3d endEffector =
        joint.getTranslation().plus(new Translation3d(seg.getLength(), 0, 0));
    assertEquals(1.5, endEffector.getX(), DELTA);
    assertEquals(0.0, endEffector.getZ(), DELTA);
  }

  // ─── Zero-length ligament ───────────────────────────────────────────────────

  @Test
  void zeroLengthLigamentProducesJointAtSameLocation() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);

    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    root.append(new LoggedMechanismLigament2d("zero", Meters.of(0.0), Degrees.of(45)));

    ArrayList<Pose3d> poses = mech.generate3dMechanism();
    assertEquals(1, poses.size());

    // A zero-length segment has its joint at the root — tip is also at origin
    assertEquals(
        0,
        poses.get(0).getTranslation().getDistance(new Translation3d()),
        DELTA,
        "Zero-length ligament joint must be at root origin");
  }

  @Test
  void zeroLengthLigamentFollowedByNonZeroLigament() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);

    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    LoggedMechanismLigament2d zero =
        root.append(new LoggedMechanismLigament2d("zero", Meters.of(0.0), Degrees.of(0)));
    zero.append(new LoggedMechanismLigament2d("arm", Meters.of(1.0), Degrees.of(90)));

    ArrayList<Pose3d> poses = mech.generate3dMechanism();
    assertEquals(2, poses.size());

    // Second joint: zero-length base contributes no translation, arm points straight up
    assertEquals(
        0,
        poses.get(1).getTranslation().getDistance(new Translation3d(0, 0, 0)),
        DELTA,
        "Second joint must still be at origin because first segment is zero-length");
  }

  // ─── Horizontal ligament (0°) ───────────────────────────────────────────────

  @Test
  void horizontalLigamentEndEffectorIsAlongX() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);

    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    LoggedMechanismLigament2d seg =
        root.append(new LoggedMechanismLigament2d("horiz", Meters.of(1.0), Degrees.of(0)));

    ArrayList<Pose3d> poses = mech.generate3dMechanism();
    assertEquals(1, poses.size());

    Pose3d joint = poses.get(0);
    Translation3d tip = joint.getTranslation().plus(new Translation3d(seg.getLength(), 0, 0));
    assertEquals(1.0, tip.getX(), DELTA, "Horizontal end-effector X must equal segment length");
    assertEquals(0.0, tip.getZ(), DELTA, "Horizontal end-effector Z must be zero");
  }

  // ─── Negative angles ────────────────────────────────────────────────────────

  @Test
  void negativeAngleLigamentTipIsBelowRoot() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);

    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 1.0); // 1 m above ground
    LoggedMechanismLigament2d seg =
        root.append(new LoggedMechanismLigament2d("down", Meters.of(1.0), Degrees.of(-90)));

    ArrayList<Pose3d> poses = mech.generate3dMechanism();
    assertEquals(1, poses.size());

    // transformBy applies the joint's rotation before translating along local X
    Pose3d endeff =
        poses.get(0).transformBy(new Transform3d(seg.getLength(), 0, 0, Rotation3d.kZero));
    // Pointing straight down from Z=1.0 → tip at Z=0.0
    assertEquals(0.0, endeff.getTranslation().getZ(), DELTA,
        "End-effector of -90° ligament should be at Z=0");
  }

  // ─── 360° / large angles ─────────────────────────────────────────────────────

  @Test
  void threeSixtyDegreesEquivalentToZeroDegrees() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech360 = new LoggedMechanism2d(0, 0);
    LoggedMechanismRoot2d root360 = mech360.getRoot("root", 0, 0);
    LoggedMechanismLigament2d seg360 =
        root360.append(new LoggedMechanismLigament2d("seg", Meters.of(1.0), Degrees.of(360)));

    @SuppressWarnings("resource")
    LoggedMechanism2d mech0 = new LoggedMechanism2d(0, 0);
    LoggedMechanismRoot2d root0 = mech0.getRoot("root", 0, 0);
    LoggedMechanismLigament2d seg0 =
        root0.append(new LoggedMechanismLigament2d("seg", Meters.of(1.0), Degrees.of(0)));

    Pose3d joint360 = mech360.generate3dMechanism().get(0);
    Pose3d joint0 = mech0.generate3dMechanism().get(0);

    Translation3d tip360 =
        joint360.getTranslation().plus(new Translation3d(seg360.getLength(), 0, 0));
    Translation3d tip0 =
        joint0.getTranslation().plus(new Translation3d(seg0.getLength(), 0, 0));

    assertEquals(
        0, tip360.getDistance(tip0), DELTA, "360° must produce the same end-effector as 0°");
  }

  @Test
  void largePositiveAngleIsHandledCorrectly() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    LoggedMechanismLigament2d seg =
        root.append(new LoggedMechanismLigament2d("seg", Meters.of(1.0), Degrees.of(450)));

    // 450° ≡ 90° — should point straight up; transformBy applies the rotation correctly
    ArrayList<Pose3d> poses = mech.generate3dMechanism();
    Pose3d endeff =
        poses.get(0).transformBy(new Transform3d(seg.getLength(), 0, 0, Rotation3d.kZero));

    assertEquals(0.0, endeff.getTranslation().getX(), DELTA, "450° end-effector X should be ~0 (same as 90°)");
    assertEquals(1.0, endeff.getTranslation().getZ(), DELTA, "450° end-effector Z should be 1.0 (same as 90°)");
  }

  // ─── Multiple independent roots ─────────────────────────────────────────────

  @Test
  void twoIndependentRootsProduceCorrectPoseCount() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);

    LoggedMechanismRoot2d rootA = mech.getRoot("a", 0, 0);
    rootA.append(new LoggedMechanismLigament2d("segA", Meters.of(0.5), Degrees.of(90)));

    LoggedMechanismRoot2d rootB = mech.getRoot("b", 1.0, 0);
    rootB.append(new LoggedMechanismLigament2d("segB", Meters.of(0.5), Degrees.of(0)));

    ArrayList<Pose3d> poses = mech.generate3dMechanism();
    assertEquals(2, poses.size(), "Two roots each with one segment must produce two poses");
  }

  @Test
  void twoRootsHaveIndependentPositions() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);

    LoggedMechanismRoot2d rootA = mech.getRoot("a", 0.0, 0.0);
    LoggedMechanismLigament2d segA =
        rootA.append(new LoggedMechanismLigament2d("segA", Meters.of(1.0), Degrees.of(90)));

    LoggedMechanismRoot2d rootB = mech.getRoot("b", 2.0, 0.0);
    LoggedMechanismLigament2d segB =
        rootB.append(new LoggedMechanismLigament2d("segB", Meters.of(1.0), Degrees.of(90)));

    ArrayList<Pose3d> poses = mech.generate3dMechanism();

    // Use transformBy so the joint's rotation is applied before adding the segment length
    Pose3d endA =
        poses.get(0).transformBy(new Transform3d(segA.getLength(), 0, 0, Rotation3d.kZero));
    Pose3d endB =
        poses.get(1).transformBy(new Transform3d(segB.getLength(), 0, 0, Rotation3d.kZero));

    // segA tip: X≈0, Z≈1 (straight up from origin)
    assertEquals(0.0, endA.getTranslation().getX(), DELTA);
    assertEquals(1.0, endA.getTranslation().getZ(), DELTA);
    // segB tip: X≈2, Z≈1 (straight up from X=2 offset)
    assertEquals(2.0, endB.getTranslation().getX(), DELTA);
    assertEquals(1.0, endB.getTranslation().getZ(), DELTA);
  }

  // ─── Dynamic angle change ────────────────────────────────────────────────────

  @Test
  void changingLigamentAngleDynamicallyUpdatesKinematics() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    LoggedMechanismLigament2d seg =
        root.append(new LoggedMechanismLigament2d("seg", Meters.of(1.0), Degrees.of(0)));

    // Initial: horizontal (0°) → end-effector at (1, 0, 0)
    ArrayList<Pose3d> before = mech.generate3dMechanism();
    Pose3d endBefore =
        before.get(0).transformBy(new Transform3d(seg.getLength(), 0, 0, Rotation3d.kZero));
    assertEquals(1.0, endBefore.getTranslation().getX(), DELTA);

    // Rotate to 90° → end-effector should move to (0, 0, 1)
    seg.setAngle(Degrees.of(90));
    ArrayList<Pose3d> after = mech.generate3dMechanism();
    Pose3d endAfter =
        after.get(0).transformBy(new Transform3d(seg.getLength(), 0, 0, Rotation3d.kZero));
    assertEquals(0.0, endAfter.getTranslation().getX(), DELTA);
    assertEquals(1.0, endAfter.getTranslation().getZ(), DELTA);
  }
}
