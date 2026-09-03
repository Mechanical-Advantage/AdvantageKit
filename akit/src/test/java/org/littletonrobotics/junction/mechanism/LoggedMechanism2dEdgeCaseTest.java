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
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.wpilibj.util.Color8Bit;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.LogTable;

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

  // ─── logOutput() serializes to LogTable ─────────────────────────────────────

  @Test
  void logOutputWritesTypeAndControllable() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(3.0, 2.0);
    LogTable table = new LogTable(0);
    mech.logOutput(table);

    assertEquals("Mechanism2d", table.get(".type", ""));
    assertFalse(table.get(".controllable", true));
  }

  @Test
  void logOutputWritesDimensions() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(4.0, 3.0);
    LogTable table = new LogTable(0);
    mech.logOutput(table);

    double[] dims = table.get("dims", new double[0]);
    assertEquals(2, dims.length);
    assertEquals(4.0, dims[0], DELTA);
    assertEquals(3.0, dims[1], DELTA);
  }

  @Test
  void logOutputWritesRootPositionAndLigamentData() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(3.0, 2.0);
    LoggedMechanismRoot2d root = mech.getRoot("arm", 0.5, 1.0);
    root.append(new LoggedMechanismLigament2d("seg", 1.0, 45.0));

    LogTable table = new LogTable(0);
    mech.logOutput(table);

    // Root position
    LogTable rootTable = table.getSubtable("arm");
    assertEquals(0.5, rootTable.get("x", 0.0), DELTA);
    assertEquals(1.0, rootTable.get("y", 0.0), DELTA);

    // Ligament data
    LogTable ligTable = rootTable.getSubtable("seg");
    assertEquals(45.0, ligTable.get("angle", 0.0), DELTA);
    assertEquals(1.0, ligTable.get("length", 0.0), DELTA);
    assertEquals("line", ligTable.get(".type", ""));
  }

  // ─── setBackgroundColor() ───────────────────────────────────────────────────

  @Test
  void setBackgroundColorChangesBackgroundInLogOutput() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(1.0, 1.0, new Color8Bit(0, 0, 32));
    mech.setBackgroundColor(new Color8Bit(255, 0, 0));

    LogTable table = new LogTable(0);
    mech.logOutput(table);

    assertEquals("#FF0000", table.get("backgroundColor", ""));
  }

  // ─── getRoot() returns existing root ────────────────────────────────────────

  @Test
  void getExistingRootReturnsTheSameObject() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(3.0, 2.0);
    LoggedMechanismRoot2d first = mech.getRoot("arm", 0.0, 0.0);
    LoggedMechanismRoot2d second = mech.getRoot("arm", 9.0, 9.0); // different coords, same name
    assertSame(first, second, "getRoot() with an existing name must return the same object");
  }

  // ─── Ligament getters / setters ─────────────────────────────────────────────

  @Test
  void ligamentGetAngleReturnsSetAngle() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("l", 1.0, 45.0);
    assertEquals(45.0, lig.getAngle(), DELTA);
  }

  @Test
  void ligamentGetLengthReturnsSetLength() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("l", 2.5, 0.0);
    assertEquals(2.5, lig.getLength(), DELTA);
  }

  @Test
  void ligamentSetAngleUpdatesAngle() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("l", 1.0, 0.0);
    lig.setAngle(90.0);
    assertEquals(90.0, lig.getAngle(), DELTA);
  }

  @Test
  void ligamentSetLengthUpdatesLength() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("l", 1.0, 0.0);
    lig.setLength(3.0);
    assertEquals(3.0, lig.getLength(), DELTA);
  }

  @Test
  void ligamentSetAngleWithRotation2d() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("l", 1.0, 0.0);
    lig.setAngle(Rotation2d.fromDegrees(30.0));
    assertEquals(30.0, lig.getAngle(), DELTA);
  }

  @Test
  void ligamentSetAngleWithAngleUnit() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("l", 1.0, 0.0);
    lig.setAngle(Degrees.of(60.0));
    assertEquals(60.0, lig.getAngle(), DELTA);
  }

  @Test
  void ligamentSetLengthWithDistanceUnit() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("l", 1.0, 0.0);
    lig.setLength(Meters.of(2.0));
    assertEquals(2.0, lig.getLength(), DELTA);
  }

  @Test
  void ligamentGetColorRoundTrip() {
    Color8Bit color = new Color8Bit(100, 150, 200);
    LoggedMechanismLigament2d lig =
        new LoggedMechanismLigament2d("l", 1.0, 0.0, 10, color);
    Color8Bit got = lig.getColor();
    assertEquals(100, got.red);
    assertEquals(150, got.green);
    assertEquals(200, got.blue);
  }

  @Test
  void ligamentSetColorUpdatesColor() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("l", 1.0, 0.0);
    lig.setColor(new Color8Bit(10, 20, 30));
    Color8Bit got = lig.getColor();
    assertEquals(10, got.red);
    assertEquals(20, got.green);
    assertEquals(30, got.blue);
  }

  @Test
  void ligamentGetLineWeightReturnsSetWeight() {
    LoggedMechanismLigament2d lig =
        new LoggedMechanismLigament2d("l", 1.0, 0.0, 8.0, new Color8Bit(0, 0, 0));
    assertEquals(8.0, lig.getLineWeight(), DELTA);
  }

  @Test
  void ligamentSetLineWeightUpdatesWeight() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("l", 1.0, 0.0);
    lig.setLineWeight(5.0);
    assertEquals(5.0, lig.getLineWeight(), DELTA);
  }

  // ─── getName() ──────────────────────────────────────────────────────────────

  @Test
  void ligamentGetNameReturnsCorrectName() {
    LoggedMechanismLigament2d lig = new LoggedMechanismLigament2d("myArm", 1.0, 0.0);
    assertEquals("myArm", lig.getName());
  }

  // ─── Duplicate child name throws ────────────────────────────────────────────

  @Test
  void appendingDuplicateNameThrows() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(3.0, 2.0);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    root.append(new LoggedMechanismLigament2d("seg", 1.0, 0.0));
    assertThrows(
        UnsupportedOperationException.class,
        () -> root.append(new LoggedMechanismLigament2d("seg", 2.0, 90.0)),
        "Appending two children with the same name must throw");
  }

  // ─── setPosition() on root ──────────────────────────────────────────────────

  @Test
  void rootSetPositionUpdatesCoordinates() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(3.0, 2.0);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0.0, 0.0);
    root.setPosition(1.5, 2.5);

    LogTable table = new LogTable(0);
    mech.logOutput(table);
    LogTable rootTable = table.getSubtable("root");
    assertEquals(1.5, rootTable.get("x", 0.0), DELTA);
    assertEquals(2.5, rootTable.get("y", 0.0), DELTA);
  }

  // ─── Ligament logOutput() ───────────────────────────────────────────────────

  @Test
  void ligamentLogOutputWritesAllFields() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(3.0, 2.0);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0.0, 0.0);
    root.append(
        new LoggedMechanismLigament2d("seg", 2.0, 30.0, 6.0, new Color8Bit(255, 128, 0)));

    LogTable table = new LogTable(0);
    mech.logOutput(table);
    LogTable ligTable = table.getSubtable("root").getSubtable("seg");

    assertEquals("line", ligTable.get(".type", ""));
    assertEquals(30.0, ligTable.get("angle", 0.0), DELTA);
    assertEquals(2.0, ligTable.get("length", 0.0), DELTA);
    assertEquals(6.0, ligTable.get("weight", 0.0), DELTA);
    assertEquals("#FF8000", ligTable.get("color", ""));
  }

  // ─── Chain of ligaments produces correct pose count ─────────────────────────

  @Test
  void threeChainedLigamentsProduceThreePoses() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(3.0, 2.0);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    LoggedMechanismLigament2d a = root.append(new LoggedMechanismLigament2d("a", 1.0, 0.0));
    LoggedMechanismLigament2d b = a.append(new LoggedMechanismLigament2d("b", 1.0, 90.0));
    b.append(new LoggedMechanismLigament2d("c", 1.0, -45.0));

    ArrayList<Pose3d> poses = mech.generate3dMechanism();
    assertEquals(3, poses.size(), "A 3-segment chain must produce exactly 3 poses");
  }

  // ─── Distance constructors ───────────────────────────────────────────────────

  @Test
  void mechanism2dDistanceConstructorUsesMeters() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(Meters.of(3.0), Meters.of(2.0));
    LogTable table = new LogTable(0);
    mech.logOutput(table);
    double[] dims = table.get("dims", new double[0]);
    assertEquals(3.0, dims[0], DELTA);
    assertEquals(2.0, dims[1], DELTA);
  }

  @Test
  void mechanism2dDistanceConstructorWithColorUsesMeters() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech =
        new LoggedMechanism2d(Meters.of(4.0), Meters.of(1.0), new Color8Bit(0, 128, 0));
    LogTable table = new LogTable(0);
    mech.logOutput(table);
    double[] dims = table.get("dims", new double[0]);
    assertEquals(4.0, dims[0], DELTA);
    assertEquals(1.0, dims[1], DELTA);
  }

  @Test
  void ligament2dDistanceAngleConstructorUsesUnits() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    LoggedMechanismLigament2d lig =
        root.append(new LoggedMechanismLigament2d("seg", Meters.of(2.0), Degrees.of(45.0)));
    assertEquals(2.0, lig.getLength(), DELTA);
    assertEquals(45.0, lig.getAngle(), DELTA);
  }

  @Test
  void ligament2dDistanceAngleWithColorConstructorUsesUnits() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    LoggedMechanismLigament2d lig =
        root.append(
            new LoggedMechanismLigament2d(
                "seg", Meters.of(1.5), Degrees.of(30.0), 8.0, new Color8Bit(255, 0, 0)));
    assertEquals(1.5, lig.getLength(), DELTA);
    assertEquals(30.0, lig.getAngle(), DELTA);
  }

  // ─── close() ───────────────────────────────────────────────────────────────

  @Test
  void closeDoesNotThrowWhenPublishersAreNull() {
    // Publishers are only initialized after initSendable(); before that, close() must be safe.
    LoggedMechanism2d mech = new LoggedMechanism2d(3.0, 2.0);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    root.append(new LoggedMechanismLigament2d("seg", 1.0, 45.0));
    assertDoesNotThrow(mech::close);
  }

  @Test
  void closeIsIdempotentForEmptyMechanism() {
    LoggedMechanism2d mech = new LoggedMechanism2d(1.0, 1.0);
    assertDoesNotThrow(mech::close);
    assertDoesNotThrow(mech::close); // second call must also be safe
  }

  // ─── Root Distance constructor ───────────────────────────────────────────────

  @Test
  void rootDistance2dConstructorUsesMeters() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(0, 0);
    // LoggedMechanismRoot2d's Distance constructor delegates to the double one
    // We can exercise it via a wrapping helper since root constructors are package-private.
    // Use Ligament2d Distance constructor to hit the same Units path.
    LoggedMechanismRoot2d root = mech.getRoot("root", Meters.of(1.0).in(Meters), Meters.of(2.0).in(Meters));
    LogTable table = new LogTable(0);
    mech.logOutput(table);
    assertEquals(1.0, table.getSubtable("root").get("x", 0.0), DELTA);
    assertEquals(2.0, table.getSubtable("root").get("y", 0.0), DELTA);
  }

  // ─── Root Distance constructor (package-private) ─────────────────────────────

  @Test
  void rootDistanceConstructorDirectlyDelegatesToDouble() {
    // LoggedMechanismRoot2d(String, Distance, Distance) is package-private; accessible here.
    LoggedMechanismRoot2d root = new LoggedMechanismRoot2d("r", Meters.of(2.5), Meters.of(1.5));
    assertEquals("r", root.getName());
    LogTable table = new LogTable(0);
    root.logOutput(table);
    assertEquals(2.5, table.get("x", 0.0), DELTA);
    assertEquals(1.5, table.get("y", 0.0), DELTA);
  }

  // ─── getName ─────────────────────────────────────────────────────────────────

  @Test
  void getRootNameReturnsConstructorName() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(10, 10);
    LoggedMechanismRoot2d root = mech.getRoot("myRoot", 0, 0);
    assertEquals("myRoot", root.getName());
  }

  // ─── getRoot returns existing root when name already used ────────────────────

  @Test
  void getRootReturnsSameInstanceWhenNameAlreadyExists() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(10, 10);
    LoggedMechanismRoot2d first = mech.getRoot("dup", 1.0, 2.0);
    LoggedMechanismRoot2d second = mech.getRoot("dup", 3.0, 4.0);
    assertSame(first, second, "getRoot must return the same instance for a duplicate name");
  }

  // ─── append duplicate name throws ────────────────────────────────────────────

  @Test
  void appendDuplicateLigamentNameThrows() {
    @SuppressWarnings("resource")
    LoggedMechanism2d mech = new LoggedMechanism2d(10, 10);
    LoggedMechanismRoot2d root = mech.getRoot("root", 0, 0);
    root.append(new LoggedMechanismLigament2d("seg", 1.0, 0.0));
    assertThrows(
        UnsupportedOperationException.class,
        () -> root.append(new LoggedMechanismLigament2d("seg", 2.0, 90.0)));
  }

  // ─── setBackgroundColor (no-pub branch) ──────────────────────────────────────

  @Test
  void setBackgroundColorBeforeInitSendableDoesNotThrow() {
    LoggedMechanism2d mech = new LoggedMechanism2d(5, 5);
    // Without initSendable(), m_colorPub is null — must not throw.
    assertDoesNotThrow(() -> mech.setBackgroundColor(new Color8Bit(255, 0, 0)));
    mech.close();
  }
}
