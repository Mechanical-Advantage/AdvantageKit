package org.littletonrobotics.conduit;

import java.nio.ByteBuffer;

public class ConduitApi {
  public static final int EVENT_NAME_MAX_SIZE = 64;
  public static final int GAME_SPECIFIC_MESSAGE_MAX_SIZE = 64;

  public static final int JOYSTICK_NAME_MAX_SIZE = 256;
  public static final int JOYSTICK_NUM_AXES = 12;
  public static final int JOYSTICK_NUM_POVS = 12;
  public static final int NUM_JOYSTICKS = 6;
  // Name(256 char), type(uint8), axisCount(uint16), buttonCount(uint8),
  // povCount(uint16), isXbox(uint8), axisTypes(uint8[12]),
  // axisValues(float32[12]), buttons(uint32), povCount(int16[12])
  public static final int JOYSTICK_DATA_SIZE = JOYSTICK_NAME_MAX_SIZE + 1 + 2 + 1 + 2 + 1 + (JOYSTICK_NUM_AXES * 1)
      + (JOYSTICK_NUM_AXES * 4) + 4 + (JOYSTICK_NUM_POVS * 2);

  private static ConduitApi instance = null;

  public static ConduitApi getInstance() {
    if (instance == null) {
      instance = new ConduitApi();
    }
    return instance;
  }

  private ByteBuffer buf;

  private ConduitApi() {
    buf = ConduitJni.getBuffer();
  }

  public long getTimestamp() {
    return Integer.toUnsignedLong(buf.getInt(0));
  };

  public int getAllianceStation() {
    return Byte.toUnsignedInt(buf.get(4));
  };

  public String getEventName() {
    byte[] eventNameBuf = new byte[EVENT_NAME_MAX_SIZE];

    buf.get(eventNameBuf, 5, EVENT_NAME_MAX_SIZE);
    return new String(eventNameBuf);
  };

  public String getEventSpecificMessage() {
    byte[] gameSpecificMessageBuf = new byte[GAME_SPECIFIC_MESSAGE_MAX_SIZE];

    buf.get(gameSpecificMessageBuf, 69, GAME_SPECIFIC_MESSAGE_MAX_SIZE);
    return new String(gameSpecificMessageBuf);
  };

  public int getGameSpecificMessageSize() {
    return Short.toUnsignedInt(buf.getShort(133));
  };

  public int getMatchNumber() {
    return Short.toUnsignedInt(buf.getShort(135));
  };

  public int getReplayNumber() {
    return Byte.toUnsignedInt(buf.get(136));
  };

  public int getMatchType() {
    return buf.getInt(137);
  };

  public int getControlWord() {
    return buf.getInt(141);
  };

  public double getMatchTime() {
    return buf.getDouble(145);
  };

  public String getJoystickName(int joystickId) {
    byte[] joystickNameBuf = new byte[JOYSTICK_NAME_MAX_SIZE];
    int offset = 153 + joystickId * JOYSTICK_DATA_SIZE;
    buf.get(joystickNameBuf, offset, JOYSTICK_NAME_MAX_SIZE);
    return new String(joystickNameBuf);
  };

  public int getJoystickType(int joystickId) {
    int offset = 409 + joystickId * JOYSTICK_DATA_SIZE;
    return Byte.toUnsignedInt(buf.get(offset));
  };

  public int getButtonCount(int joystickId) {
    int offset = 410 + joystickId * JOYSTICK_DATA_SIZE;
    return Byte.toUnsignedInt(buf.get(offset));
  };

  public int getButtonValues(int joystickId) {
    int offset = 411 + joystickId * JOYSTICK_DATA_SIZE;
    return buf.getInt(offset);
  };

  public int getAxisCount(int joystickId) {
    int offset = 415 + joystickId * JOYSTICK_DATA_SIZE;
    return buf.getShort(offset);
  };

  public int[] getAxisTypes(int joystickId) {
    int[] axisTypeBuf = new int[JOYSTICK_NUM_AXES];
    int offset = 417 + joystickId * JOYSTICK_DATA_SIZE;

    for (int i = 0; i < JOYSTICK_NUM_AXES; i++) {
      axisTypeBuf[i] = Byte.toUnsignedInt(buf.get(offset + i));
    }
    return axisTypeBuf;
  };

  public float[] getAxisValues(int joystickId) {
    float[] axisValueBuf = new float[JOYSTICK_NUM_AXES];

    // Base offset + axisTypes(uint8[12])
    int offset = 417 + (JOYSTICK_NUM_AXES * 1) + joystickId * JOYSTICK_DATA_SIZE;

    for (int i = 0; i < JOYSTICK_NUM_AXES; i++) {
      axisValueBuf[i] = buf.getFloat(offset + i * 4);
    }
    return axisValueBuf;
  };

  public int getPovCount(int joystickId) {
    int offset = 417 + (JOYSTICK_NUM_AXES * 1) + (JOYSTICK_NUM_AXES * 4) + joystickId * JOYSTICK_DATA_SIZE;

    return buf.getShort(offset);
  };

  public int[] getPovValues(int joystickId) {
    int[] povCountBuf = new int[JOYSTICK_NUM_AXES];
    int offset = 419 + (JOYSTICK_NUM_AXES * 1) + (JOYSTICK_NUM_AXES * 4) + joystickId * JOYSTICK_DATA_SIZE;

    for (int i = 0; i < JOYSTICK_NUM_POVS; i++) {
      povCountBuf[i] = buf.getShort(offset + i * 2);
    }
    return povCountBuf;
  };

  public boolean isXbox(int joystickId) {
    int offset = 419 + (JOYSTICK_NUM_AXES * 1) + (JOYSTICK_NUM_AXES * 4) + (JOYSTICK_NUM_POVS * 2)
        + joystickId * JOYSTICK_DATA_SIZE;
    return buf.get(offset) != 0;
  }
}