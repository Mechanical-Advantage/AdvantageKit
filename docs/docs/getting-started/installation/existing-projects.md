---
sidebar_position: 1
---

# Existing Projects

To install the AdvantageKit vendordep, follow the instructions in the WPILib documentation for [installing vendor libraries](https://docs.wpilib.org/en/stable/docs/software/vscode-overview/3rd-party-libraries.html#installing-libraries) and choose "AdvantageKit" from the list. Alternatively, go to "WPILib: Manage Vendor Libraries" > "Install new libraries (online)" in VSCode and paste the URL below.

```
https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest/download/AdvantageKit.json
```

Next, add the following blocks to the `build.gradle` file. Note that the lines under `dependencies` should be combined with the existing `dependencies` block.

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

## Robot Configuration

The main `Robot` class **must inherit from `LoggedRobot`** (see below). `LoggedRobot` performs the same functions as `TimedRobot`, with some exceptions:

- It does not support adding extra periodic functions.
- The method `setUseTiming` allows the user code to disable periodic timing and run cycles as fast as possible during replay. The timestamp read by methods like `Timer.getFPGATimstamp()` will still match the timestamp from the real robot.

```java
public class Robot extends LoggedRobot {
    ...
}
```

The user program is responsible for configuring and initializing the logging framework. This setup should be placed in the constructor of `Robot` _before any other initialization_. An example configuration is provided below:

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
