---
sidebar_position: 5.5
---

# 🔀 How To: Replay Configuration {#how-to-replay-configuration}

The AdvantageKit template projects are designed to be a simple starting point, and teams are encouraged to adapt them to their own workflows. By default, the template projects switch between simulation and replay using the `simMode` option in `Constants.java` (see [traditional replay](./traditional-replay.md#setup)). This page describes how to configure the sim GUI, along with an alternative approach for teams that prefer to launch the robot program from the command line.

## Enabling the Sim GUI {#enabling-the-sim-gui}

AdvantageKit replay requires that all HAL simulation extensions be disabled, including the [simulation GUI](https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/simulation-gui.html). For this reason, the GUI is disabled by default in the `build.gradle` file of the AdvantageKit template projects:

```groovy
wpi.sim.addGui().defaultEnabled = false
wpi.sim.addDriverstation()
```

To enable the GUI by default when running the simulation, change `defaultEnabled` to `true`. The value of `defaultEnabled` sets the extensions that are used when running `./gradlew run`, as well as the extensions that are selected by default when launching the simulation from VSCode.

:::warning
If the GUI is enabled by default, it must be disabled manually before running replay. When launching from VSCode, uncheck all of the extensions in the prompt before starting the simulation.
:::

To enable the GUI automatically in simulation and disable it in replay, see [selecting the mode from the command line](#selecting-the-mode-from-the-command-line).

## Selecting the Mode from the Command Line {#selecting-the-mode-from-the-command-line}

Teams that launch the simulation from the command line can instead select the mode with arguments passed to Gradle, without any code changes. This approach also enables the GUI automatically in simulation and disables it in replay.

First, replace the lines from the previous section in `build.gradle` with the following:

```groovy
def akitLogPath = findProperty("akit.log.path") ?: System.getProperty("akit.log.path") ?: System.getenv("AKIT_LOG_PATH")
def isReplay = akitLogPath != null || System.getProperty("simMode") == "REPLAY"

wpi.sim.addGui().defaultEnabled = !isReplay
wpi.sim.addDriverstation().defaultEnabled = !isReplay

tasks.withType(JavaExec).configureEach {
    standardInput = System.in
    systemProperty "simMode", isReplay ? "REPLAY" : "SIM"
    if (akitLogPath != null) {
        environment "AKIT_LOG_PATH", akitLogPath
    }
}
```

Gradle does not forward terminal input to the robot program by default, so the `standardInput` line is required for the [log path prompt](./traditional-replay.md#finding-the-replay-log) to work.

Next, update the `simMode` option in `Constants.java` to read the value provided by Gradle:

```java
public static final Mode simMode = Mode.valueOf(System.getProperty("simMode", "SIM"));
```

The mode can now be selected when launching the simulation:

```bash
./gradlew run                                     # Simulation, with the GUI enabled
./gradlew run -Dakit.log.path=/path/to/log.wpilog # Replay a specific log file
./gradlew run -DsimMode=REPLAY                    # Replay the log open in AdvantageScope, or prompt for a path
```

When running from Windows PowerShell, wrap each `-D` argument in quotes (e.g. `"-Dakit.log.path=C:\path\to\log.wpilog"`).

:::info
Gradle accepts two types of properties from the command line: `-D` sets a Java system property (read using `System.getProperty`) and `-P` sets a Gradle project property (read using `findProperty`). Both types are only available to Gradle itself, so they must be forwarded to the robot program by `build.gradle` (as shown above using `systemProperty` and `environment`). This allows users to pass in simulation configurations (such as the aforementioned replay mode) to the robot code without changing the source.  
To change the default value of a property without passing it each time, add a line such as `systemProp.simMode=REPLAY` to the `gradle.properties` file in the project, or to `~/.gradle/gradle.properties` to apply it to a single computer. Values passed on the command line override the values from `gradle.properties`. Check the [Gradle documentation](https://docs.gradle.org/current/userguide/build_environment.html) for more details.
:::

[Replay watch](./replay-watch.md) is also supported by providing the log path, either using `./gradlew replayWatch -Dakit.log.path=/path/to/log.wpilog` or by setting the `AKIT_LOG_PATH` environment variable. Note that setting `AKIT_LOG_PATH` for the entire shell will cause `./gradlew run` to always run in replay.

:::info
The Gradle arguments are not available when launching the simulation from VSCode, which always runs in simulation with this configuration. Use the default extensions in the VSCode prompt for simulation, or uncheck all of the extensions and set the default value of `simMode` in `Constants.java` to `"REPLAY"` for replay.
:::

:::tip
When a log path is provided, replay runs without the sim GUI or any other user input and exits automatically at the end of the log. This allows replay to run in automated environments such as CI, for example to replay a reference log on every pull request and save the replayed log as a build artifact.
:::
