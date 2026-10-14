// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.wpilog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/** Encode data into the WPILOG format with the output provided via a stream. */
class WPILOGEncoder {
  private final OutputStream outputStream;
  private int nextEntryId = 1;

  // Reusable buffers to minimize allocations
  private final ByteArrayOutputStream payloadBuffer = new ByteArrayOutputStream(1024);
  private final ByteBuffer primitiveBuffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
  private final byte[] headerBuffer = new byte[17];

  public WPILOGEncoder(OutputStream outputStream) throws IOException {
    this.outputStream = outputStream;
  }

  public void close() throws IOException {
    outputStream.close();
  }

  public void flush() throws IOException {
    outputStream.flush();
  }

  public void writeHeader(String extraHeader) throws IOException {
    outputStream.write("WPILOG".getBytes(StandardCharsets.UTF_8));
    outputStream.write(0x00);
    outputStream.write(0x01);

    byte[] extraBytes = extraHeader.getBytes(StandardCharsets.UTF_8);
    ByteBuffer lenBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
    lenBuf.putInt(extraBytes.length);
    outputStream.write(lenBuf.array());
    outputStream.write(extraBytes);
  }

  public int startEntry(String name, String type, String metadata, long timestampNs)
      throws IOException {
    int id = nextEntryId++;
    payloadBuffer.reset();
    payloadBuffer.write(0);
    writeIntToBuffer(id, 4);
    writeStringToBuffer(name);
    writeStringToBuffer(type);
    writeStringToBuffer(metadata);
    writeRecord(0, nanosToMicros(timestampNs), payloadBuffer.toByteArray());
    return id;
  }

  public void finishEntry(int id, long timestampNs) throws IOException {
    payloadBuffer.reset();
    payloadBuffer.write(1);
    writeIntToBuffer(id, 4);
    writeRecord(0, nanosToMicros(timestampNs), payloadBuffer.toByteArray());
  }

  public void setMetadata(int id, String metadata, long timestampNs) throws IOException {
    payloadBuffer.reset();
    payloadBuffer.write(2);
    writeIntToBuffer(id, 4);
    writeStringToBuffer(metadata);
    writeRecord(0, nanosToMicros(timestampNs), payloadBuffer.toByteArray());
  }

  // MARK: Data Appenders

  public void appendRaw(int id, byte[] value, long timestampNs) throws IOException {
    writeRecord(id, nanosToMicros(timestampNs), value);
  }

  public void appendBoolean(int id, boolean value, long timestampNs) throws IOException {
    writeRecord(id, nanosToMicros(timestampNs), new byte[] {(byte) (value ? 1 : 0)});
  }

  public void appendInteger(int id, long value, long timestampNs) throws IOException {
    primitiveBuffer.clear();
    primitiveBuffer.putLong(value);
    writeRecord(id, nanosToMicros(timestampNs), primitiveBuffer.array());
  }

  public void appendFloat(int id, float value, long timestampNs) throws IOException {
    primitiveBuffer.clear();
    primitiveBuffer.putFloat(value);
    writeRecord(id, nanosToMicros(timestampNs), primitiveBuffer.array(), 4);
  }

  public void appendDouble(int id, double value, long timestampNs) throws IOException {
    primitiveBuffer.clear();
    primitiveBuffer.putDouble(value);
    writeRecord(id, nanosToMicros(timestampNs), primitiveBuffer.array());
  }

  public void appendString(int id, String value, long timestampNs) throws IOException {
    writeRecord(id, nanosToMicros(timestampNs), value.getBytes(StandardCharsets.UTF_8));
  }

  public void appendBooleanArray(int id, boolean[] value, long timestampNs) throws IOException {
    byte[] bytes = new byte[value.length];
    for (int i = 0; i < value.length; i++) bytes[i] = (byte) (value[i] ? 1 : 0);
    writeRecord(id, nanosToMicros(timestampNs), bytes);
  }

