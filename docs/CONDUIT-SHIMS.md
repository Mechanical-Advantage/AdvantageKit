# Conduit & WPILib Shims

Input data from the Driver Station is some of the most critical information recorded by the logging framework. This includes the current robot state (enabled, autonomous, etc.) and joystick data like axis and button values. In managing this data, we have two key goals:

* As with all functions of the logging framework, we want to **minimize the amount of time consumed in each cycle** while recording DS data. This is one of the largest chunks of data saved to the log, so we don't want this component to inadvertently cause loop overruns.

* The DS **data used by the user program on the real robot should be identical to the data saved to the log**. Given the importance of this data, even an offset of a single cycle between the real robot and the replay environment could envoke new behavior (the system is no longer [deterministic](https://en.wikipedia.org/wiki/Deterministic_system)). For example, if the robot is "enabled" one cycle later during replay, all of the sensor data would be offset from the internal control loops and result in different outputs.

## Stock WPILib

The built-in `DriverStation` class in WPILib is responsible for making DS data available to the user program. Classes like `RobotBase`, `Joystick`, and `XboxController` all refer back to `DriverStation`. Internally, `DriverStation` retrieves new data through the following steps (in summary):

1. It launches a thread which waits for a new DS packet with updated data. These updates occur every 20ms, but *not in sync with the user program*.

2. When a new packet arrives, common data like the robot state and joystick values are read from the HAL and stored to a local cache. The thread then blocks until the next packet is received.

3. When this common data is requested by the user program, it is read from the internal cache.

4. When some less common data is requested by the user program, it is read directly from the HAL. This includes joystick types, names, the alliance color/station, match time, etc.

The approach to logging this data seems clear - record all of the values from the `DriverStation` class at the start of each loop cycle, then replay them in the simulator using the `DriverStationSim` class (which includes setters for all of these fields). However, this approach falls short on both of our original objectives:

* When the cache isn't used, reading data directly from the HAL is performance intensive. We believe this is because 1) reading the data involves making lots of JNI (Java Native Interface) calls, which are known to be inefficient and 2) calls to the HAL make use of NetComm functions which may be blocking on network IO. Based on our testing, reading all of the DS data in Java took ~2-3ms per cycle. Given that this does not include any other periodic functions of the logging framework, we felt that this overhead was not acceptable.

* The structure of the `DriverStation` class also poses a more fundamental issue - since the cache is updated in a thread, the data read by the logging framework at the beginning of the cycle may be different from the data read by the user program. If a DS packet happens to arrive in the ~5-10ms window where the loop cycle is actively running, we have no way of replaying that update.

## Solution #1: Performance/Conduit

Most of the logging framework is written in Java (this is [`junction`](/junction)). To improve the performance of reading DS data, we created a C++ component ([`conduit`](/conduit)). `conduit` is responsible for efficiently transfering DS data from the HAL to `junction`. Below is the new pathway for DS data:

1. `conduit` launches a thread which waits for new DS packets (much like the Java `DriverStation`). When a new packet arrives, all of the data is saved to an internal memory buffer in `conduit`. This includes all of the DS data, including the less common fields not cached by default.

2. When the logging framework needs to record DS data each cycle, it makes a single JNI call into `conduit`. The data from the internal memory buffer is copied to a shared buffer accessible to the Java code.

3. Data is read from the shared memory buffer and saved to the log file.

This system vastly reduces the number of JNI calls, as all of the data is transferred at once. Reading from the shared buffer in Java is very efficient, and all calls that make use of NetComm occur within the `conduit` thread. Based on our testing, the entire update process now takes only ~0.1-0.3ms.

## Solution #2: Determinism/Shims

Using `conduit`, the logging framework already has a complete copy of all of the DS data provided by the `DriverStation` class. Thus, the most logical solution to control the data read by the user program is to replace the build-in `DriverStation` class. Our shimmed version of `DriverStation` does not include a separate thread, instead reading data directly from the cache in `junction`. This means that the data saved to the log file will always be identical to the data read by the user program.

## Timestamps

The same problem of determinism applies not just to DS data, but also to the timestamp. Calls like `Timer.getFPGATimestamp()` read directly from the HAL, meaning that every call within a cycle will return a different value. This means that the exact behavior of the user program may not be reproducible as these values are not logged. Our solution is to record a single timestamp at the start of the loop cycle, which is used until the next udpate. The `RobotController` class is shimmed such that the `getFPGATime()` method returns the timestamp recorded by the logging framework (this method is used by a variety of other classes including `Timer`). Again, the shim guarantees that the data saved to the log file is identiacl to the data read by the user program.

The full list of modified classes is [here](/junction/shims/wpilib). The artifact `org.littletonrobotics.akit.junction:wpilib-shim` is a full replacement for `wpilibj`, except with our shimmed classes. The following lines in `build.gradle` (as seen in the [installation instructions](/docs/START-LOGGING.md#installation-with-gradle)) replace the default implementation:

```groovy
configurations.all {
    exclude group: "edu.wpi.first.wpilibj"
}
```

## What if the logging framework is disabled?

It's critical that the timestamp and DS data are available to the user program at all times, even if the rest of the logging framework is disabled. If the call to `Logger.getInstance().start()` is not included in `robotInit`, the following fallbacks are used.

* All calls to record data (`recordOutput` and `processInputs`) will return immediately.

* When the timestamp is requested (including through a shimmed class), the real timestamp from the HAL will be returned.

* During the normal logging periodic function, DS data will be updated from conduit. No other periodic code will run. Note that the main `Robot` class *must still inherit from `LoggedRobot`*, otherwise the periodic function of the logging framework will not be called and DS data will not be updated.