// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.mechanism;

import static org.wpilib.units.Units.Degrees;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Radians;

import java.util.ArrayList;
import java.util.Map.Entry;
import org.littletonrobotics.junction.LogTable;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.geometry.Transform3d;
import org.wpilib.math.geometry.Translation2d;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.Distance;

/**
 * Root Mechanism2d node.
 *
 * <p>A root is the anchor point of other nodes (such as ligaments).
 *
 * <p>Do not create objects of this class directly! Obtain instances from the {@link
 * LoggedMechanism2d#getRoot(String, double, double)} factory method.
 *
 * <p>Append other nodes by using {@link #append(LoggedMechanismObject2d)}.
 */
public final class LoggedMechanismRoot2d extends LoggedMechanismObject2d {
  private final double[] m_location = new double[2];

  /**
   * Package-private constructor for roots.
   *
   * @param name name
   * @param x x coordinate of root (provide only when constructing a root node)
   * @param y y coordinate of root (provide only when constructing a root node)
   */
  LoggedMechanismRoot2d(String name, double x, double y) {
    super(name);
    m_location[0] = x;
    m_location[1] = y;
  }

  LoggedMechanismRoot2d(String name, Distance x, Distance y) {
    this(name, x.in(Meters), y.in(Meters));
  }

  /**
   * Set the root's position.
   *
   * @param x new x coordinate
   * @param y new y coordinate
   */
  public synchronized void setPosition(double x, double y) {
    m_location[0] = x;
    m_location[1] = y;
  }

  /**
   * Set the root's position.
   *
   * @param x new x coordinate
   * @param y new y coordinate
   */
  public synchronized void setPosition(Distance x, Distance y) {
    setPosition(x.in(Meters), y.in(Meters));
  }

  /**
   * Set the root's position.
   *
   * @param position new position
   */
  public synchronized void setPosition(Translation2d position) {
    setPosition(position.getX(), position.getY());
  }

  /**
   * Get the root's position.
   *
   * @return double array of [x, y] coordinates
   */
  public synchronized double[] getPosition() {
    return m_location.clone();
  }

  /**
   * Get the root's x coordinate.
   *
   * @return x coordinate
   */
  public synchronized double getX() {
    return m_location[0];
  }

  /**
   * Get the root's y coordinate.
   *
   * @return y coordinate
   */
  public synchronized double getY() {
    return m_location[1];
  }

  @Override
  public void logTo(TelemetryTable table) {
    synchronized (this) {
      table.log("position", m_location);
      super.logTo(table);
    }
  }

  @Override
  synchronized void logOutput(LogTable table) {
    table.put("position", m_location);
    super.logOutput(table);
  }

  @Override
  public double getObject2dRange() {
    return 0.0;
  }

  @Override
  public double getAngle() {
    return 0.0;
  }

  /**
   * Converts the Mechanism2d into a series of Pose3d objects. Poses are generated with standard
   * coordinate frame (+x forward, +y left, +z up) and each pivot point is assumed to be at the
   * origin of the model.
   *
   * <p>The order of the poses returned is based on the order of insertion. The first root inserted
   * into the Mechanism2d goes first, and processed in a depth-first manner.
   *
   * @return list of poses for starting from the root point
   */
  public synchronized ArrayList<Pose3d> generate3dMechanism() {
    ArrayList<Pose3d> poses = new ArrayList<>();

    // Coordinate shift changes from the xz plane to the xyz plane which is 'y' is 0
    Pose3d initial_pose = new Pose3d(m_location[0], 0, m_location[1], new Rotation3d());
    for (Entry<String, LoggedMechanismObject2d> obj : m_objects.entrySet()) {
      // convert mech2d angle to Rotation3d
      // remembering that +rotation in 2d is -pitch in 3d
      var new_rotation = new Rotation3d(0, Degrees.of(-obj.getValue().getAngle()).in(Radians), 0);

      // Generate the pose for the next segment
      var new_pose = new Pose3d(initial_pose.getTranslation(), new_rotation);
      poses.add(new_pose);

      // recurse down the length of that ligament
      var next_pose =
          new_pose.transformBy(
              new Transform3d(obj.getValue().getObject2dRange(), 0, 0, Rotation3d.ZERO));
      var more_poses = obj.getValue().generate3dMechanism(next_pose);
      poses.addAll(more_poses);
    }

    return poses;
  }
}
