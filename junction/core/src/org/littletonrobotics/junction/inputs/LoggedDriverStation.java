package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.conduit.ConduitApi;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringPublisher;

/**
 * Manages logging and replaying data from the driver station (robot state,
 * joysticks, etc.)
 */
public class LoggedDriverStation {

  private static LoggedDriverStation instance;
  private static final Logger logger = Logger.getInstance();

  private final DriverStationInputs dsInputs = new DriverStationInputs();
  private final JoystickInputs[] joystickInputs = { new JoystickInputs(), new JoystickInputs(), new JoystickInputs(),
      new JoystickInputs(), new JoystickInputs(), new JoystickInputs() };
  private final MatchDataSender matchDataSender = new MatchDataSender();

  private LoggedDriverStation() {
  }

  public static LoggedDriverStation getInstance() {
    if (instance == null) {
      instance = new LoggedDriverStation();
    }
    return instance;
  }

  /**
   * General driver station data that needs to be updated throughout the match.
   */
  public static class DriverStationInputs implements LoggableInputs {
    public long allianceStation = 0;
    public String eventName = "";
    public String gameSpecificMessage = "";
    public long matchNumber = 0;
    public long replayNumber = 0;
    public long matchType = 0;
    public double matchTime = 0.00;

    public boolean enabled = false;
    public boolean autonomous = false;
    public boolean test = false;
    public boolean emergencyStop = false;
    public boolean fmsAttached = false;
    public boolean dsAttached = false;

    public void toLog(LogTable table) {
      table.put("AllianceStation", allianceStation);
      table.put("EventName", eventName);
      table.put("GameSpecificMessage", gameSpecificMessage);
      table.put("MatchNumber", matchNumber);
      table.put("ReplayNumber", replayNumber);
      table.put("MatchType", matchType);
      table.put("MatchTime", matchTime);

      table.put("Enabled", enabled);
      table.put("Autonomous", autonomous);
      table.put("Test", test);
      table.put("EmergencyStop", emergencyStop);
      table.put("FMSAttached", fmsAttached);
      table.put("DSAttached", dsAttached);
    }

    public void fromLog(LogTable table) {
      allianceStation = table.get("AllianceStation", allianceStation);
      eventName = table.get("EventName", eventName);
      gameSpecificMessage = table.get("GameSpecificMessage", gameSpecificMessage);
      matchNumber = table.get("MatchNumber", matchNumber);
      replayNumber = table.get("ReplayNumber", replayNumber);
      matchType = table.get("MatchType", matchType);
      matchTime = table.get("MatchTime", matchTime);

      enabled = table.get("Enabled", enabled);
      autonomous = table.get("Autonomous", autonomous);
      test = table.get("Test", test);
      emergencyStop = table.get("EmergencyStop", emergencyStop);
      fmsAttached = table.get("FMSAttached", fmsAttached);
      dsAttached = table.get("DSAttached", dsAttached);
    }
  }

  /**
   * All of the required inputs for a single joystick.
   */
  public static class JoystickInputs implements LoggableInputs {
    public String name = "";
    public long type = 0;
    public boolean xbox = false;
    public long buttonCount = 0;
    public long buttonValues = 0;
    public long[] povs = {};
    public float[] axisValues = {};
    public long[] axisTypes = {};

    public void toLog(LogTable table) {
      table.put("Name", name);
      table.put("Type", type);
      table.put("Xbox", xbox);
      table.put("ButtonCount", buttonCount);
      table.put("ButtonValues", buttonValues);
      table.put("POVs", povs);
      table.put("AxisValues", axisValues);
      table.put("AxisTypes", axisTypes);
    }

    public void fromLog(LogTable table) {
      name = table.get("Name", name);
      type = table.get("Type", type);
      xbox = table.get("Xbox", xbox);
      buttonCount = table.get("ButtonCount", buttonCount);
      buttonValues = table.get("ButtonValues", buttonValues);
      povs = table.get("POVs", povs);
      axisValues = table.get("AxisValues", axisValues);
      axisTypes = table.get("AxisTypes", axisTypes);
    }
  }

  /**
   * Records inputs from the real driver station via conduit
   */
  public void periodic() {
    // Update inputs from conduit
    if (!logger.hasReplaySource()) {
      ConduitApi conduit = ConduitApi.getInstance();

      dsInputs.allianceStation = conduit.getAllianceStation();
      dsInputs.eventName = conduit.getEventName().trim();
      dsInputs.gameSpecificMessage = conduit.getGameSpecificMessage().trim();
      dsInputs.matchNumber = conduit.getMatchNumber();
      dsInputs.replayNumber = conduit.getReplayNumber();
      dsInputs.matchType = conduit.getMatchType();
      dsInputs.matchTime = conduit.getMatchTime();

      int controlWord = conduit.getControlWord();
      dsInputs.enabled = (controlWord & 1) != 0;
      dsInputs.autonomous = (controlWord & 2) != 0;
      dsInputs.test = (controlWord & 4) != 0;
      dsInputs.emergencyStop = (controlWord & 8) != 0;
      dsInputs.fmsAttached = (controlWord & 16) != 0;
      dsInputs.dsAttached = (controlWord & 32) != 0;

      for (int id = 0; id < joystickInputs.length; id++) {
        JoystickInputs joystick = joystickInputs[id];
        joystick.name = conduit.getJoystickName(id).trim();
        joystick.type = conduit.getJoystickType(id);
        joystick.xbox = conduit.isXbox(id);
        joystick.buttonCount = conduit.getButtonCount(id);
        joystick.buttonValues = conduit.getButtonValues(id);

        // POVs
        int povCount = conduit.getPovCount(id);
        int[] povValues = conduit.getPovValues(id);
        joystick.povs = new long[povCount];
        for (int i = 0; i < povCount; i++) {
          joystick.povs[i] = povValues[i];
        }

        // Axes
        int axisCount = conduit.getAxisCount(id);
        float[] axisValues = conduit.getAxisValues(id);
        int[] axisTypes = conduit.getAxisTypes(id);
        joystick.axisValues = new float[axisCount];
        joystick.axisTypes = new long[axisCount];
        for (int i = 0; i < axisCount; i++) {
          joystick.axisValues[i] = axisValues[i];
          joystick.axisTypes[i] = axisTypes[i];
        }
      }
    }

    // Send/receive log data
    logger.processInputs("DriverStation", dsInputs);
    for (int id = 0; id < joystickInputs.length; id++) {
      logger.processInputs("DriverStation/Joystick" + Integer.toString(id), joystickInputs[id]);
    }

    // Update FMSInfo table
    matchDataSender.sendMatchData(dsInputs);
  }

