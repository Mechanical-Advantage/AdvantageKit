// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.wpilib.driverstation.Alert;

/** An Alert wrapper that is compatible with AdvantageKit logging. */
public class LoggedAlert extends Alert {
  private static final List<LoggedAlert> registry = Collections.synchronizedList(new ArrayList<>());

  private final String group;

  /**
   * Creates a new LoggedAlert in the default group ("Alerts").
   *
   * @param text The text to display when active.
   * @param type The alert severity level.
   */
  public LoggedAlert(String text, Level type) {
    this("Alerts", text, type);
  }

  /**
   * Creates a new LoggedAlert in the specified group.
   *
   * @param group The group name.
   * @param text The text to display when active.
   * @param type The alert severity level.
   */
  public LoggedAlert(String group, String text, Level type) {
    super(group, text, type);
    this.group = group;
    registry.add(this);
  }

  /**
   * Gets the group name of this alert.
   *
   * @return The group name.
   */
  public String getGroup() {
    return group;
  }

  /** Log the current state of all alerts as outputs. */
  static void periodic() {
    List<LoggedAlert> alertsCopy;
    synchronized (LoggedAlert.registry) {
      alertsCopy = new ArrayList<>(LoggedAlert.registry);
    }

    // Identify all unique groups
    Set<String> groups = new HashSet<>();
    for (LoggedAlert alert : alertsCopy) {
      groups.add(alert.getGroup());
    }

    for (String group : groups) {
      List<String> lowAlerts = new ArrayList<>();
      List<String> mediumAlerts = new ArrayList<>();
      List<String> highAlerts = new ArrayList<>();

      // Get all active alerts for this group
      List<LoggedAlert> activeGroupAlerts = new ArrayList<>();
      for (LoggedAlert alert : alertsCopy) {
        if (alert.getGroup().equals(group) && alert.get()) {
          activeGroupAlerts.add(alert);
        }
      }

      for (LoggedAlert alert : activeGroupAlerts) {
        if (alert.getType() == Level.LOW) {
          lowAlerts.add(alert.getText());
        } else if (alert.getType() == Level.MEDIUM) {
          mediumAlerts.add(alert.getText());
        } else if (alert.getType() == Level.HIGH) {
          highAlerts.add(alert.getText());
        }
      }

      Logger.recordOutput(group + "/.type", "Alerts");
      Logger.recordOutput(group + "/low", lowAlerts.toArray(new String[0]));
      Logger.recordOutput(group + "/medium", mediumAlerts.toArray(new String[0]));
      Logger.recordOutput(group + "/high", highAlerts.toArray(new String[0]));
    }
  }
}
