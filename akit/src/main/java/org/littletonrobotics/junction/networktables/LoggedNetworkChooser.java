// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction.networktables;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.inputs.LoggableInputs;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.PubSubOption;
import org.wpilib.networktables.StringArrayPublisher;
import org.wpilib.networktables.StringPublisher;
import org.wpilib.networktables.StringSubscriber;
import org.wpilib.tunable.Selectable;
import org.wpilib.tunable.Tunable;

/**
 * Manages a chooser value published to NetworkTables.
 *
 * @param <V> The value type associated with each string key.
 */
public class LoggedNetworkChooser<V> extends LoggedNetworkInput implements Supplier<V> {
  private static final String TYPE = ".type";
  private static final String DEFAULT = "default";
  private static final String OPTIONS = "options";
  private static final String SELECTED = "selected";
  private static final String TUNE = "tune";
  private static final String VALUE = "value";

  private final String key;
  private final Map<String, V> options = new LinkedHashMap<>();
  private String defaultChoice = "";
  private String selectedValue = null;
  private String previousValue = null;
  private Consumer<V> listener = null;

  private final StringPublisher typePublisher;
  private final StringPublisher defaultPublisher;
  private final StringArrayPublisher optionsPublisher;
  private final StringPublisher activePublisher;
  private final StringSubscriber selectedSubscriber;

  private final LoggableInputs inputs =
      new LoggableInputs() {
        public void toLog(LogTable table) {
          table.put(removeSlash(key), selectedValue != null ? selectedValue : "");
        }

        public void fromLog(LogTable table) {
          selectedValue = table.get(removeSlash(key), defaultChoice);
        }
      };

  /**
   * Creates a new LoggedNetworkChooser, for handling a chooser input sent via NetworkTables.
   *
   * @param key The key for the chooser, published to NT or "/NetworkInputs/{key}" when logged.
   */
  public LoggedNetworkChooser(String key) {
    this.key = key;
    var inst = NetworkTableInstance.getDefault();
    var topicPath = key.startsWith("/") ? key : "/" + key;

    typePublisher =
        inst.getStringTopic(topicPath + "/" + TYPE).publishEx("string", "{\"mutable\":true}");
    typePublisher.set("Selectable");

    defaultPublisher =
        inst.getStringTopic(topicPath + "/" + DEFAULT).publishEx("string", "{\"mutable\":false}");
    defaultPublisher.set(defaultChoice);

    optionsPublisher =
        inst.getStringArrayTopic(topicPath + "/" + OPTIONS)
            .publishEx("string[]", "{\"mutable\":false}");
    optionsPublisher.set(new String[0]);

    activePublisher =
        inst.getStringTopic(topicPath + "/" + SELECTED + "/" + VALUE)
            .publishEx("string", "{\"mutable\":true}");
    activePublisher.set("");

    selectedSubscriber =
        inst.getStringTopic(topicPath + "/" + SELECTED + "/" + TUNE)
            .subscribe("", PubSubOption.excludePublisher(activePublisher));

    periodic();
    Logger.registerDashboardInput(this);
  }

  /**
   * Creates a new LoggedNetworkChooser, for handling a chooser input sent via NetworkTables. This
   * constructor copies the options from a {@link Selectable}. Note that updates to the original
   * Selectable will not affect this object.
   *
   * @param key The key for the chooser, published to NT or "/NetworkInputs/{key}" when logged.
   * @param selectable The existing Selectable object.
   */
  @SuppressWarnings("unchecked")
  public LoggedNetworkChooser(String key, Selectable<V> selectable) {
    this(key);

    // Get options map
    Map<String, V> options = new HashMap<>();
    try {
      Field mapField = Selectable.class.getDeclaredField("m_map");
      mapField.setAccessible(true);
      options = (Map<String, V>) mapField.get(selectable);
    } catch (NoSuchFieldException
        | SecurityException
        | IllegalArgumentException
        | IllegalAccessException e) {
      e.printStackTrace();
    }

    // Get default option
    String defaultString = "";
    try {
      Field defaultField = Selectable.class.getDeclaredField("m_defaultChoice");
      defaultField.setAccessible(true);
      Tunable<String> defaultTunable = (Tunable<String>) defaultField.get(selectable);
      defaultString = defaultTunable.get();
    } catch (NoSuchFieldException
        | SecurityException
        | IllegalArgumentException
        | IllegalAccessException e) {
      e.printStackTrace();
    }

    // Add options
    for (String optionKey : options.keySet()) {
      if (optionKey.equals(defaultString)) {
        addDefault(optionKey, options.get(optionKey));
      } else {
        add(optionKey, options.get(optionKey));
      }
    }
  }

  /**
   * Adds the given object to the list of options.
   *
   * @param name The name of the option.
   * @param object The option object.
   */
  public void add(String name, V object) {
    options.put(name, object);
    optionsPublisher.set(options.keySet().toArray(new String[0]));
    if (selectedValue == null) {
      selectedValue = defaultChoice;
    }
  }

  /**
   * Adds the given object to the list of options and marks it as the default.
   *
   * @param name The name of the option.
   * @param object The option object.
   */
  public void addDefault(String name, V object) {
    add(name, object);
    setDefault(name);
  }

  /**
   * Removes the option with the given name. If the removed option is the default, the default is
   * reset.
   *
   * @param name The name of the option.
   */
  public void remove(String name) {
    if (options.containsKey(name)) {
      options.remove(name);
      optionsPublisher.set(options.keySet().toArray(new String[0]));
      if (name.equals(defaultChoice)) {
        setDefault("");
      }
    }
  }

  /**
   * Marks the given option as the default.
   *
   * @param name The name of the option.
   */
  public void setDefault(String name) {
    defaultChoice = name != null ? name : "";
    defaultPublisher.set(defaultChoice);
  }

  /** Clears the list of options and resets the default. */
  public void clear() {
    options.clear();
    optionsPublisher.set(new String[0]);
    setDefault("");
  }

  /**
   * Returns the selected option. If there is none selected, it will return the default. If there is
   * none or invalid option selected and no default, then it will return {@code null}.
   *
   * @return The value for the selected option.
   */
  @Override
  public V get() {
    if (selectedValue != null && !selectedValue.isEmpty() && options.containsKey(selectedValue)) {
      return options.get(selectedValue);
    }
    return options.get(defaultChoice);
  }

  /**
   * Returns the selected option. If there is none selected, it will return the default. If there is
   * none or invalid option selected and no default, then it will return {@code null}.
   *
   * @return The value for the selected option.
   */
  public V getSelected() {
    return get();
  }

  /**
   * Binds the callback to run whenever the selected option changes. There can only be one listener,
   * and this method overwrites it with each invocation.
   *
   * @param listener The function to call that accepts the new value.
   */
  public void onChange(Consumer<V> listener) {
    this.listener = listener;
  }

  public void periodic() {
    if (!Logger.hasReplaySource()) {
      for (var update : selectedSubscriber.readQueue()) {
        selectedValue = update.value;
      }
      if (selectedValue == null) {
        String current = selectedSubscriber.get();
        if (current != null && !current.isEmpty()) {
          selectedValue = current;
        }
      }
      if (selectedValue == null || selectedValue.isEmpty() || !options.containsKey(selectedValue)) {
        selectedValue = defaultChoice;
      }
      activePublisher.set(selectedValue != null ? selectedValue : "");
    }
    Logger.processInputs(prefix, inputs);
    if (!Objects.equals(previousValue, selectedValue)) {
      if (listener != null) listener.accept(get());
      previousValue = selectedValue;
    }
  }
}
