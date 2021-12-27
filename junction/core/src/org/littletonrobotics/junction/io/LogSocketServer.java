package org.littletonrobotics.junction.io;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/** Sends log data over a socket connection. */
public class LogSocketServer implements LogRawDataReceiver {
  private final int port;
  private ServerThread thread;

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
      super("LogSocketServer");
      this.setDaemon(true);
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
          byte[] lengthBytes = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();
          byte[] fullData = new byte[lengthBytes.length + data.length];
          System.arraycopy(lengthBytes, 0, fullData, 0, lengthBytes.length);
          System.arraycopy(data, 0, fullData, lengthBytes.length, data.length);
          socket.getOutputStream().write(fullData);
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
      byte[] lengthBytes = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();
      byte[] fullData = new byte[lengthBytes.length + data.length];
      System.arraycopy(lengthBytes, 0, fullData, 0, lengthBytes.length);
      System.arraycopy(data, 0, fullData, lengthBytes.length, data.length);
      for (int i = 0; i < sockets.size(); i++) {
        Socket socket = sockets.get(i);
        if (socket.isClosed()) {
          continue;
        }

        try {
          socket.getOutputStream().write(fullData);
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
