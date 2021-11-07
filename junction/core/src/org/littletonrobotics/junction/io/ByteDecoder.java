// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.littletonrobotics.junction.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.LogTable.LoggableType;

/** Converts byte array format to log tables. */
public class ByteDecoder {
  public static final List<Byte> supportedLogRevisions = List.of((byte) 1);

  Byte logRevision = null;
  LogTable table = new LogTable(0.0);
  Map<Short, String> keyIDs = new HashMap<>();

  public LogTable decodeTable(DataInputStream input) {
    readTable: try {
      if (logRevision == null) {
        logRevision = input.readByte();
        if (!supportedLogRevisions.contains(logRevision)) {
          DriverStation.reportError("Log revision " + Integer.toString(logRevision & 0xff) + " is not supported.",
              false);
          return null;
        }
        input.skip(1); // Second byte specifies timestamp type, this will be assumed
      }
      if (input.available() == 0) {
        return null; // No more data, so we can't start a new table
      }
      table = new LogTable(decodeTimestamp(input), table);

      readLoop: while (true) {
        if (input.available() == 0) {
          break readTable; // This was the last cycle, return the data
        }

        byte type = input.readByte();
        switch (type) {
        case 0: // Next timestamp
          break readLoop;
        case 1: // New key ID
          decodeKey(input);
          break;
        case 2: // Updated field
          decodeValue(input);
          break;
        }
      }

    } catch (IOException e) {
      return null; // Problem decoding, might have been interrupted while writing this cycle
    }

    return new LogTable(table.getTimestamp(), table);
  }

  private double decodeTimestamp(DataInputStream input) throws IOException {
    return input.readDouble();
  }

  private void decodeKey(DataInputStream input) throws IOException {
    short keyID = input.readShort();
    short length = input.readShort();
    String key = new String(input.readNBytes(length), "UTF-8");
    keyIDs.put(keyID, key);
  }

  private void decodeValue(DataInputStream input) throws IOException {
    String key = keyIDs.get(input.readShort()).substring(1);
    int typeInt = input.read();
    if (typeInt == 0) {
      table.remove(key);
      return;
    }

    LoggableType type = LoggableType.values()[typeInt - 1];
    short length;
    switch (type) {
    case Boolean:
      table.put(key, input.readBoolean());
      break;
    case Byte:
      table.put(key, input.readByte());
      break;
    case Integer:
      table.put(key, input.readInt());
      break;
    case Double:
      table.put(key, input.readDouble());
      break;
    case String:
      length = input.readShort();
      table.put(key, new String(input.readNBytes(length), "UTF-8"));
      break;
    case BooleanArray:
      length = input.readShort();
      boolean[] booleanArray = new boolean[length];
      for (int i = 0; i < length; i++) {
        booleanArray[i] = input.readBoolean();
      }
      table.put(key, booleanArray);
      break;
    case ByteArray:
      length = input.readShort();
      byte[] byteArray = new byte[length];
      for (int i = 0; i < length; i++) {
        byteArray[i] = input.readByte();
      }
      table.put(key, byteArray);
      break;
    case IntegerArray:
      length = input.readShort();
      int[] intArray = new int[length];
      for (int i = 0; i < length; i++) {
        intArray[i] = input.readInt();
      }
      table.put(key, intArray);
      break;
    case DoubleArray:
      length = input.readShort();
      double[] doubleArray = new double[length];
      for (int i = 0; i < length; i++) {
        doubleArray[i] = input.readDouble();
      }
      table.put(key, doubleArray);
      break;
    case StringArray:
      length = input.readShort();
      String[] stringArray = new String[length];
      for (int i = 0; i < length; i++) {
        short stringLength = input.readShort();
        stringArray[i] = new String(input.readNBytes(stringLength), "UTF-8");
      }
      table.put(key, stringArray);
      break;
    }
  }
}
