---
sidebar_position: 5
---

# Deterministic Timestamps

### The Problem

To guarantee accurate replay, all of the data used by the robot code must be included in the log file and replayed in simulation. This includes the current timestamp, which may be used for calculations in control loops. WPILib's default behavior is to read the timestamp directly from the FPGA every time a method like `Timer.getTimestamp()` is called. Every return value will be slightly different as the timestamp continues increasing throughout each loop cycle. This behavior is problematic for AdvantageKit replay because the return values from those method calls are not logged and **cannot be accurately replayed in simulation**.

AdvantageKit's solution is to use _synchronized timestamps_. The timestamp is read once from the FPGA at the start of the loop cycle and injected into WPILib. By default, all calls to `Timer.getTimestamp()` or similar return the synchronized timestamp from AdvantageKit instead of the "real" FPGA time. During replay, the logged timestamp is used for each loop cycle. The result is that all of the control logic is _deterministic_ and will **exactly match the behavior of the real robot**.

### Solution #1

Where precise timestamps are required, the best solution is to record measurement timestamps as part of the input data for each subsystem. For example, [NetworkTables](https://docs.wpilib.org/en/stable/docs/software/networktables/publish-and-subscribe.html#subscribing-to-a-topic) and [Phoenix 6](https://api.ctr-electronics.com/phoenix6/release/java/com/ctre/phoenix6/StatusSignal.SignalMeasurement.html#timestamp) include methods to read the timestamp of each update, which can be used in calculations for wheel odometry, vision localization, or other precise controls alongside AdvantageKit's deterministic timestamps. For most use cases, measuring the timestamp of the original sample is more desirable than measuring the precise timestamp on the robot.

### Solution #2

`Timer.getFPGATimestamp()` can always be used to access the "real" FPGA timestamp where necessary, like within IO implementations or for analyzing performance. This method is not affected by log replay (i.e. it will not reflect the accelerated rate of replay). One use case for this method is measuring code execution time, since those values don't need to be recreated during log replay. WPILib classes like `Watchdog` use this method because they use the timestamp for analyzing performance and are not part of the robot's control logic.

### Solution #3

Optionally, AdvantageKit allows you to disable deterministic timestamps. This reverts to the default WPILib behavior of reading from the FPGA for every method call, making the behavior of the robot code non-deterministic. The "output" values seen in simulation may be slightly different than they were on the real robot. This alternative mode should only be used when _all_ of the following are true:

1. The control logic depends on the exact timestamp _within_ a single loop cycle, like a high precision control loop that is significantly affected by the precise time that it is executed within each (usually 20ms) loop cycle.
2. The sensor values used in the loop cannot be associated with timestamps in an IO implementation. See solution #1.
3. The IO (sensors, actuators, etc) involved in the loop are sufficiently low-latency that the exact timestamp on the RIO is significant. For example, CAN motor controllers are limited by the rate of their CAN frames, so the extra precision on the RIO is insignificant in most cases.

If you need to disable deterministic timestamps globally, add the following lines to the constructor of `Robot` _after_ `Logger.start()`:

```java
if (!Logger.hasReplaySource()) {
    RobotController.setTimeSource(RobotController::getFPGATime);
}
```