  public void appendIntegerArray(int id, long[] value, long timestampNs) throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(value.length * 8).order(ByteOrder.LITTLE_ENDIAN);
    for (long v : value) bb.putLong(v);
    writeRecord(id, nanosToMicros(timestampNs), bb.array());
  }

  public void appendFloatArray(int id, float[] value, long timestampNs) throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(value.length * 4).order(ByteOrder.LITTLE_ENDIAN);
    for (float v : value) bb.putFloat(v);
    writeRecord(id, nanosToMicros(timestampNs), bb.array());
  }

  public void appendDoubleArray(int id, double[] value, long timestampNs) throws IOException {
    ByteBuffer bb = ByteBuffer.allocate(value.length * 8).order(ByteOrder.LITTLE_ENDIAN);
    for (double v : value) bb.putDouble(v);
    writeRecord(id, nanosToMicros(timestampNs), bb.array());
  }

  public void appendStringArray(int id, String[] value, long timestampNs) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ByteBuffer lenBuf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
    lenBuf.putInt(value.length);
    bos.write(lenBuf.array());
    for (String s : value) {
      byte[] sBytes = s.getBytes(StandardCharsets.UTF_8);
      lenBuf.clear();
      lenBuf.putInt(sBytes.length);
      bos.write(lenBuf.array());
      bos.write(sBytes);
    }
    writeRecord(id, nanosToMicros(timestampNs), bos.toByteArray());
  }

  // MARK: Internal Helpers

  private void writeRecord(int entryId, long timestamp, byte[] payload) throws IOException {
    writeRecord(entryId, timestamp, payload, payload.length);
  }

  private void writeRecord(int entryId, long timestamp, byte[] payload, int length)
      throws IOException {
    long unsignedEntryId = Integer.toUnsignedLong(entryId);
    long unsignedLength = Integer.toUnsignedLong(length);
    int idLen = bytesNeeded(unsignedEntryId);
    int sizeLen = bytesNeeded(unsignedLength);
    int tsLen = bytesNeeded(timestamp);

    // Calculate bitfield
    int bitfield = (idLen - 1) | ((sizeLen - 1) << 2) | ((tsLen - 1) << 4);

    // Assemble header
    int pos = 0;
    headerBuffer[pos++] = (byte) bitfield;

    // Write ID
    long tempVal = unsignedEntryId;
    for (int i = 0; i < idLen; i++) {
      headerBuffer[pos++] = (byte) (tempVal & 0xFF);
      tempVal >>>= 8;
    }

    // Write size
    tempVal = unsignedLength;
    for (int i = 0; i < sizeLen; i++) {
      headerBuffer[pos++] = (byte) (tempVal & 0xFF);
      tempVal >>>= 8;
    }

    // Write timestamp
    tempVal = timestamp;
    for (int i = 0; i < tsLen; i++) {
      headerBuffer[pos++] = (byte) (tempVal & 0xFF);
      tempVal >>>= 8;
    }

    // Write header
    outputStream.write(headerBuffer, 0, pos);

    // Write payload
    outputStream.write(payload, 0, length);
  }

  private int bytesNeeded(long val) {
    if (val == 0) return 1;
    if (val < 0) return 8;
    if (val < (1L << 8)) return 1;
    if (val < (1L << 16)) return 2;
    if (val < (1L << 24)) return 3;
    if (val < (1L << 32)) return 4;
    if (val < (1L << 40)) return 5;
    if (val < (1L << 48)) return 6;
    if (val < (1L << 56)) return 7;
    return 8;
  }

  private void writeIntToBuffer(int val, int len) {
    for (int i = 0; i < len; i++) {
      payloadBuffer.write((val >> (i * 8)) & 0xFF);
    }
  }

  private void writeStringToBuffer(String str) throws IOException {
    byte[] b = str.getBytes(StandardCharsets.UTF_8);
    writeIntToBuffer(b.length, 4);
    payloadBuffer.write(b);
  }

  private long nanosToMicros(long nanos) {
    return nanos / 1000;
  }
}
