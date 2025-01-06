---
sidebar_class_name: hidden
---

# Welcome to AdvantageKit

AdvantageKit is a logging, telemetry, and replay framework developed by Team 6328. AdvantageKit enables log replay, where the full state of the robot code can be replayed in simulation based on a log file ([What is AdvantageKit?](/getting-started/what-is-advantagekit/)).

:::danger
**_AdvantageKit is not a general-purpose logging framework._** Before continuing, check the documentation for:

- [AdvantageScope](https://docs.advantagescope.org), our robot telemetry application which _does not require AdvantageKit to use_.
- [WPILib Data Logging](https://docs.wpilib.org/en/stable/docs/software/telemetry/datalog.html), a simpler logging system included in WPILib (does not support log replay in simulation, but covers the needs of most teams).

:::

## REQUIRED

**The steps below are required to complete a valid installation of AdvantageKit.**

:::tip
Instead of installing AdvantageKit in an existing project, consider using one of AdvantageKit's [template projects](/category/template-projects) to get started more quickly. These projects can be downloaded from the [latest release](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest) and include prebuilt subsystems for **swerve drives**, **vision systems**, the **FIRST KitBot**, and more!
:::

1. Add the following blocks to the `build.gradle` file. Note that the lines under `dependencies` should be combined with the existing `dependencies` block.

```groovy
task(replayWatch, type: JavaExec) {
    mainClass = "org.littletonrobotics.junction.ReplayWatch"
    classpath = sourceSets.main.runtimeClasspath
}

dependencies {
    // ...
    def akitJson = new groovy.json.JsonSlurper().parseText(new File(projectDir.getAbsolutePath() + "/vendordeps/AdvantageKit.json").text)
    annotationProcessor "org.littletonrobotics.akit:akit-autolog:$akitJson.version"
}
```

2. Configure the main `Robot` class to inherit from `LoggedRobot` as shown below. See [here](./existing-projects.md#robot-configuration) for details.

```java
public class Robot extends LoggedRobot {
    ...
}
```

3. Initialize the logging framework in the constructor of `Robot` _before any other initialization_. An example configuration is provided below:

```java
Logger.recordMetadata("ProjectName", "MyProject"); // Set a metadata value

if (isReal()) {
    Logger.addDataReceiver(new WPILOGWriter()); // Log to a USB stick ("/U/logs")
    Logger.addDataReceiver(new NT4Publisher()); // Publish data to NetworkTables
    new PowerDistribution(1, ModuleType.kRev); // Enables power distribution logging
} else {
    setUseTiming(false); // Run as fast as possible
    String logPath = LogFileUtil.findReplayLog(); // Pull the replay log from AdvantageScope (or prompt the user)
    Logger.setReplaySource(new WPILOGReader(logPath)); // Read replay log
    Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim"))); // Save outputs to a new log
}

Logger.start(); // Start logging! No more data receivers, replay sources, or metadata values may be added.
```

:::info
By default, the `WPILOGWriter` class writes to a USB stick when running on the roboRIO. **A FAT32 formatted USB stick must be connected to one of the roboRIO USB ports**.
:::

This setup enters replay mode for all simulator runs. If you need to run the simulator without replay (e.g. a physics simulator or Romi), extra constants or selection logic is required. See the template projects for one method of implementing this logic.

:::note
For all support requests, please email software@team6328.org.
:::
