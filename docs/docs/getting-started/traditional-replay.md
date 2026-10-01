---
sidebar_position: 4
---

# ⏪ How To: Traditional Replay {#how-to-traditional-replay}

## Setup {#setup}

The AdvantageKit template projects are preconfigured to support replay by changing the `simMode` option in `Constants.java` to `REPLAY`. More broadly, replay requires the following elements in the logger configuration:

- A log file to use as the source, containing the original inputs and outputs:

```java
// The log path can be read from anything, but this method is provided for convenience
String logPath = LogFileUtil.findReplayLog();

// The following sources are used automatically, with these priorities:
//
// 1. The value of the "AKIT_LOG_PATH" environment variable, if set
// 2. The file currently open in AdvantageScope, if available
// 3. The result of the prompt displayed to the user
```

See [finding the replay log](#finding-the-replay-log) for more details.

- A replay source such as `WPILOGReader`:

```java
Logger.setReplaySource(new WPILOGReader(logPath));
```

- A data receiver such as `WPILOGWriter`, which will write a new log file containing the new outputs along with the original inputs and outputs:

```java
// The addPathSuffix function generates a new filename by adding the suffix.
// If running replay repeatedly, a numeric index is added to the filename instead.
Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
```

- Optionally, the robot program can be configured to run faster than real-time. This allows log replay to complete faster than the duration of the original log file and **does not affect the accuracy of log replay**.

```java
setUseTiming(false);
```

## Finding the Replay Log {#finding-the-replay-log}

The `LogFileUtil` class provides helper functions for selecting the log files used in replay. These functions are optional, and the log path can be provided from any source.

`LogFileUtil.findReplayLog()` returns the path of the log to replay, checking the following sources in order:

1. **`AKIT_LOG_PATH` environment variable:** If set, its value is used as the log path. This is useful when launching replay from a script or the [command line](./replay-configuration.md#selecting-the-mode-from-the-command-line).
2. **AdvantageScope:** The log file currently open in AdvantageScope is used, so replay can be started without providing a path after opening the original log.
3. **Prompt:** If neither source is available, the path is requested in the terminal. Quotes around the path are removed, so paths copied or dragged into the terminal can be used directly.

`LogFileUtil.addPathSuffix(path, suffix)` generates the path for the replayed log by adding a suffix to the original filename (e.g. `match.wpilog` → `match_sim.wpilog`). If the filename already ends with the suffix, a numeric index is added instead (`match_sim.wpilog` → `match_sim_2.wpilog` → `match_sim_3.wpilog`), so replaying the output of a previous replay does not overwrite it.

## Usage {#usage}

To launch log replay, start the robot project in [simulation](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html). The generated log file will be opened automatically in AdvantageScope. Replay outputs are stored in the `ReplayOutputs` table alongside the unmodified inputs and outputs (stored in the `RealOutputs` table).

The behavior for opening log files in AdvantageScope can be customized by passing an `AdvantageScopeOpenBehavior` to the `WPILOGWriter` constructor (check the [API docs](pathname:///javadoc/org/littletonrobotics/junction/wpilog/WPILOGWriter.AdvantageScopeOpenBehavior.html) for details):

- `AUTO` (default): Open the log file in AdvantageScope when running in replay.
- `ALWAYS`: Always open the log file in AdvantageScope when running in simulation.
- `NEVER`: Never open the log file in AdvantageScope.

```java
Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim"), AdvantageScopeOpenBehavior.NEVER));
```

:::tip
The simulation GUI **must be disabled** when running in replay. The GUI is disabled by default in the AdvantageKit template projects.
:::

## Replay Bubble {#replay-bubble}

The most straightforward uses of replay involve [logging additional outputs](./what-is-advantagekit/example-output-logging.md). Code can also be modified when running in log replay. However, this use case comes with limitations as **modified outputs cannot affect replayed inputs**. This issue is discussed in more detail in the clip below, which is part of 6328's [2025 Championship Conference](./what-is-advantagekit/champs-conference.md).

<iframe width="100%" style={{"aspect-ratio": "16 / 9"}} src="https://www.youtube.com/embed/8FfwFQcvRmU?start=1957" title="FRC Log Replay and Simulation (2025) -  FRC 6328 FIRST Championship Conference" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

![Replay bubble](./img/replay-bubble.png)
