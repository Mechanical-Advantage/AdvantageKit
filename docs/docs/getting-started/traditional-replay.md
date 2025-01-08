---
sidebar_position: 4
---

# How To: Traditional Replay

## Setup

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

## Usage

To launch log replay, start the robot project in [simulation](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html). The generated log file will be opened automatically in AdvantageScope (check the API documentation for `WPILOGWriter` for details on customizing this behavior). Replay outputs are stored in the `ReplayOutputs` table alongside the unmodified inputs and outputs (stored in the `RealOutputs` table).

:::tip
We recommend **disabling the sim GUI** when running in replay. The GUI is disabled by default in the AdvantageKit template projects.
:::

## Replay Bubble

The most straightforward uses of replay involve [logging additional outputs](./what-is-advantagekit/example-output-logging.md). Code can also be modified when running in log replay. However, this use case comes with limitations as **modified outputs cannot affect replayed inputs**. This issue is discussed in more detail in the clip below, which is part of 6328's [2024 Championship Conference](./what-is-advantagekit/champs-conference.md).

<iframe width="100%" style={{"aspect-ratio": "16 / 9"}} src="https://www.youtube.com/embed/BrzPw6ngx4o?start=1676&end=1841" title="FRC Log Replay and Simulation (2024) -  FRC 6328 FIRST Championship Conference" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

![Replay bubble](./img/replay-bubble.png)
