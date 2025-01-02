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

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.networktables.DoubleEntry;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.StringEntry;
import edu.wpi.first.networktables.StringPublisher;
import edu.wpi.first.wpilibj.util.Color8Bit;
import org.littletonrobotics.junction.LogTable;

/**
 * Ligament node on a Mechanism2d. A ligament can have its length changed (like
 * an elevator) or angle changed, like an arm.
 *
 * @see edu.wpi.first.wpilibj.smartdashboard.Mechanism2d
 */
public class LoggedMechanismLigament2d extends LoggedMechanismObject2d {
  private StringPublisher m_typePub;
  private double m_angle;
  private DoubleEntry m_angleEntry;
  private String m_color;
  private StringEntry m_colorEntry;
  private double m_length;
  private DoubleEntry m_lengthEntry;
  private double m_weight;
  private DoubleEntry m_weightEntry;

  /**
   * Create a new ligament.
   *
   * @param name      The ligament name.
   * @param length    The ligament length.
   * @param angle     The ligament angle in degrees.
   * @param lineWidth The ligament's line width.
   * @param color     The ligament's color.
   */
  public LoggedMechanismLigament2d(
      String name, double length, double angle, double lineWidth, Color8Bit color) {
    super(name);
    setColor(color);
    setLength(length);
    setAngle(angle);
    setLineWeight(lineWidth);
  }

  /**
   * Create a new ligament with the default color (orange) and thickness (6).
   *
   * @param name   The ligament's name.
   * @param length The ligament's length.
   * @param angle  The ligament's angle relative to its parent in degrees.
   */
  public LoggedMechanismLigament2d(String name, double length, double angle) {
    this(name, length, angle, 10, new Color8Bit(235, 137, 52));
  }

  @Override
  public void close() {
    super.close();
    if (m_typePub != null) {
      m_typePub.close();
    }
    if (m_angleEntry != null) {
      m_angleEntry.close();
    }
    if (m_colorEntry != null) {
      m_colorEntry.close();
    }
    if (m_lengthEntry != null) {
      m_lengthEntry.close();
    }
    if (m_weightEntry != null) {
      m_weightEntry.close();
    }
  }

  /**
   * Set the ligament's angle relative to its parent.
   *
   * @param degrees the angle in degrees
   */
  public synchronized void setAngle(double degrees) {
    m_angle = degrees;
    if (m_angleEntry != null) {
      m_angleEntry.set(degrees);
    }
  }

  /**
   * Set the ligament's angle relative to its parent.
   *
   * @param angle the angle
   */
  public synchronized void setAngle(Rotation2d angle) {
    setAngle(angle.getDegrees());
  }

  /**
   * Get the ligament's angle relative to its parent.
   *
   * @return the angle in degrees
   */
  public synchronized double getAngle() {
    if (m_angleEntry != null) {
      m_angle = m_angleEntry.get();
    }
    return m_angle;
  }

  /**
   * Set the ligament's length.
   *
   * @param length the line length
   */
  public synchronized void setLength(double length) {
    m_length = length;
    if (m_lengthEntry != null) {
      m_lengthEntry.set(length);
    }
  }

  /**
   * Get the ligament length.
   *
   * @return the line length
   */
  public synchronized double getLength() {
    if (m_lengthEntry != null) {
      m_length = m_lengthEntry.get();
    }
    return m_length;
  }

  /**
   * Set the ligament color.
   *
   * @param color the color of the line
   */
  public synchronized void setColor(Color8Bit color) {
    m_color = String.format("#%02X%02X%02X", color.red, color.green, color.blue);
    if (m_colorEntry != null) {
      m_colorEntry.set(m_color);
    }
  }

  /**
   * Get the ligament color.
   *
   * @return the color of the line
   */
  public synchronized Color8Bit getColor() {
    if (m_colorEntry != null) {
      m_color = m_colorEntry.get();
    }
    int r = 0;
    int g = 0;
    int b = 0;
    if (m_color.length() == 7 && m_color.charAt(0) == '#') {
      try {
        r = Integer.parseInt(m_color.substring(1, 3), 16);
        g = Integer.parseInt(m_color.substring(3, 5), 16);
        b = Integer.parseInt(m_color.substring(5, 7), 16);
      } catch (NumberFormatException e) {
        r = 0;
        g = 0;
        b = 0;
      }
    }
    return new Color8Bit(r, g, b);
  }

  /**
   * Set the line thickness.
   *
   * @param weight the line thickness
   */
  public synchronized void setLineWeight(double weight) {
    m_weight = weight;
    if (m_weightEntry != null) {
      m_weightEntry.set(weight);
    }
  }

  /**
   * Get the line thickness.
   *
   * @return the line thickness
   */
  public synchronized double getLineWeight() {
    if (m_weightEntry != null) {
      m_weight = m_weightEntry.get();
    }
    return m_weight;
  }

  @Override
  protected void updateEntries(NetworkTable table) {
    if (m_typePub != null) {
      m_typePub.close();
    }
    m_typePub = table.getStringTopic(".type").publish();
    m_typePub.set("line");

    if (m_angleEntry != null) {
      m_angleEntry.close();
    }
    m_angleEntry = table.getDoubleTopic("angle").getEntry(0.0);
    m_angleEntry.set(m_angle);

    if (m_lengthEntry != null) {
      m_lengthEntry.close();
    }
    m_lengthEntry = table.getDoubleTopic("length").getEntry(0.0);
    m_lengthEntry.set(m_length);

    if (m_colorEntry != null) {
      m_colorEntry.close();
    }
    m_colorEntry = table.getStringTopic("color").getEntry("");
    m_colorEntry.set(m_color);

    if (m_weightEntry != null) {
      m_weightEntry.close();
    }
    m_weightEntry = table.getDoubleTopic("weight").getEntry(0.0);
    m_weightEntry.set(m_weight);
  }

  synchronized void logOutput(LogTable table) {
    table.put(".type", "line");
    table.put("angle", m_angle);
    table.put("length", m_length);
    table.put("color", m_color);
    table.put("weight", m_weight);
    super.logOutput(table);
  }
}
