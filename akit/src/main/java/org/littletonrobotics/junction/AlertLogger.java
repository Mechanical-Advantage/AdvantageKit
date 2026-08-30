// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.wpilib.util.AlertDataJNI;

class AlertLogger {
  private static final Set<String> knownGroups = new HashSet<>();

  /** Log the current state of all alerts as outputs. */
  public static void periodic() {
    AlertDataJNI.AlertInfo[] allAlerts = AlertDataJNI.getAlerts();

    Map<String, List<AlertDataJNI.AlertInfo>> activeByGroup = new HashMap<>();

    for (AlertDataJNI.AlertInfo info : allAlerts) {
      knownGroups.add(info.group);
      if (info.activeStartTime != 0) {
        activeByGroup.computeIfAbsent(info.group, g -> new ArrayList<>()).add(info);
      }
    }

    for (String group : knownGroups) {
      List<AlertDataJNI.AlertInfo> groupAlerts =
          activeByGroup.getOrDefault(group, Collections.emptyList());

      // Sort by most recent first, then text ascending
      groupAlerts.sort(
          Comparator.comparingLong((AlertDataJNI.AlertInfo a) -> a.activeStartTime)
              .reversed()
              .thenComparing(a -> a.text));

      List<String> errors = new ArrayList<>();
      List<String> warnings = new ArrayList<>();
      List<String> infos = new ArrayList<>();

      for (AlertDataJNI.AlertInfo alert : groupAlerts) {
        switch (alert.level) {
          case AlertDataJNI.LEVEL_HIGH -> errors.add(alert.text);
          case AlertDataJNI.LEVEL_MEDIUM -> warnings.add(alert.text);
          case AlertDataJNI.LEVEL_LOW -> infos.add(alert.text);
        }
      }

      Logger.recordOutput(group + "/.type", "Alerts");
      Logger.recordOutput(group + "/errors", errors.toArray(new String[0]));
      Logger.recordOutput(group + "/warnings", warnings.toArray(new String[0]));
      Logger.recordOutput(group + "/infos", infos.toArray(new String[0]));
    }
  }
}
