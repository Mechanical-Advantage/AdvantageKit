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

package org.littletonrobotics.junction;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringArraySubscriber;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
class AlertLogger {
  private static Map<String, Object> groups = null;
  private static Map<String, StringArraySubscriber> errorSubscribers = new HashMap<>();
  private static Map<String, StringArraySubscriber> warningSubscribers = new HashMap<>();
  private static Map<String, StringArraySubscriber> infoSubscribers = new HashMap<>();

  static {
    try {
      Class<?> sendableAlertsClass = Class.forName("edu.wpi.first.wpilibj.Alert$SendableAlerts");
      Field groupsField = sendableAlertsClass.getDeclaredField("groups");
      groupsField.setAccessible(true);
      groups = (Map<String, Object>) groupsField.get(null);
    } catch (ClassNotFoundException
        | IllegalArgumentException
        | IllegalAccessException
        | NoSuchFieldException
        | SecurityException e) {
      e.printStackTrace();
    }
  }

  /** Log the current state of all alerts as outputs. */
  public static void periodic() {
    if (groups == null)
      return;
    for (String group : groups.keySet()) {
      Logger.recordOutput(group + "/.type", "Alerts");

      // Create NetworkTables subscribers
      if (!errorSubscribers.containsKey(group)) {
        errorSubscribers.put(
            group,
            NetworkTableInstance.getDefault()
                .getStringArrayTopic("/SmartDashboard/" + group + "/errors")
                .subscribe(new String[0]));
      }
      if (!warningSubscribers.containsKey(group)) {
        warningSubscribers.put(
            group,
            NetworkTableInstance.getDefault()
                .getStringArrayTopic("/SmartDashboard/" + group + "/warnings")
                .subscribe(new String[0]));
      }
      if (!infoSubscribers.containsKey(group)) {
        infoSubscribers.put(
            group,
            NetworkTableInstance.getDefault()
                .getStringArrayTopic("/SmartDashboard/" + group + "/infos")
                .subscribe(new String[0]));
      }

      // Get values
      Logger.recordOutput(group + "/errors", errorSubscribers.get(group).get());
      Logger.recordOutput(group + "/warnings", warningSubscribers.get(group).get());
      Logger.recordOutput(group + "/infos", infoSubscribers.get(group).get());
    }
  }
}
