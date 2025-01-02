// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package org.littletonrobotics.junction.mechanism;

import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import java.util.HashMap;
import java.util.Map;
import org.littletonrobotics.junction.LogTable;

/**
 * Root Mechanism2d node.
 *
 * <p>
 * A root is the anchor point of other nodes (such as ligaments).
 *
 * <p>
 * Do not create objects of this class directly! Obtain instances from the
 * {@link edu.wpi.first.wpilibj.smartdashboard.Mechanism2d#getRoot(String, double, double)}
 * factory method.
 *
 * <p>
 * Append other nodes by using {@link #append(MechanismObject2d)}.
 */
public final class LoggedMechanismRoot2d implements AutoCloseable {
  private final String m_name;
  private NetworkTable m_table;
  private final Map<String, LoggedMechanismObject2d> m_objects = new HashMap<>(1);
  private double m_x;
  private DoublePublisher m_xPub;
  private double m_y;
  private DoublePublisher m_yPub;

  /**
   * Package-private constructor for roots.
   *
   * @param name name
   * @param x    x coordinate of root (provide only when constructing a root node)
   * @param y    y coordinate of root (provide only when constructing a root node)
   */
  LoggedMechanismRoot2d(String name, double x, double y) {
    m_name = name;
    m_x = x;
    m_y = y;
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
   * @param <T>    The object type.
   * @param object the object to add.
   * @return the object given as a parameter, useful for variable assignments and
   *         call chaining.
   * @throws UnsupportedOperationException if the object's name is already used -
   *                                       object names must
   *                                       be unique.
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
}
