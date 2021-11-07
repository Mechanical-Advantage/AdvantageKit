// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.littletonrobotics.junction.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/** Sends log data over a socket connection. */
public class LogSocketServer implements LogRawDataReceiver {
  final int port;
  ServerThread thread;

  public LogSocketServer(int port) {
    this.port = port;
  }

  public void start(ByteEncoder encoder) {
    thread = new ServerThread(port, encoder);
    thread.start();
    System.out.println("Log server started on port " + Integer.toString(port));
  }

  public void end() {
    if (thread != null) {
      thread.close();
      thread = null;
    }
  }

  public void processEntry() {
    if (thread != null) {
      thread.sendEntry();
    }
  }

  private class ServerThread extends Thread {
    ServerSocket server;
    ByteEncoder encoder;

    List<Socket> sockets = new ArrayList<>();

    public ServerThread(int port, ByteEncoder encoder) {
      this.encoder = encoder;
      try {
        server = new ServerSocket(port);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void run() {
      if (server == null) {
        return;
      }
      while (true) {
        try {
          Socket socket = server.accept();
          byte[] data = encoder.getNewcomerData().array();
          socket.getOutputStream().write(data);
          sockets.add(socket);
          System.out.println("Connected to log client - " + socket.getInetAddress().getHostAddress());

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    public void sendEntry() {
      if (server == null) {
        return;
      }
      byte[] data = encoder.getOutput().array();
      for (int i = 0; i < sockets.size(); i++) {
        Socket socket = sockets.get(i);
        if (socket.isClosed()) {
          continue;
        }

        try {
          socket.getOutputStream().write(data);
        } catch (IOException e) {
          try {
            socket.close();
            System.out.println("Disconnected from log client - " + socket.getInetAddress().getHostAddress());
          } catch (IOException a) {
            a.printStackTrace();
          }
        }
      }

    }

    public void close() {
      if (server != null) {
        try {
          server.close();
          server = null;
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      this.interrupt();
    }
  }
}
