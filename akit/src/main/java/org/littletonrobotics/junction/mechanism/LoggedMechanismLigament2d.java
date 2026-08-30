// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.mechanism;

import static org.wpilib.units.Units.Degrees;
import static org.wpilib.units.Units.Meters;

import org.littletonrobotics.junction.LogTable;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.telemetry.TelemetryTable;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.Distance;
import org.wpilib.util.Color8Bit;

/**
 * Ligament node on a Mechanism2d. A ligament can have its length changed (like an elevator) or
 * angle changed, like an arm.
 *
 * @see org.littletonrobotics.junction.mechanism.LoggedMechanism2d
 */
public class LoggedMechanismLigament2d extends LoggedMechanismObject2d {
  private double m_angle;
  private String m_color;
  private double m_length;
  private double m_weight;

  /**
   * Create a new ligament.
   *
   * @param name The ligament name.
   * @param length The ligament length.
   * @param angle The ligament angle in degrees.
   * @param lineWidth The ligament's line width.
   * @param color The ligament's color.
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
   * Create a new ligament.
   *
   * @param name The ligament name.
   * @param length The ligament length.
   * @param angle The ligament angle.
   * @param lineWidth The ligament's line width.
   * @param color The ligament's color.
   */
  public LoggedMechanismLigament2d(
      String name, Distance length, Angle angle, double lineWidth, Color8Bit color) {
    this(name, length.in(Meters), angle.in(Degrees), lineWidth, color);
  }

  /**
   * Create a new ligament with the default color (orange) and thickness (10).
   *
   * @param name The ligament's name.
   * @param length The ligament's length.
   * @param angle The ligament's angle relative to its parent in degrees.
   */
  public LoggedMechanismLigament2d(String name, double length, double angle) {
    this(name, length, angle, 10, new Color8Bit(235, 137, 52));
  }

  /**
   * Create a new ligament with the default color (orange) and thickness (10).
   *
   * @param name The ligament's name.
   * @param length The ligament's length.
   * @param angle The ligament's angle relative to its parent.
   */
  public LoggedMechanismLigament2d(String name, Distance length, Angle angle) {
    this(name, length.in(Meters), angle.in(Degrees));
  }

  /**
   * Set the ligament's angle relative to its parent.
   *
   * @param degrees the angle in degrees
   */
  public final synchronized void setAngle(double degrees) {
    m_angle = degrees;
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
   * Set the ligament's angle relative to its parent.
   *
   * @param angle the angle
   */
  public synchronized void setAngle(Angle angle) {
    setAngle(angle.in(Degrees));
  }

  /**
   * Get the ligament's angle relative to its parent.
   *
   * @return the angle in degrees
   */
  public synchronized double getAngle() {
    return m_angle;
  }

  /**
   * Set the ligament's length.
   *
   * @param length the line length
   */
  public final synchronized void setLength(double length) {
    m_length = length;
  }

  /**
   * Set the ligament's length.
   *
   * @param length the line length
   */
  public synchronized void setLength(Distance length) {
    setLength(length.in(Meters));
  }

  /**
   * Get the ligament length.
   *
   * @return the line length
   */
  public synchronized double getLength() {
    return m_length;
  }

  /**
   * Set the ligament color.
   *
   * @param color the color of the line
   */
  public final synchronized void setColor(Color8Bit color) {
    m_color = String.format("#%02X%02X%02X", color.red, color.green, color.blue);
  }

  /**
   * Get the ligament color.
   *
   * @return the color of the line
   */
  public synchronized Color8Bit getColor() {
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
  public final synchronized void setLineWeight(double weight) {
    m_weight = weight;
  }

  /**
   * Get the line thickness.
   *
   * @return the line thickness
   */
  public synchronized double getLineWeight() {
    return m_weight;
  }

  @Override
  public void logTo(TelemetryTable table) {
    double angle;
    double length;
    String color;
    double weight;
    synchronized (this) {
      angle = m_angle;
      length = m_length;
      color = m_color;
      weight = m_weight;
    }

    table.log("angle", angle);
    table.log("length", length);
    table.log("color", color);
    table.log("weight", weight);
    super.logTo(table);
  }

  @Override
  public String getTelemetryType() {
    return "line";
  }

  @Override
  synchronized void logOutput(LogTable table) {
    table.put(".type", "line");
    table.put("angle", m_angle);
    table.put("length", m_length);
    table.put("color", m_color);
    table.put("weight", m_weight);
    super.logOutput(table);
  }

  @Override
  public double getObject2dRange() {
    return getLength();
  }
}
