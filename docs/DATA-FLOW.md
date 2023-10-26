# Understanding Data Flow

`Logger` is the primary class managing data flow for AdvantageKit. It functions in two possible modes depending on the environment:

- **Real robot/simulator** - When running on a real robot (or a physics simulation, Romi, etc.), `Logger` reads data from the user program and built-in sources, then saves it to one or more targets (usually a log file).
- **Replay** - During this mode, which runs in the simulator, `Logger` reads data from an external source like a log file and writes it out to the user program. It then records the original data (plus any outputs from the user program) to a separate log file.

## Components

![Diagram of data flow](resources/data-flow.png)

Below are definitions of each component:

- **User inputs** - Input data from hardware managed by the user program. This primarily includes input data to subsystem classes. See ["Code Structure"](CODE-STRUCTURE.md) for how this component is implemented.
- **User outputs** - Data produced by the user program based on the current inputs (odometry, calculated voltages, internal states, etc.). This data can be reproduced during replay, so it's the primary method of debugging code based on a log file. _Starting in 2023, user outputs also include any text sent to the console (logged automatically by AdvantageKit)._
- **Replay source** - Provides data from an external source for use during replay. This usually means reading data from a log file produced by the robot. A replay source only exists while in replay (never on the real robot).
- **Data receiver** - Saves data to an external source in all modes. Multiple data receivers can be provided (or none at all). While data receivers can to a log file or send data over the network.
- **LoggedDriverStation** _(Built-in input)_ - Internal class for recording and replaying driver station data (enabled state, joystick data, alliance color, etc).
- **LoggedSystemStats** _(Built-in input)_ - Internal class for recording and replaying data from the roboRIO (battery voltage, rail status, CAN status).
- **LoggedPowerDistribution** _(Built-in input)_ - Internal class for recording and replaying data from the PDP or PDH (channel currents, faults, etc).

## Simple Data Types

Data is stored using string keys where slashes are used to denote subtables (similar to NetworkTables). Each subsystem stores data in a separate subtable. Like NetworkTables, **all logged values are persistent (they will continue to appear on subsequent cycles until updated**). The following simple data types are currently supported:

`boolean, long, float, double, String, boolean[], long[], float[], double[], String[], byte[]`

## Structured Data Types

Many WPILib classes can be serialized to binary data using [structs](https://github.com/wpilibsuite/allwpilib/blob/main/wpiutil/doc/struct.adoc) or [protobufs](https://protobuf.dev). Supported classes include `Translation2d`, `Pose3d`, and `SwerveModuleState` with more coming soon. These classes can be logged as single values or arrays just like any simple type, and used as input or output fields.

AdvantageKit also supports logging the state of a `Mechanism2d` object as an output. For details, see [here](CODE-STRUCTURE.md#logging-outputs).

## Deterministic Timestamps

### The Problem

To guarantee accurate replay, all of the data used by the robot code must be included in the log file and replayed in simulation. This includes the current timestamp, which may be used for calculations in control loops. WPILib's default behavior is to read the timestamp directly from the FPGA every time a method like `Timer.getFPGATimestamp()` is called. Every return value will be slightly different as the timestamp continues increasing throughout each loop cycle. This behavior is problematic for AdvantageKit replay, because the return values from those method calls are not logged and **cannot be accurately replayed in simulation**.

AdvantageKit's solution is to use _synchronized timestamps_. The timestamp is read once from the FPGA at the start of the loop cycle and injected into WPILib. By default, all calls to `Timer.getFPGATimestamp()` or similar return the synchronized timestamp from AdvantageKit instead of the "real" FPGA time. During replay, the logged timestamp is used for each loop cycle. The result is that all of the control logic is _deterministic_ and will **exactly match the behavior of the real robot**.

### Solution #1

Where precise timestamps are required, the best solution is to record measurement timestamps as part of the input data for each subsystem. For example, [NetworkTables](https://docs.wpilib.org/en/stable/docs/software/networktables/publish-and-subscribe.html#subscribing-to-a-topic) and [Phoenix 6](https://api.ctr-electronics.com/phoenix6/release/java/com/ctre/phoenix6/StatusSignal.SignalMeasurement.html#timestamp) include methods to read the timestamp of each update, which can be used in calculations for wheel odometry, vision localization, or other precise controls alongside AdvantageKit's deterministic timestamps. For most use cases, measuring the timestamp of the original sample is more desirable than measuring the precise timestamp on the robot.

### Solution #2

`Logger.getRealTimestamp()` can always be used to access the "real" FPGA timestamp where necessary, like within IO implementations or for analyzing performance. This method is not affected by log replay (i.e. it will not reflect the accelerated rate of replay). One use case for this method is measuring code execution time, since those values don't need to be recreated during log replay. AdvantageKit shims WPILib classes like `Watchdog` to use this method because they use the timestamp for analyzing performance and are not part of the robot's control logic.

### Solution #3

Optionally, AdvantageKit allows you to disable deterministic timestamps. This reverts to the default WPILib behavior of reading from the FPGA for every method call, making the behavior of the robot code non-deterministic. The "output" values seen in simulation may be slightly different than they were on the real robot. This alternative mode should only be used when _all_ of the following are true:

1. The control logic depends on the exact timestamp _within_ a single loop cycle, like a high precision control loop that is significantly affected by the precise time that it is executed within each (usually 20ms) loop cycle.
2. The sensor values used in the loop cannot be associated with timestamps in an IO implementation. See solution #1.
3. The IO (sensors, actuators, etc) involved in the loop are sufficiently low-latency that the exact timestamp on the RIO is significant. For example, CAN motor controllers are limited by the rate of their CAN frames, so the extra precision on the RIO is insignificant in most cases.

If you need to disable deterministic timestamps globally, add the following line to `robotInit()` before `Logger.start()`:

```java
Logger.disableDeterministicTimestamps();
```
