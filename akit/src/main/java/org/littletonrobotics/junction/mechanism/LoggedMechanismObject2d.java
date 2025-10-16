// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.mechanism;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Radians;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.networktables.NetworkTable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.littletonrobotics.junction.LogTable;

/**
 * Common base class for all Mechanism2d node types.
 *
 * <p>To append another node, call {@link #append(LoggedMechanismObject2d)}. Objects that aren't
 * appended to a published {@link edu.wpi.first.wpilibj.smartdashboard.Mechanism2d} container are
 * nonfunctional.
 *
 * @see org.littletonrobotics.junction.mechanism.LoggedMechanism2d
 */
public abstract class LoggedMechanismObject2d implements AutoCloseable {
  /** Relative to parent. */
  private final String m_name;

  private NetworkTable m_table;
  private final Map<String, LoggedMechanismObject2d> m_objects = new LinkedHashMap<>(1);

  /**
   * Create a new Mechanism node object.
   *
   * @param name the node's name, must be unique.
   */
  protected LoggedMechanismObject2d(String name) {
    m_name = name;
  }

  @Override
  public void close() {
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
  public final synchronized <T extends LoggedMechanismObject2d> T append(T object) {
    if (m_objects.containsKey(object.getName())) {
      throw new UnsupportedOperationException("Mechanism object names must be unique!");
    }
    m_objects.put(object.getName(), object);
    if (m_table != null) {
      object.update(m_table.getSubTable(object.getName()));
    }
    return object;
  }

  final synchronized void update(NetworkTable table) {
    m_table = table;
    updateEntries(m_table);
    for (LoggedMechanismObject2d obj : m_objects.values()) {
      obj.update(m_table.getSubTable(obj.m_name));
    }
  }

  /**
   * Update all entries with new ones from a new table.
   *
   * @param table the new table.
   */
  protected abstract void updateEntries(NetworkTable table);

  /**
   * Get the name of the object.
   *
   * @return The name of the object.
   */
  public final String getName() {
    return m_name;
  }

  synchronized void logOutput(LogTable table) {
    for (LoggedMechanismObject2d obj : m_objects.values()) {
      obj.logOutput(table.getSubtable(obj.m_name));
    }
  }

  /**
   * Propogates the mechanism2d down the tree structure.
   *
   * @param seed position to start the calculations at
   * @return array list of all poses generated from this point in a depth-first pattern
   */
  public ArrayList<Pose3d> generate3dMechanism(Pose3d seed) {
    ArrayList<Pose3d> poses = new ArrayList<>();

    Pose3d initial_pose = seed;
    for (Entry<String, LoggedMechanismObject2d> obj : m_objects.entrySet()) {
      // convert mech2d angle to Rotation3d
      // remembering that +rotation in 2d is -pitch in 3d
      var new_rotation = new Rotation3d(0, Degrees.of(-obj.getValue().getAngle()).in(Radians), 0);
      new_rotation = initial_pose.getRotation().plus(new_rotation);

      // Generate the pose for the new joint
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

  /**
   * Abstract helper function. A proxy for getLength() with Ligament2d, but would be something else
   * like getRadius() for circular parts if they were to be implemented.
   *
   * @return distance in meters
   */
  public abstract double getObject2dRange();

  /**
   * Abstract helper function. Should be common to all 2d parts, and assumes a normal xy or xz
   * positive direction of left or up, respectively.
   *
   * @return angle in degrees
   */
  public abstract double getAngle();
}