  /**
   * Returns a reference to an object containing all driver station data other
   * than joysticks.
   */
  public DriverStationInputs getDSData() {
    return dsInputs;
  }

  /**
   * Returns a reference to an object containing data for a single joystick.
   * 
   * @param id ID of the joystick to read(0-6)
   */
  public JoystickInputs getJoystickData(int id) {
    return joystickInputs[id];
  }

  /**
   * Class for updating the "FMSInfo" table in NetworkTables, modified from the
   * original DriverStation.
   */
  private static class MatchDataSender {
    NetworkTable table;
    StringPublisher typeMetadata;
    StringPublisher gameSpecificMessage;
    StringPublisher eventName;
    IntegerPublisher matchNumber;
    IntegerPublisher replayNumber;
    IntegerPublisher matchType;
    BooleanPublisher alliance;
    IntegerPublisher station;
    IntegerPublisher controlWord;
    boolean oldIsRedAlliance = true;
    long oldStationNumber = 1;
    String oldEventName = "";
    String oldGameSpecificMessage = "";
    long oldMatchNumber;
    long oldReplayNumber;
    long oldMatchType;
    long oldControlWord;

    MatchDataSender() {
      table = NetworkTableInstance.getDefault().getTable("FMSInfo");
      typeMetadata = table.getStringTopic(".type").publish();
      typeMetadata.set("FMSInfo");
      gameSpecificMessage = table.getStringTopic("GameSpecificMessage").publish();
      gameSpecificMessage.set("");
      eventName = table.getStringTopic("EventName").publish();
      eventName.set("");
      matchNumber = table.getIntegerTopic("MatchNumber").publish();
      matchNumber.set(0);
      replayNumber = table.getIntegerTopic("ReplayNumber").publish();
      replayNumber.set(0);
      matchType = table.getIntegerTopic("MatchType").publish();
      matchType.set(0);
      alliance = table.getBooleanTopic("IsRedAlliance").publish();
      alliance.set(true);
      station = table.getIntegerTopic("StationNumber").publish();
      station.set(1);
      controlWord = table.getIntegerTopic("FMSControlData").publish();
      controlWord.set(0);
    }

    private void sendMatchData(DriverStationInputs dsInputs) {
      boolean isRedAlliance = false;
      int stationNumber = 1;
      switch ((int) dsInputs.allianceStation) {
        case 0:
          isRedAlliance = true;
          stationNumber = 1;
          break;
        case 1:
          isRedAlliance = true;
          stationNumber = 2;
          break;
        case 2:
          isRedAlliance = true;
          stationNumber = 3;
          break;
        case 3:
          isRedAlliance = false;
          stationNumber = 1;
          break;
        case 4:
          isRedAlliance = false;
          stationNumber = 2;
          break;
        case 5:
          isRedAlliance = false;
          stationNumber = 3;
          break;
      }

      String currentEventName = dsInputs.eventName;
      String currentGameSpecificMessage = dsInputs.gameSpecificMessage;
      long currentMatchNumber = dsInputs.matchNumber;
      long currentReplayNumber = dsInputs.replayNumber;
      long currentMatchType = dsInputs.matchType;
      long currentControlWord = 0;
      currentControlWord += dsInputs.enabled ? 1 : 0;
      currentControlWord += dsInputs.autonomous ? 2 : 0;
      currentControlWord += dsInputs.test ? 4 : 0;
      currentControlWord += dsInputs.emergencyStop ? 8 : 0;
      currentControlWord += dsInputs.fmsAttached ? 16 : 0;
      currentControlWord += dsInputs.dsAttached ? 32 : 0;

      if (oldIsRedAlliance != isRedAlliance) {
        alliance.set(isRedAlliance);
        oldIsRedAlliance = isRedAlliance;
      }
      if (oldStationNumber != stationNumber) {
        station.set(stationNumber);
        oldStationNumber = stationNumber;
      }
      if (!oldEventName.equals(currentEventName)) {
        eventName.set(currentEventName);
        oldEventName = currentEventName;
      }
      if (!oldGameSpecificMessage.equals(currentGameSpecificMessage)) {
        gameSpecificMessage.set(currentGameSpecificMessage);
        oldGameSpecificMessage = currentGameSpecificMessage;
      }
      if (currentMatchNumber != oldMatchNumber) {
        matchNumber.set(currentMatchNumber);
        oldMatchNumber = currentMatchNumber;
      }
      if (currentReplayNumber != oldReplayNumber) {
        replayNumber.set(currentReplayNumber);
        oldReplayNumber = currentReplayNumber;
      }
      if (currentMatchType != oldMatchType) {
        matchType.set(currentMatchType);
        oldMatchType = currentMatchType;
      }
      if (currentControlWord != oldControlWord) {
        controlWord.set(currentControlWord);
        oldControlWord = currentControlWord;
      }
    }
  }
}
