// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.mechanism;

import static org.wpilib.units.Units.Meters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.littletonrobotics.junction.LogTable;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.telemetry.TelemetryLoggable;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.Distance;
import org.wpilib.util.Color8Bit;

/**
 * Visual 2D representation of arms, elevators, and general mechanisms through a node-based API.
 *
 * <p>A Mechanism2d object contains at least one root node. A root is the anchor point of other
 * nodes (such as ligaments). Other nodes are recursively appended based on other nodes.
 *
 * @see org.littletonrobotics.junction.mechanism.LoggedMechanismObject2d
 * @see org.littletonrobotics.junction.mechanism.LoggedMechanismLigament2d
 * @see org.littletonrobotics.junction.mechanism.LoggedMechanismRoot2d
 */
public final class LoggedMechanism2d implements TelemetryLoggable {
  private final Map<String, LoggedMechanismRoot2d> m_roots;
  private final double[] m_dims = new double[2];
  private String m_color;

  /**
   * Create a new Mechanism2d with the given dimensions and default color (dark blue).
   *
   * <p>The dimensions represent the canvas that all the nodes are drawn on.
   *
   * @param width the width in meters
   * @param height the height in meters
   */
  public LoggedMechanism2d(double width, double height) {
    this(width, height, new Color8Bit(0, 0, 32));
  }

  /**
   * Create a new Mechanism2d with the given dimensions and default color (dark blue).
   *
   * <p>The dimensions represent the canvas that all the nodes are drawn on.
   *
   * @param width the width
   * @param height the height
   */
  public LoggedMechanism2d(Distance width, Distance height) {
    this(width.in(Meters), height.in(Meters));
  }

  /**
   * Create a new Mechanism2d with the given dimensions.
   *
   * <p>The dimensions represent the canvas that all the nodes are drawn on.
   *
   * @param width the width in meters
   * @param height the height in meters
   * @param backgroundColor the background color. Defaults to dark blue.
   */
  public LoggedMechanism2d(double width, double height, Color8Bit backgroundColor) {
    m_roots = new LinkedHashMap<>();
    m_dims[0] = width;
    m_dims[1] = height;
    setBackgroundColor(backgroundColor);
  }

  /**
   * Create a new Mechanism2d with the given dimensions.
   *
   * <p>The dimensions represent the canvas that all the nodes are drawn on.
   *
   * @param width the width
   * @param height the height
   * @param backgroundColor the background color. Defaults to dark blue.
   */
  public LoggedMechanism2d(Distance width, Distance height, Color8Bit backgroundColor) {
    this(width.in(Meters), height.in(Meters), backgroundColor);
  }

  /**
   * Get or create a root in this Mechanism2d with the given name and position.
   *
   * <p>If a root with the given name already exists, the given x and y coordinates are not used.
   *
   * @param name the root name
   * @param x the root x coordinate
   * @param y the root y coordinate
   * @return a new root joint object, or the existing one with the given name.
   */
  public synchronized LoggedMechanismRoot2d getRoot(String name, double x, double y) {
    var existing = m_roots.get(name);
    if (existing != null) {
      return existing;
    }

    var root = new LoggedMechanismRoot2d(name, x, y);
    m_roots.put(name, root);
    return root;
  }

  /**
   * Get or create a root in this Mechanism2d with the given name and position.
   *
   * <p>If a root with the given name already exists, the given x and y coordinates are not used.
   *
   * @param name the root name
   * @param x the root x coordinate
   * @param y the root y coordinate
   * @return a new root joint object, or the existing one with the given name.
   */
  public synchronized LoggedMechanismRoot2d getRoot(String name, Distance x, Distance y) {
    return getRoot(name, x.in(Meters), y.in(Meters));
  }

  /**
   * Set the Mechanism2d background color.
   *
   * @param color the new color
   */
  public synchronized void setBackgroundColor(Color8Bit color) {
    m_color = color.toHexString();
  }

  @Override
  public void logTo(TelemetryTable table) {
    synchronized (this) {
      table.log("dims", m_dims);
      table.log("backgroundColor", m_color);
      for (Entry<String, LoggedMechanismRoot2d> entry : m_roots.entrySet()) {
        table.log(entry.getKey(), entry.getValue());
      }
    }
  }

  @Override
  public String getTelemetryType() {
    return "Mechanism2d";
  }

  /**
   * Record the current value to the log. <b>This function should never be called by user code.</b>
   *
   * @param table The table to which data should be written.
   */
  public synchronized void logOutput(LogTable table) {
    table.put(".type", "Mechanism2d");
    table.put("dims", m_dims);
    table.put("backgroundColor", m_color);
    for (Entry<String, LoggedMechanismRoot2d> entry : m_roots.entrySet()) {
      entry.getValue().logOutput(table.getSubtable(entry.getKey()));
    }
  }

  /**
   * Converts a forward facing Mechanism2d into a series of Pose3d objects. Poses are generated with
   * standard coordinate frame (+x forward, +y left, +z up) and each pivot point is assumed to be at
   * the origin of the model.
   *
   * <p>The order of the poses returned is based on the order of insertion. The first root inserted
   * into the Mechanism2d goes first, and processed in a depth-first manner.
   *
   * @return Pose3d[] representing each mechanism component
   */
  public synchronized ArrayList<Pose3d> generate3dMechanism() {
    ArrayList<Pose3d> poses = new ArrayList<>();
    for (Entry<String, LoggedMechanismRoot2d> root : m_roots.entrySet()) {
      poses.addAll(root.getValue().generate3dMechanism());
    }
    return poses;
  }
}
