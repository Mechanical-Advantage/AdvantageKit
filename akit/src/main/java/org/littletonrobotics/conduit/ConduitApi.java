// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.conduit;

import edu.wpi.first.math.geometry.Quaternion;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import org.littletonrobotics.conduit.schema.CoreInputs;
import org.littletonrobotics.conduit.schema.DSData;
import org.littletonrobotics.conduit.schema.Joystick;
import org.littletonrobotics.conduit.schema.NetworkStatus;
import org.littletonrobotics.conduit.schema.PDPData;
import org.littletonrobotics.conduit.schema.SystemData;
import org.littletonrobotics.conduit.schema.Vector3;

public class ConduitApi {
  // Length constants
  public static final int NUM_JOYSTICKS = 6;
  public static final int NUM_JOYSTICK_AXES = 12;
  public static final int NUM_JOYSTICK_POVS = 12;
  public static final int NUM_CAN_BUSES = 5;

  private static final byte[] eventNameBytes = new byte[64];
  private static final byte[] gameSpecificMessageBytes = new byte[64];
  private static final byte[] joystickNameBytes = new byte[256];

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

  public double getBatteryVoltage() {
    return sys.batteryVoltage();
  }

  public boolean getWatchdogActive() {
    return sys.watchdogActive();
  }

  public long getIOFrequency() {
    return sys.ioFrequency();
  }

  public long getTeamNumber() {
    return sys.teamNumber();
  }

  public long getEpochTime() {
    return sys.epochTime();
  }

  public boolean getEpochTimeValid() {
    return sys.epochTimeValid();
  }

  public NetworkStatus getNetworkEthernet() {
    return sys.networkEthernet();
  }

  public NetworkStatus getNetworkWiFi() {
    return sys.networkWifi();
  }

  public NetworkStatus getNetworkUSBTether() {
    return sys.networkUsbTether();
  }

  public NetworkStatus getNetworkCAN(int bus) {
    return sys.networkCan(bus);
  }

  public double getCPUPercent() {
    return sys.cpuPercent();
  }

  public double getCPUTempCelcius() {
    return sys.cpuTemp();
  }

  public long getMemoryUsageBytes() {
    return sys.memoryUsageBytes();
  }

  public long getMemoryTotalBytes() {
    return sys.memoryTotalBytes();
  }

  public double getMemoryPercent() {
    return sys.memoryPercent();
  }

  public long getStorageUsageBytes() {
    return sys.storageUsageBytes();
  }

  public long getStorageTotalBytes() {
    return sys.storageTotalBytes();
  }

  public double getStoragePercent() {
    return sys.storagePercent();
  }

  public Vector3 getIMUAccelRaw() {
    return sys.imuAccelRaw();
  }

  public Vector3 getIMUGyroRates() {
    return sys.imuGyroRates();
  }

  public Vector3 getIMUGyroEulerFlat() {
    return sys.imuGyroEulerFlat();
  }

  public Vector3 getIMUGyroEulerLandscape() {
    return sys.imuGyroEulerLandscape();
  }

  public Vector3 getIMUGyroEulerPortrait() {
    return sys.imuGyroEulerPortrait();
  }

  public Rotation3d getIMUGyroRotation3d() {
    return new Rotation3d(
        new Quaternion(
            sys.imuGyroQuaternion().w(),
            sys.imuGyroQuaternion().x(),
            sys.imuGyroQuaternion().y(),
            sys.imuGyroQuaternion().z()));
  }

  public Rotation2d getIMUGyroYawFlat() {
    return new Rotation2d(sys.imuGyroYawFlat());
  }

  public Rotation2d getIMUGyroYawLandscape() {
    return new Rotation2d(sys.imuGyroYawLandscape());
  }

  public Rotation2d getIMUGyroYawPortrait() {
    return new Rotation2d(sys.imuGyroYawPortrait());
  }
}
