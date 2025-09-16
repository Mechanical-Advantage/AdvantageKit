// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.RobotController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.littletonrobotics.junction.LogTable.LogValue;

class RadioLogger {
  private static final double requestPeriodSecs = 5.0;
  private static final int connectTimeout = 500;
  private static final int readTimeout = 500;

  private static URL statusURL;
  private static Notifier notifier;
  private static final Object lock = new Object();
  private static boolean isConnected = false;
  private static String statusJson = "";

  public static void periodic(LogTable table) {
    if (notifier == null && RobotController.getTeamNumber() != 0) {
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
      statusURL = new URI(statusURLBuilder.toString()).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      return;
    }

    // Launch notifier
    notifier =
        new Notifier(
            () -> {
              // Request status from radio
              StringBuilder response = new StringBuilder();
              try {
                HttpURLConnection connection = (HttpURLConnection) statusURL.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(connectTimeout);
                connection.setReadTimeout(readTimeout);

                try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                  for (String line; (line = reader.readLine()) != null; ) {
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
    notifier.setName("AdvantageKit_RadioLogger");
    notifier.startPeriodic(requestPeriodSecs);
  }
}
