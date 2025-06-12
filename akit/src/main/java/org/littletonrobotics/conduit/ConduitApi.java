// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.conduit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import org.littletonrobotics.conduit.schema.CANStatus;
import org.littletonrobotics.conduit.schema.CoreInputs;
import org.littletonrobotics.conduit.schema.DSData;
import org.littletonrobotics.conduit.schema.Joystick;
import org.littletonrobotics.conduit.schema.PDPData;
import org.littletonrobotics.conduit.schema.SystemData;

public class ConduitApi {
  // Length constants
  public static final int NUM_JOYSTICKS = 6;
  public static final int NUM_JOYSTICK_AXES = 12;
  public static final int NUM_JOYSTICK_POVS = 12;
  public static final int NUM_CAN_BUSES = 5;

  private static final byte[] eventNameBytes = new byte[64];
  private static final byte[] gameSpecificMessageBytes = new byte[64];
  private static final byte[] joystickNameBytes = new byte[256];
  private static final byte[] serialNumberBytes = new byte[8];
  private static final byte[] commentsBytes = new byte[64];

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
  private final CANStatus[] canStatus = new CANStatus[NUM_CAN_BUSES];

  private ConduitApi() {
    ConduitJni.start();
    ByteBuffer buffer = ConduitJni.getBuffer();
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    inputs.__init(0, buffer);
    ds = inputs.ds();
    pdp = inputs.pdp();
    sys = inputs.sys();
    for (int i = 0; i < NUM_JOYSTICKS; i++) {
      joysticks[i] = ds.joysticks(new Joystick(), i);
    }
    for (int i = 0; i < NUM_CAN_BUSES; i++) {
      canStatus[i] = sys.canStatus(new CANStatus(), i);
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
    int i;
    for (i = 0; i < eventNameBytes.length; i++) {
      eventNameBytes[i] = (byte) ds.eventName(i);
      if (eventNameBytes[i] == 0) break;
    }
    return new String(eventNameBytes, 0, i, utf8Charset);
  }

  public String getGameSpecificMessage() {
    int i;
    for (i = 0; i < gameSpecificMessageBytes.length; i++) {
      gameSpecificMessageBytes[i] = (byte) ds.gameSpecificMessage(i);
      if (gameSpecificMessageBytes[i] == 0) break;
    }
    return new String(gameSpecificMessageBytes, 0, i, utf8Charset);
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
    int i;
    for (i = 0; i < joystickNameBytes.length; i++) {
      joystickNameBytes[i] = (byte) joysticks[joystickId].name(i);
      if (joystickNameBytes[i] == 0) break;
    }

    return new String(joystickNameBytes, 0, i, utf8Charset);
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

  public boolean isGamepad(int joystickId) {
    return joysticks[joystickId].isGamepad();
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

  public int getFPGAVersion() {
    return sys.fpgaVersion();
  }

  public int getFPGARevision() {
    return sys.fpgaRevision();
  }

  public String getSerialNumber() {
    int i;
    for (i = 0; i < Math.min(serialNumberBytes.length, sys.serialNumberSize()); i++) {
      serialNumberBytes[i] = (byte) sys.serialNumber(i);
    }
    return new String(serialNumberBytes, 0, i, utf8Charset);
  }

  public String getComments() {
    int i;
    for (i = 0; i < Math.min(commentsBytes.length, sys.commentsSize()); i++) {
      commentsBytes[i] = (byte) sys.comments(i);
    }
    return new String(commentsBytes, 0, i, utf8Charset);
  }

  public int getTeamNumber() {
    return sys.teamNumber();
  }

  public boolean getSystemActive() {
    return sys.systemActive() != 0;
  }

  public boolean getBrownedOut() {
    return sys.brownedOut() != 0;
  }

  public int getCommsDisableCount() {
    return sys.commsDisableCount();
  }

  public boolean getRSLState() {
    return sys.rslState() != 0;
  }

  public boolean getSystemTimeValid() {
    return sys.systemTimeValid() != 0;
  }

  public double getVoltageVin() {
    return sys.voltageVin();
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

  public double getBrownoutVoltage() {
    return sys.brownoutVoltage();
  }

  public double getCPUTemp() {
    return sys.cpuTemp();
  }

  public long getEpochTime() {
    return sys.epochTime();
  }

  public float getCANBusUtilization(int busId) {
    return canStatus[busId].percentBusUtilization();
  }

  public long getBusOffCount(int busId) {
    return canStatus[busId].busOffCount();
  }

  public long getTxFullCount(int busId) {
    return canStatus[busId].txFullCount();
  }

  public long getReceiveErrorCount(int busId) {
    return canStatus[busId].receiveErrorCount();
  }

  public long getTransmitErrorCount(int busId) {
    return canStatus[busId].transmitErrorCount();
  }

  public void configurePowerDistribution(int busID, int moduleID, int type) {
    ConduitJni.configurePowerDistribution(busID, moduleID, type);
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
