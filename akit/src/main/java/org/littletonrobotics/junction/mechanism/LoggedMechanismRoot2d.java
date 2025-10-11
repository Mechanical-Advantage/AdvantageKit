// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.mechanism;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Radians;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.units.measure.Distance;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.littletonrobotics.junction.LogTable;

/**
 * Root Mechanism2d node.
 *
 * <p>A root is the anchor point of other nodes (such as ligaments).
 *
 * <p>Do not create objects of this class directly! Obtain instances from the {@link
 * edu.wpi.first.wpilibj.smartdashboard.Mechanism2d#getRoot(String, double, double)} factory method.
 *
 * <p>Append other nodes by using {@link #append(LoggedMechanismObject2d)}.
 */
public final class LoggedMechanismRoot2d implements AutoCloseable {
  private final String m_name;
  private NetworkTable m_table;
  private final Map<String, LoggedMechanismObject2d> m_objects = new LinkedHashMap<>(1);
  private double m_x;
  private DoublePublisher m_xPub;
  private double m_y;
  private DoublePublisher m_yPub;

  /**
   * Package-private constructor for roots.
   *
   * @param name name
   * @param x x coordinate of root (provide only when constructing a root node)
   * @param y y coordinate of root (provide only when constructing a root node)
   */
  LoggedMechanismRoot2d(String name, double x, double y) {
    m_name = name;
    m_x = x;
    m_y = y;
  }

  LoggedMechanismRoot2d(String name, Distance x, Distance y) {
    this(name, x.in(Meters), y.in(Meters));
  }

  @Override
  public void close() {
    if (m_xPub != null) {
      m_xPub.close();
    }
    if (m_yPub != null) {
      m_yPub.close();
    }
    for (LoggedMechanismObject2d obj : m_objects.values()) {
      obj.close();
    }
  }

  /**
   * Append a Mechanism object that is based on this one.
   *
   * @param <T> The object type.
   * @param object the object to add.
   * @return the object given as a parameter, useful for variable assignments and call chaining.
   * @throws UnsupportedOperationException if the object's name is already used - object names must
   *     be unique.
   */
  public synchronized <T extends LoggedMechanismObject2d> T append(T object) {
    if (m_objects.containsKey(object.getName())) {
      throw new UnsupportedOperationException("Mechanism object names must be unique!");
    }
    m_objects.put(object.getName(), object);
    if (m_table != null) {
      object.update(m_table.getSubTable(object.getName()));
    }
    return object;
  }

  /**
   * Set the root's position.
   *
   * @param x new x coordinate
   * @param y new y coordinate
   */
  public synchronized void setPosition(double x, double y) {
    m_x = x;
    m_y = y;
    flush();
  }

  synchronized void update(NetworkTable table) {
    m_table = table;
    if (m_xPub != null) {
      m_xPub.close();
    }
    m_xPub = m_table.getDoubleTopic("x").publish();
    if (m_yPub != null) {
      m_yPub.close();
    }
    m_yPub = m_table.getDoubleTopic("y").publish();
    flush();
    for (LoggedMechanismObject2d obj : m_objects.values()) {
      obj.update(m_table.getSubTable(obj.getName()));
    }
  }

  /**
   * Get the name of the root.
   *
   * @return The name of the root.
   */
  public String getName() {
    return m_name;
  }

  private void flush() {
    if (m_xPub != null) {
      m_xPub.set(m_x);
    }
    if (m_yPub != null) {
      m_yPub.set(m_y);
    }
  }

  synchronized void logOutput(LogTable table) {
    table.put("x", m_x);
    table.put("y", m_y);
    for (LoggedMechanismObject2d obj : m_objects.values()) {
      obj.logOutput(table.getSubtable(obj.getName()));
    }
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
    Pose3d initial_pose = new Pose3d(m_x, 0, m_y, new Rotation3d());
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
              new Transform3d(obj.getValue().getObject2dRange(), 0, 0, Rotation3d.kZero));
      var more_poses = obj.getValue().generate3dMechanism(next_pose);
      poses.addAll(more_poses);
    }

    return poses;
  }
}
