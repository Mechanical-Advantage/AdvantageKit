package org.littletonrobotics.junction.rlog;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.littletonrobotics.junction.LogDataReceiver;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

/** Sends log data over a socket connection using the RLOG format. */
public class RLOGServer implements LogDataReceiver {
  private final int port;
  private ServerThread thread;

  public RLOGServer(int port) {
    this.port = port;
  }

  public void start() {
    thread = new ServerThread(port);
    thread.start();
    System.out.println("Log server started on port " + Integer.toString(port));
  }

  public void end() {
    if (thread != null) {
      thread.close();
      thread = null;
    }
  }

  public void putTable(LogTable table) {
    if (thread != null) {
      thread.periodic(table);
    }
  }

  private class ServerThread extends Thread {
    private static final double heartbeatTimeoutSecs = 3.0; // Close connection if hearbeat not received for this length

    ServerSocket server;
    RLOGEncoder encoder = new RLOGEncoder();

    List<Socket> sockets = new ArrayList<>();
    List<Double> lastHeartbeats = new ArrayList<>();

    public ServerThread(int port) {
      super("LogSocketServer");
      this.setDaemon(true);
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
          socket.getOutputStream().write(encodeData(encoder.getNewcomerData().array()));
          sockets.add(socket);
          lastHeartbeats.add(Logger.getRealTimestamp() / 1000000.0);
          System.out.println("Connected to log client - " + socket.getInetAddress().getHostAddress());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    public void periodic(LogTable table) {
      if (server == null) {
        return;
      }

      encoder.encodeTable(table);
      byte[] data = encodeData(encoder.getOutput().array());
      for (int i = 0; i < sockets.size(); i++) {
        Socket socket = sockets.get(i);
        if (socket.isClosed()) {
          continue;
        }

        try {
          // Read heartbeat
          InputStream inputStream = socket.getInputStream();
          if (inputStream.available() > 0) {
            inputStream.skip(inputStream.available());
            lastHeartbeats.set(i, Logger.getRealTimestamp() / 1000000.0);
          }

          // Close connection if socket timed out
          if (Logger.getRealTimestamp() / 1000000.0 - lastHeartbeats.get(i) > heartbeatTimeoutSecs) {
            socket.close();
            printDisconnectMessage(socket, "timeout");
          } else {

            // Send new data
            socket.getOutputStream().write(data);
          }
        } catch (IOException e) {
          try {
            socket.close();
            printDisconnectMessage(socket, "IOException");
          } catch (IOException a) {
            a.printStackTrace();
          }
        }
      }
    }

    private byte[] encodeData(byte[] data) {
      byte[] lengthBytes = ByteBuffer.allocate(Integer.BYTES).putInt(data.length).array();
      byte[] fullData = new byte[lengthBytes.length + data.length];
      System.arraycopy(lengthBytes, 0, fullData, 0, lengthBytes.length);
      System.arraycopy(data, 0, fullData, lengthBytes.length, data.length);
      return fullData;
    }

    private void printDisconnectMessage(Socket socket, String reason) {
      System.out.println("Disconnected from log client (" + reason + ") - " + socket.getInetAddress().getHostAddress());
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
