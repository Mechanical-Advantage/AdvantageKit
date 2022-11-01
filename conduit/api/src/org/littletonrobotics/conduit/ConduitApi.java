package org.littletonrobotics.conduit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.littletonrobotics.conduit.schema.CoreInputs;
import org.littletonrobotics.conduit.schema.DSData;
import org.littletonrobotics.conduit.schema.Joystick;
import org.littletonrobotics.conduit.schema.PDPData;
import org.littletonrobotics.conduit.schema.SystemData;

public class ConduitApi {
  // Length constants
  private static final int NUM_JOYSTICKS = 6;
  private static final int NUM_JOYSTICK_AXES = 12;
  private static final int NUM_JOYSTICK_POVS = 12;
  private static final int JOYSTICK_NAME_LEN = 256;
  private static final int EVENT_NAME_LEN = 64;
  private static final int GAME_SPECIFIC_MESSAGE_LEN = 64;

  private static ConduitApi instance = null;

  private static final Charset utf8Charset = Charset.forName("UTF-8");

  public static ConduitApi getInstance() {
    if (instance == null) {
      instance = new ConduitApi();
    }
    return instance;
  }

  private final CoreInputs inputs = new CoreInputs();
  private final DSData ds;
  private final PDPData pdp;
  private final SystemData sys;
  private final Joystick[] joysticks = new Joystick[NUM_JOYSTICKS];

  private ConduitApi() {
    ByteBuffer buffer = ConduitJni.getBuffer();
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    inputs.__init(0, buffer);
    ds = inputs.ds();
    pdp = inputs.pdp();
    sys = inputs.sys();
    for (int i = 0; i < NUM_JOYSTICKS; i++) {
      joysticks[i] = ds.joysticks(new Joystick(), i);
    }
  }

  public void captureData() {
    ConduitJni.capture();
  }

  public long getTimestamp() {
    return inputs.timestamp();
  }

  public int getAllianceStation() {
    return ds.allianceStation();
  }

  public String getEventName() {
    byte[] bytes = new byte[EVENT_NAME_LEN];
    int i;
    for (i = 0; i < EVENT_NAME_LEN; i++) {
      bytes[i] = (byte) ds.eventName(i);
      if (bytes[i] == 0)
        break;
    }
    return new String(bytes, 0, i, utf8Charset);
  }

  public String getGameSpecificMessage() {
    byte[] bytes = new byte[GAME_SPECIFIC_MESSAGE_LEN];
    int i;
    for (i = 0; i < GAME_SPECIFIC_MESSAGE_LEN; i++) {
      bytes[i] = (byte) ds.gameSpecificMessage(i);
      if (bytes[i] == 0)
        break;
    }
    return new String(bytes, 0, i, utf8Charset);
  }

  public int getGameSpecificMessageSize() {
    return ds.gameSpecificMessageSize();
  }

  public int getMatchNumber() {
    return ds.matchNumber();
  }

  public int getReplayNumber() {
    return ds.replayNumber();
  }

  public int getMatchType() {
    return ds.matchType();
  }

  public int getControlWord() {
    return ds.controlWord();
  }

  public double getMatchTime() {
    return ds.matchTime();
  }

  public String getJoystickName(int joystickId) {
    byte[] bytes = new byte[JOYSTICK_NAME_LEN];
    int i;
    for (i = 0; i < JOYSTICK_NAME_LEN; i++) {
      bytes[i] = (byte) joysticks[joystickId].name(i);
      if (bytes[i] == 0)
        break;
    }

    return new String(bytes, 0, i, utf8Charset);
  }

  public int getJoystickType(int joystickId) {
    return joysticks[joystickId].type();
  }

  public int getButtonCount(int joystickId) {
    return joysticks[joystickId].buttonCount();
  }

  public int getButtonValues(int joystickId) {
    return joysticks[joystickId].buttons();
  }

  public int getAxisCount(int joystickId) {
    return joysticks[joystickId].axisCount();
  }

