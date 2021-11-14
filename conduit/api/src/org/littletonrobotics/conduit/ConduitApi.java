package org.littletonrobotics.conduit;

public class ConduitApi {
  private static ConduitApi instance = null;

  public static ConduitApi getInstance() {
    if (instance == null) {
      instance = new ConduitApi();
    }
    return instance;
  }

  private ConduitApi() {
  }

  long getTimestamp() {
    return 0;
  };

  int getAllianceStation() {
    return 0;
  };

  String getEventName() {
    return "";
  };

  String getEventSpecificMessage() {
    return "";
  };

  int getGameSpecificMessageSize() {
    return 0;
  };

  int getMatchNumber() {
    return 0;
  };

  int getReplayNumber() {
    return 0;
  };

  int getMatchType() {
    return 0;
  };

  int getControlWord() {
    return 0;
  };

  double getMatchTime() {
    return 0.0;
  };

  String getJoystickName(int joystickId) {
    return "";
  };

  int getJoystickType(int joystickId) {
    return 0;
  };

  int getButtonCount() {
    return 0;
  };

  int getButtonValues() {
    return 0;
  };

  int getAxisCount(int joystickId) {
    return 0;
  };

  int[] getAxisTypes(int joystickId) {
    return new int[] {};
  };

  float[] getAxisValues(int joystickId) {
    return new float[] {};
  };

  int getPovCount(int joystickId) {
    return 0;
  };

  int[] getPovValues(int joystickId) {
    return new int[] {};
  };
}