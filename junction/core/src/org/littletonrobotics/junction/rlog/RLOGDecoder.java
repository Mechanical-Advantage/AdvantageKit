package org.littletonrobotics.junction.rlog;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.wpi.first.wpilibj.DriverStation;
import org.littletonrobotics.junction.LogTable;

/** Converts the RLOG format to log tables. */
public class RLOGDecoder {
  public static final List<Byte> supportedLogRevisions = List.of((byte) 1);

  private Byte logRevision = null;
  private Map<Short, String> keyIDs = new HashMap<>();

  /**
   * Updates a table with data from the next cycle.
   * 
   * @param input The RLOG data to decode.
   * @param table The table to update with new data.
   * @return A boolean indicating whether the replay should exit.
   */
  public boolean decodeTable(DataInputStream input, LogTable table) {
    readTable: try {
      if (logRevision == null) {
        logRevision = input.readByte();
        if (!supportedLogRevisions.contains(logRevision)) {
          DriverStation.reportError("Log revision " + Integer.toString(logRevision & 0xff) + " is not supported.",
              false);
          return true;
        }
        input.skip(1); // Second byte specifies timestamp type, this will be assumed
      }
      if (input.available() == 0) {
        return true; // No more data, so we can't start a new table
      }
      table.setTimestamp((long) (decodeTimestamp(input) * 1000000.0));

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
            decodeValue(input, table);
            break;
        }
      }

    } catch (IOException e) {
      return true; // Problem decoding, might have been interrupted while writing this cycle
    }

    return false;
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

  private void decodeValue(DataInputStream input, LogTable table) throws IOException {
    String key = keyIDs.get(input.readShort()).substring(1);
    short length;
    switch (input.read()) {
      case 0:
        // The null type (delete command) is no longer supported
        break;
      case 1:
        table.put(key, input.readBoolean());
        break;
      case 2:
        length = input.readShort();
        boolean[] booleanArray = new boolean[length];
        for (int i = 0; i < length; i++) {
          booleanArray[i] = input.readBoolean();
        }
        table.put(key, booleanArray);
        break;
      case 3:
        table.put(key, input.readInt());
        break;
      case 4:
        length = input.readShort();
        long[] intArray = new long[length];
        for (int i = 0; i < length; i++) {
          intArray[i] = input.readInt();
        }
        table.put(key, intArray);
        break;
      case 5:
        table.put(key, input.readDouble());
        break;
      case 6:
        length = input.readShort();
        double[] doubleArray = new double[length];
        for (int i = 0; i < length; i++) {
          doubleArray[i] = input.readDouble();
        }
        table.put(key, doubleArray);
        break;
      case 7:
        length = input.readShort();
        table.put(key, new String(input.readNBytes(length), "UTF-8"));
        break;
      case 8:
        length = input.readShort();
        String[] stringArray = new String[length];
        for (int i = 0; i < length; i++) {
          short stringLength = input.readShort();
          stringArray[i] = new String(input.readNBytes(stringLength), "UTF-8");
        }
        table.put(key, stringArray);
        break;
      case 9:
        // Convert single byte to byte array (single bytes are no longer supported)
        table.put(key, new byte[] { input.readByte() });
        break;
      case 10:
        length = input.readShort();
        byte[] byteArray = new byte[length];
        for (int i = 0; i < length; i++) {
          byteArray[i] = input.readByte();
        }
        table.put(key, byteArray);
        break;
      default:
        break;
    }
  }
}