  public int[] getAxisTypes(int joystickId) {
    int[] ret = new int[NUM_JOYSTICK_AXES];
    for (int i = 0; i < NUM_JOYSTICK_AXES; i++) {
      ret[i] = joysticks[joystickId].axisTypes(i);
    }
    return ret;
  }

  public float[] getAxisValues(int joystickId) {
    float[] ret = new float[NUM_JOYSTICK_AXES];
    for (int i = 0; i < NUM_JOYSTICK_AXES; i++) {
      ret[i] = joysticks[joystickId].axisValues(i);
    }
    return ret;
  }

  public int getPovCount(int joystickId) {
    return joysticks[joystickId].povCount();
  }

  public int[] getPovValues(int joystickId) {
    int[] ret = new int[NUM_JOYSTICK_POVS];
    for (int i = 0; i < NUM_JOYSTICK_POVS; i++) {
      ret[i] = joysticks[joystickId].povValues(i);
    }
    return ret;
  }

  public boolean isXbox(int joystickId) {
    return joysticks[joystickId].isXbox();
  }

  public double getPDPTemperature() {
    return pdp.temperature();
  }

  public double getPDPVoltage() {
    return pdp.voltage();
  }

  public double getPDPChannelCurrent(int channel) {
    return pdp.channelCurrent(channel);
  }

  public double getPDPTotalCurrent() {
    return pdp.totalCurrent();
  }

  public double getPDPTotalPower() {
    return pdp.totalPower();
  }

  public double getPDPTotalEnergy() {
    return pdp.totalEnergy();
  }

  public double getVoltageVin() {
    return sys.voltageVin();
  }

  public double getCurrentVin() {
    return sys.currentVin();
  }

  public double getUserVoltage3v3() {
    return sys.userVoltage3v3();
  }

  public double getUserCurrent3v3() {
    return sys.userCurrent3v3();
  }

  public boolean getUserActive3v3() {
    return sys.userActive3v3() != 0;
  }

  public int getUserCurrentFaults3v3() {
    return sys.userCurrentFaults3v3();
  }

  public double getUserVoltage5v() {
    return sys.userVoltage5v();
  }

  public double getUserCurrent5v() {
    return sys.userCurrent5v();
  }

  public boolean getUserActive5v() {
    return sys.userActive5v() != 0;
  }

  public int getUserCurrentFaults5v() {
    return sys.userCurrentFaults5v();
  }

  public double getUserVoltage6v() {
    return sys.userVoltage6v();
  }

  public double getUserCurrent6v() {
    return sys.userCurrent6v();
  }

  public boolean getUserActive6v() {
    return sys.userActive6v() != 0;
  }

  public int getUserCurrentFaults6v() {
    return sys.userCurrentFaults6v();
  }

  public boolean getBrownedOut() {
    return sys.brownedOut() != 0;
  }

  public boolean getSystemActive() {
    return sys.systemActive() != 0;
  }

  public float getCANBusUtilization() {
    return sys.canStatus().percentBusUtilization();
  }

  public long getBusOffCount() {
    return sys.canStatus().busOffCount();
  }

  public long getTxFullCount() {
    return sys.canStatus().txFullCount();
  }

  public long getReceiveErrorCount() {
    return sys.canStatus().receiveErrorCount();
  }

  public long getTransmitErrorCount() {
    return sys.canStatus().transmitErrorCount();
  }

  public long getEpochTime() {
    return sys.epochTime();
  }

  public void configurePowerDistribution(int moduleID, int type) {
    ConduitJni.configurePowerDistribution(moduleID, type);
  }

  public int getPDPChannelCount() {
    return pdp.channelCount();
  }

  public int getPDPHandle() {
    return pdp.handle();
  }

  public int getPDPType() {
    return pdp.type();
  }

  public int getPDPModuleId() {
    return pdp.moduleId();
  }

  public long getPDPFaults() {
    return pdp.faults();
  }

  public long getPDPStickyFaults() {
    return pdp.stickyFaults();
  }
}