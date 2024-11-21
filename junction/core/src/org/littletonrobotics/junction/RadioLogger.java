// Copyright 2021-2024 FRC 6328
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

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.RobotController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LogValue;

class RadioLogger {
  private static final double requestPeriodSecs = 0.25;
  private static final int connectTimeout = 100;
  private static final int readTimeout = 100;

  private static URL statusURL;
  private static Notifier notifier;
  private static final Object lock = new Object();
  private static boolean isConnected = false;
  private static String statusJson = "";

  public static void periodic(LogTable table) {
    if (notifier == null && RobotBase.isReal()) {
      start();
    }

    synchronized (lock) {
      table.put("Connected", isConnected);
      table.put("Status", new LogValue(statusJson, "json"));
    }
  }

  private static void start() {
    // Get status URL
    int teamNumber = RobotController.getTeamNumber();
    StringBuilder statusURLBuilder = new StringBuilder();
    statusURLBuilder.append("http://10.");
    statusURLBuilder.append(teamNumber / 100);
    statusURLBuilder.append(".");
    statusURLBuilder.append(teamNumber % 100);
    statusURLBuilder.append(".1/status");
    try {
      statusURL = new URL(statusURLBuilder.toString());
    } catch (MalformedURLException e) {
      return;
    }

    // Launch notifier
    notifier = new Notifier(
        () -> {
          // Request status from radio
          StringBuilder response = new StringBuilder();
          try {
            HttpURLConnection connection = (HttpURLConnection) statusURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
              for (String line; (line = reader.readLine()) != null;) {
                response.append(line);
              }
            }
          } catch (Exception e) {
          }

          // Update status
          String responseStr = response.toString().replaceAll("\\s+", "");
          synchronized (lock) {
            isConnected = responseStr.length() > 0;
            statusJson = responseStr;
          }
        });
    notifier.setName("RadioLogger");
    notifier.setHALThreadPriority(false, 0);
    notifier.startPeriodic(requestPeriodSecs);
  }
}
