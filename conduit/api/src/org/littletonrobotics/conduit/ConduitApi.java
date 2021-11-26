package org.littletonrobotics.conduit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.littletonrobotics.conduit.schema.CoreInputs;
import org.littletonrobotics.conduit.schema.Joystick;

public class ConduitApi {
  // Length constants
  private static final int NUM_JOYSTICK_AXES = 12;
  private static final int NUM_JOYSTICK_POVS = 12;
  private static final int JOYSTICK_NAME_LEN = 256;
  private static final int EVENT_NAME_LEN = 64;
  private static final int GAME_SPECIFIC_MESSAGE_LEN = 64;

  private static ConduitApi instance = null;

  public static ConduitApi getInstance() {
    if (instance == null) {
      instance = new ConduitApi();
    }
    return instance;
  }

  private final CoreInputs inputs = new CoreInputs();

  private ConduitApi() {
    ByteBuffer buffer = ConduitJni.getBuffer();
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    inputs.__init(0, buffer);
  }

  public void captureData() {
    ConduitJni.capture();
  }

  public long getTimestamp() {
    return inputs.timestamp();
  }

  public int getAllianceStation() {
    return inputs.allianceStation();
  }

  public String getEventName() {
    byte[] bytes = new byte[EVENT_NAME_LEN];
    for (int i = 0; i < EVENT_NAME_LEN; i++) {
      bytes[i] = (byte) inputs.eventName(i);
    }
    return new String(bytes);
  }

  public String getGameSpecificMessage() {
    byte[] bytes = new byte[GAME_SPECIFIC_MESSAGE_LEN];
    for (int i = 0; i < GAME_SPECIFIC_MESSAGE_LEN; i++) {
      bytes[i] = (byte) inputs.gameSpecificMessage(i);
    }
    return new String(bytes);
  }

  public int getGameSpecificMessageSize() {
    return inputs.gameSpecificMessageSize();
  }

  public int getMatchNumber() {
    return inputs.matchNumber();
  }

  public int getReplayNumber() {
    return inputs.replayNumber();
  }

  public int getMatchType() {
    return inputs.matchType();
  }

  public int getControlWord() {
    return inputs.controlWord();
  }

  public double getMatchTime() {
    return inputs.matchTime();
  }

  public String getJoystickName(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    byte[] bytes = new byte[JOYSTICK_NAME_LEN];
    for (int i = 0; i < JOYSTICK_NAME_LEN; i++) {
      bytes[i] = (byte) j.name(i);
    }

    return new String(bytes);
  }

  public int getJoystickType(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    return j.type();
  }

  public int getButtonCount(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    return j.buttonCount();
  }

  public int getButtonValues(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    return j.buttons();
  }

  public int getAxisCount(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    return j.axisCount();
  }

  public int[] getAxisTypes(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    int[] ret = new int[NUM_JOYSTICK_AXES];
    for (int i = 0; i < NUM_JOYSTICK_AXES; i++) {
      ret[i] = j.axisTypes(i);
    }
    return ret;
  }

  public float[] getAxisValues(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    float[] ret = new float[NUM_JOYSTICK_AXES];
    for (int i = 0; i < NUM_JOYSTICK_AXES; i++) {
      ret[i] = j.axisValues(i);
    }
    return ret;
  }

  public int getPovCount(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    return j.povCount();
  }

  public int[] getPovValues(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    int[] ret = new int[NUM_JOYSTICK_POVS];
    for (int i = 0; i < NUM_JOYSTICK_POVS; i++) {
      ret[i] = j.povValues(i);
    }
    return ret;
  }

  public boolean isXbox(int joystickId) {
    Joystick j = new Joystick();
    inputs.joysticks(j, joystickId);

    return j.isXbox();
  }
}