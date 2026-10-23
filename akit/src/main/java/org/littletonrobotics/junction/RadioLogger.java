// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import org.littletonrobotics.junction.LogTable.LogValue;
import org.wpilib.framework.RobotBase;
import org.wpilib.system.Notifier;

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
    if (notifier == null && RobotBase.isReal()) {
      start();
    }

    synchronized (lock) {
      table.put("Connected", isConnected);
      table.put("Status", new LogValue(statusJson, "json"));
    }
  }

  private static void start() {
    // Launch notifier
    notifier =
        new Notifier(
            () -> {
              if (statusURL == null) {
                statusURL = getStatusURL();
              }
              if (statusURL == null) {
                return;
              }

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

  /** Returns the URL for the radio status page, or null if it cannot be determined. */
  private static URL getStatusURL() {
    try {
      NetworkInterface iface = NetworkInterface.getByName("eth0");
      if (iface == null) {
        return null;
      }
      Enumeration<InetAddress> addresses = iface.getInetAddresses();
      while (addresses.hasMoreElements()) {
        InetAddress address = addresses.nextElement();
        byte[] addressBytes = address.getAddress();
        if (addressBytes.length == 4) {
          int b0 = Byte.toUnsignedInt(addressBytes[0]);
          int b1 = Byte.toUnsignedInt(addressBytes[1]);
          int b2 = Byte.toUnsignedInt(addressBytes[2]);
          if (b0 == 10 && b2 < 100 && (b1 > 0 || b2 > 0)) {
            int teamNumber = b1 * 100 + b2;
            StringBuilder statusURLBuilder = new StringBuilder();
            statusURLBuilder.append("http://10.");
            statusURLBuilder.append(teamNumber / 100);
            statusURLBuilder.append(".");
            statusURLBuilder.append(teamNumber % 100);
            statusURLBuilder.append(".1/status");
            return new URI(statusURLBuilder.toString()).toURL();
          }
        }
      }
    } catch (SocketException | MalformedURLException | URISyntaxException e) {
    }
    return null;
  }
}
