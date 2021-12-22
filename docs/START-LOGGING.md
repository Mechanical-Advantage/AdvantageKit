# Getting Started: Logging

## Background

A variety of logging frameworks already exist in FRC, from built-in tools like the Driver Station and Network Tables all the way to custom solutions for logging more detailed information. However, most systems exist around the same fundamental idea; record a limited set of values explicitly provided by the code. That could include sensor data, PID error, odometry pose, internal state, etc. While this had enormous value, it doesn't solve the eternal sentiment when something goes wrong: "If only we were logging this one extra field!"

Logging in AdvantageKit is built around a different principle, often used in industry. Instead of logging specific output values from the user code, it records *all of the data flowing into the robot code*. Every sensor value, joystick press, Network Tables update, and much more is logged every loop cycle. After a match, these values can be replayed to the robot code in a simulator. Since every input command is the same, all of the internal logic of the code is replayed exactly. This allows you to log extra fields after the fact, or add breakpoints and inspect the code line by line. This technique means that logging is more than just a tool for checking on specific issues; it's also a safety net that can be used to verify how any part of the code functions.

Logging in AdvantageKit is built with the following goals in mind:

* Support a high level of logging in way that is **accessible**. This menas reducing the changes required to user code as much as possible, and building on top of existing frameworks (like command based WPILib).

* Provide **capable** and **easy-to-use** methods of viewing log data. The robot code saves logs in a custom binary format which is read by our viewer application [Advantage Scope](https://github.com/Mechanical-Advantage/AdvantageScope). Data can also be viewed live over the networkk.

* Maintain **openness** and **transparency** during development as part of 6328's efforts with the [#OpenAlliance](https://github.com/OpenAllianceFRC/info).

## Advantage Scope

You can download the latest version of Advantage Scope [here](https://github.com/Mechanical-Advantage/AdvantageScope/releases/latest) for Windows, macOS, and Linux. This application is required for viewing log files.

## Installation With Gradle

AdvantageKit is available through GitHub Packages. To make our Maven repository available through Gradle, add the following block to the `build.gradle` file:

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Mechanical-Advantage/AdvantageKit")
        credentials {
            username = "Mechanical-Advantage-Bot"
            password = "ghp_ykO20iuCY5H2i" + "LgmNxz8VIKJ2uQa6g1DdV0i"
        }
    }
}
```

To pull in the logging framework, add the following lines in `dependencies`. Replace "X.X.X" with the latest version of AdvantageKit (see [here](https://github.com/Mechanical-Advantage/AdvantageKit/releases/latest)).

```groovy
implementation "org.littletonrobotics.akit.junction:wpilib-shim:X.X.X"
implementation "org.littletonrobotics.akit.junction:junction-core:X.X.X"
implementation "org.littletonrobotics.akit.conduit:conduit-api:X.X.X"
// TODO: Add native dependencies
```

AdvantageKit modifies some components of `wpilibj` (see [our explanation](/docs/CONDUIT-SHIMS.md) for the purpose of these shims or the [list of modified classes](/junction/shims/wpilib#interface)). Add the following block to `build.gradle` to replace the default implementation. **This is required for the framework to function**

```groovy
configurations.all {
    exclude group: "edu.wpi.first.wpilibj"
}
```

## Understanding Data Flow

`Logger` is the primary class managing data flow for the logging framework. It functions in two possible modes depending on the environment:
* **Real robot/simulator** - When running on a real robot (or a physics simulation, Romi, etc.), `Logger` reads data from the user program and built-in sources, then saves it to one or more targets (usually a log file).
* **Replay** - During this mode, which runs in the simulator, `Logger` reads data from an external source like a log file and writes it out to the user program. It then records the original data (plus any outputs from the user program) to a separate log file.

![Diagram of data flow](resources/data-flow.png)

Below are definitions of each component:
* **User inputs** - Input data from hardware managed by the user program. This primarily includes input data to subsystem classes. See "Subsystems" below for how this component is implemented.
* **User outputs** - Data produced by the user program based on the current inputs (odometry, calculated voltages, internal states, etc.). This data can be reproduced during replay, so it's the primary method of debugging code based on a log file.
* **Replay source** - Provides data from an external source for use during replay. This almost always means reading data from a log file produced by the robot. A replay source only exists while in replay (never on the real robot).
* **Data receiver** - Saves data to an external source in all modes. Multiple data receivers can be provided (or none at all). While a data receiver usually writes to a log file, it can also be used to send data over the network.
* **LoggedSystemStats** *(Built-in input)* - Records robot stats like battery voltage, PDP current draw, and CAN bus utilization for diagnostics. This data is not available during replay.
* **LoggedNetworkTables** *(Built-in input)* - Records and replays data in Network Tables. This is primarily intended for data which is not time sensitive such as dashboard options and tuning values.
* **LoggedDriverStation** *(Built-in input)* - Internal class managing driver station data. See [here](/docs/CONDUIT-SHIMS.md) for details on why this data needs to be managed by the framework.

Data is stored based on string keys where slashes are used to denote subtables (similar to Network Tables) - each subsystem stores data in a separate subtable. A new set of data is produced for each cycle, meaning values are NOT persistent. If a key is not set during a cycle, its value will be `null` by default. The following data types are currently supported:

`boolean, boolean[], int, int[], double, double[], String, String[], byte, byte[]`

`Logger` is also responsible for manging timestamps. The current timestamp from the FPGA is read at the start of each cycle and syncronized for all logic. For example, calls to `Timer.getFPGATimestamp()` will return this syncronized timestamp. This system guarantees that control logic can be replayed accurately.

## Robot Configuration

TODO.

## Subsystems

TODO.

## Logging Outputs

TODO.

## Restrictions

TODO.