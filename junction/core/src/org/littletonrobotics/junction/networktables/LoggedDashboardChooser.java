package org.littletonrobotics.junction.networktables;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class LoggedDashboardChooser<V> implements LoggedDashboardInput {
  private final String key;
  private String selectedValue = null;
  private SendableChooser<String> sendableChooser = new SendableChooser<>();
  private Map<String, V> options = new HashMap<>();

  private final LoggableInputs inputs = new LoggableInputs() {
    public void toLog(LogTable table) {
      table.put(key, selectedValue);
    }

    public void fromLog(LogTable table) {
      selectedValue = table.get(key, selectedValue);
    }
  };

  /**
   * Creates a new LoggedDashboardChooser, for handling a chooser input sent via
   * NetworkTables.
   *
   * @param key The key for the chooser, published to "/SmartDashboard/{key}" for
   *            NT or
   *            "/DashboardInputs/{key}" when logged.
   */
  public LoggedDashboardChooser(String key) {
    this.key = key;
    SmartDashboard.putData(key, sendableChooser);
    periodic();
    Logger.registerDashboardInput(this);
  }

  /**
   * Creates a new LoggedDashboardChooser, for handling a chooser input sent via
   * NetworkTables. This constructor copies the options from a SendableChooser.
   * Note that updates to the original SendableChooser will not affect this
   * object.
   *
   * @param key The key for the chooser, published to "/SmartDashboard/{key}" for
   *            NT or "/DashboardInputs/{key}" when logged.
   */
  @SuppressWarnings("unchecked")
  public LoggedDashboardChooser(String key, SendableChooser<V> chooser) {
    this(key);

    // Get options map
    Map<String, V> options = new HashMap<>();
    try {
      Field mapField = SendableChooser.class.getDeclaredField("m_map");
      mapField.setAccessible(true);
      options = (Map<String, V>) mapField.get(chooser);
    } catch (NoSuchFieldException
        | SecurityException
        | IllegalArgumentException
        | IllegalAccessException e) {
      e.printStackTrace();
    }

    // Get default option
    String defaultString = "";
    try {
      Field defaultField = SendableChooser.class.getDeclaredField("m_defaultChoice");
      defaultField.setAccessible(true);
      defaultString = (String) defaultField.get(chooser);
    } catch (NoSuchFieldException
        | SecurityException
        | IllegalArgumentException
        | IllegalAccessException e) {
      e.printStackTrace();
    }

    // Add options
    for (String optionKey : options.keySet()) {
      if (optionKey.equals(defaultString)) {
        addDefaultOption(optionKey, options.get(optionKey));
      } else {
        addOption(optionKey, options.get(optionKey));
      }
    }
  }

  /** Adds a new option to the chooser. */
  public void addOption(String key, V value) {
    sendableChooser.addOption(key, key);
    options.put(key, value);
  }

  /** Adds a new option to the chooser and sets it to the default. */
  public void addDefaultOption(String key, V value) {
    sendableChooser.setDefaultOption(key, key);
    options.put(key, value);
  }

  /**
   * Returns the selected option. If there is none selected, it will return the
   * default. If there is none selected and no default, then it will return
   * {@code null}.
   */
  public V get() {
    return options.get(selectedValue);
  }

  /**
   * Returns the internal sendable chooser object, for use when setting up
   * dashboard layouts. Do not read data from the sendable chooser directly.
   */
  public SendableChooser<String> getSendableChooser() {
    return sendableChooser;
  }

  public void periodic() {
    if (!Logger.hasReplaySource()) {
      selectedValue = sendableChooser.getSelected();
    }
    Logger.processInputs(prefix, inputs);
  }
}