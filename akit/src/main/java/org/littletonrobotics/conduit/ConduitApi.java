// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.conduit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import org.littletonrobotics.conduit.schema.CANInfo;
import org.littletonrobotics.conduit.schema.CoreInputs;
import org.littletonrobotics.conduit.schema.DSData;
import org.littletonrobotics.conduit.schema.Joystick;
import org.littletonrobotics.conduit.schema.NetworkStatus;
import org.littletonrobotics.conduit.schema.PDPData;
import org.littletonrobotics.conduit.schema.SystemData;
import org.littletonrobotics.conduit.schema.Vector3;
import org.wpilib.math.geometry.Quaternion;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.geometry.Rotation3d;

public class ConduitApi {
  // Length constants
  public static final int NUM_JOYSTICKS = 6;
  public static final int NUM_CAN_BUSES = 5;

  private static final byte[] eventNameBytes = new byte[64];
  private static final byte[] gameDataBytes = new byte[9];
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
      if (eventNameBytes[i] == 0) {
        break;
      }
    }
    return new String(eventNameBytes, 0, i, utf8Charset);
  }

  public String getGameData() {
    int i;
    for (i = 0; i < gameDataBytes.length; i++) {
      gameDataBytes[i] = (byte) ds.gameData(i);
      if (gameDataBytes[i] == 0) {
        break;
      }
    }
    return new String(gameDataBytes, 0, i, utf8Charset);
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

  public long getControlWord() {
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

  public int getJoystickSupportedOutputs(int joystickId) {
    return joysticks[joystickId].supportedOutputs();
  }

  public long getButtonsAvailable(int joystickId) {
    return joysticks[joystickId].buttonsAvailable();
  }

  public long getButtonValues(int joystickId) {
    return joysticks[joystickId].buttons();
  }

  public int getAxesAvailable(int joystickId) {
    return joysticks[joystickId].axesAvailable();
  }

  public float[] getAxisValues(int joystickId) {
    int count = availableToCount(getAxesAvailable(joystickId));
    float[] ret = new float[count];
    for (int i = 0; i < count; i++) {
      ret[i] = joysticks[joystickId].axisValues(i);
    }
    return ret;
  }

  public int[] getAxisValuesRaw(int joystickId) {
    int count = availableToCount(getAxesAvailable(joystickId));
    int[] ret = new int[count];
    for (int i = 0; i < count; i++) {
      ret[i] = (int) joysticks[joystickId].axisRaw(i);
    }
    return ret;
  }

  public int getPovsAvailable(int joystickId) {
    return joysticks[joystickId].povsAvailable();
  }

  public int[] getPovValues(int joystickId) {
    int count = availableToCount(getPovsAvailable(joystickId));
    int[] ret = new int[count];
    for (int i = 0; i < count; i++) {
      ret[i] = joysticks[joystickId].povValues(i);
    }
    return ret;
  }

  public boolean isGamepad(int joystickId) {
    return joysticks[joystickId].isGamepad();
  }

  public int getTouchpadCount(int joystickId) {
    return joysticks[joystickId].touchpadCount();
  }

  public int getTouchpadFingerCount(int joystickId, int touchpadId) {
    return joysticks[joystickId].touchpads(touchpadId).fingerCount();
  }

  public boolean getTouchpadFingerDown(int joystickId, int touchpadId, int fingerId) {
    return joysticks[joystickId].touchpads(touchpadId).fingers(fingerId).down() != 0;
  }

  public float getTouchpadFingerX(int joystickId, int touchpadId, int fingerId) {
    return joysticks[joystickId].touchpads(touchpadId).fingers(fingerId).x();
  }

  public float getTouchpadFingerY(int joystickId, int touchpadId, int fingerId) {
    return joysticks[joystickId].touchpads(touchpadId).fingers(fingerId).y();
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

  public CANInfo getNetworkCANInfo(int bus) {
    return sys.networkCanInfo(bus);
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

  private static int availableToCount(long available) {
    // Top bit has to be set
    if (available < 0) {
      return 64;
    }

    int count = 0;

    // Top bit not set, we will eventually get a 0 bit
    while ((available & 0x1) != 0) {
      count++;
      available >>= 1;
    }
    return count;
  }
}
