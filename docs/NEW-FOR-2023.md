# What's New For 2023?

The fundamental goals and structure of AdvantageKit have not changed for 2023, but a variety of new features have been introduced. All of the changes from AdvantageKit v1.8.1 to v2.0.0 are listed below.

- [WPILOG/NT4 Support](#wpilognt4-support)
- [Dashboard Input Classes](#dashboard-input-classes)
- [Console Capture](#console-capture)
- [AutoLog Annotation](#autolog-annotation)
- [Geometry Logging Methods](#geometry-logging-methods)
- [Improved System Stats & Power Distribution Logging](#improved-system-stats--power-distribution-logging)
- [Misc Changes](#misc-changes)

## WPILOG/NT4 Support

The recommended formats for log data and network streaming are now WPILOG and NT4. WPILOG was introduced by WPILib during the 2022 season as a binary data logging format (very similar to the RLOG format previously used by AdvantageKit). NetworkTables 4 (NT4) improves on the older NetworkTables protocol with a pub/sub architecture and WebSocket protocol. Given the feature parity between WPILOG/NT4 and RLOG, we feel that switching to these formats will help users of AdvantageKit (by allowing for compatability with WPILib tools like Glass and Sapphire) and it will help us as developers (by reducing the amount of custom protocol code that must be maintained).

**So what's changing?**

`WPILOGWriter`, `WPILOGReader`, and `NT4Publisher` directly replace `ByteLogReceiver`, `ByteLogReplay`, and `LogSocketServer`. Here's what the logging configuration might look like with WPILOG and NT4:

```java
if (isReal()) {
    Logger.getInstance().addDataReceiver(new WPILOGWriter("/media/sda1"));
    Logger.getInstance().addDataReceiver(new NT4Publisher());
} else {
    String logPath = LogFileUtil.findReplayLog();
    logger.setReplaySource(new WPILOGReader(logPath));
    logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
}
```

The **`WPILOGWriter`** class manages log files independent of WPILib's [`DataLogManager`](https://first.wpi.edu/wpilib/allwpilib/docs/release/java/edu/wpi/first/wpilibj/DataLogManager.html). The log files can be opened with [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope) 2023 and later. The **`NT4Publisher`** class publishes AdvantageKit data to the "/AdvantageKit" table in NetworkTables (e.g. "/AdvantageKit/RealOutputs/FieldName"). AdvantageScope's "NetworkTables 4 (AdvantageKit)" live mode shows only this table. More information [here](https://github.com/Mechanical-Advantage/AdvantageScope/blob/main/docs/OPEN-LIVE.md).

> Important change: **Log data in AdvantageKit now persists between cycles**, to match the default behavior for WPILOGs and NT4. While this change does not affect subsystem inputs (which always log data every cycle), please keep the new behavior in mind when logging output data.

The set of loggable types has also been adjusted to align with the new formats. The table below shows the loggable types for AdvantageKit v1 and v2. The single "Byte" type has been removed, "Float" / "FloatArray" types have been added, and the "Integer" / "IntegerArray" types are now 64 bit.

| AdvantageKit v1 (2022)     | AdvantageKit v2 (2023)     |
| -------------------------- | -------------------------- |
| Byte                       | _Not available_            |
| ByteArray                  | ByteArray                  |
| Boolean                    | Boolean                    |
| Integer **(int32)**        | Integer **(int64)**        |
| _Not available_            | Float                      |
| Double                     | Double                     |
| String                     | String                     |
| BooleanArray               | BooleanArray               |
| IntegerArray **(int32[])** | IntegerArray **(int64[])** |
| _Not available_            | FloatArray                 |
| DoubleArray                | DoubleArray                |
| StringArray                | StringArray                |

## Dashboard Input Classes

Automatic logging of NetworkTables as an input has always been unreliable in AdvantageKit (the timing during replay was not deterministic). With WPILib's upgrade from NT3 to NT4, we have removed NT input logging (`LoggedNetworkTables`) and replaced it with the following solutions:

- For subsystems that use NT input data (reading from coprocessors), we recommend treating the NetworkTables interaction as a hardware interface using an IO layer. See 6328's [2022 vision subsystem](https://github.com/Mechanical-Advantage/RobotCode2022/tree/main/src/main/java/frc/robot/subsystems/vision) as an example.
- When reading dashboard inputs from NT (auto selector, tuning values, etc) AdvantageKit includes the following classes which correctly handle periodic logging and replay:
  - [`LoggedDashboardChooser`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedDashboardChooser.java) - Replaces `SendableChooser` with equivalent functionality.
  - [`LoggedDashboardNumber`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedDashboardNumber.java) - Simple number field
  - [`LoggedDashboardString`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedDashboardString.java) - Simple string field
  - [`LoggedDashboardBoolean`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedDashboardBoolean.java) - Simple boolean field

## Console Capture

AdvantageKit now automatically captures STDOUT and STDERR on the robot and in the simulator. No configuration is required. AdvantageScope includes a new "Console" view that can be used to view this data.

> Note: Output from native code is not captured in the simulator. All output is captured when running on the RIO.

![AdvantageScope console view](resources/console-1.png)

## `@AutoLog` Annotation

The `@AutoLog` annotation was added in v1.8.0 and improved for the 2023 releases. By adding this annotation to an inputs class, AdvantageKit will automatically generate implementations of `toLog` and `fromLog` for your inputs.

For example:

```java
@AutoLog
public class MyInputs {
    public double myField = 0;
}
```

This will generate the following class:

```java
class MyInputsAutoLogged extends MyInputs implements LoggableInputs {
    public void toLog(LogTable table) {
        table.put("MyField", myField);
    }

    public void fromLog(LogTable table) {
        myField = table.getDouble("MyField", myField);
    }
}
```

The `@AutoLog` annotation is included in the AdvantageKit example projects. To add this feature to an existing project, see the AdvantageKit [installation guide](INSTALLATION.md).

## Geometry Logging Methods

Logging geometry objects like `Translation2d`, `Pose2d`, `Pose3d`, etc. is common in robot code. AdvantageKit now includes the following functions to easily log these objects in the formats expected by AdvantageScope:

```java
// 2D geometry (for odometry view)
Logger.getInstance().recordOutput("MyTranslation2d", new Translation2d());
Logger.getInstance().recordOutput("MyTranslation2dArray", new Translation2d[] {});
Logger.getInstance().recordOutput("MyPose2d", new Pose2d());
Logger.getInstance().recordOutput("MyPose2dArray", new Pose2d[] {});

// 3D geometry (for 3D field view)
Logger.getInstance().recordOutput("MyTranslation3d", new Translation3d());
Logger.getInstance().recordOutput("MyTranslation3dArray", new Translation3d[] {});
Logger.getInstance().recordOutput("MyPose3d", new Pose3d());
Logger.getInstance().recordOutput("MyPose3dArray", new Pose3d[] {});

// Swerve module states (for swerve visualization)
Logger.getInstance().recordOutput("MySwerveModuleStates", new SwerveModuleState[] {});
```

## Improved System Stats & Power Distribution Logging

Logging of system stats and power distribution data has been moved into conduit, allowing for more data to be gathered efficiently. The following fields are now included, and **available in replay**:

### SystemStats

- BatteryVoltage
- BatteryCurrent
- RIO rail status (3.3V, 5V, and 6V):
  - Active, Current, Faults, Voltage
- BrownedOut
- SystemActive
- CAN status:
  - Utilization, TxFullCount, ReceiveErrorCount, TransmitErrorCount, OffCount
- EpochTimeMicros

### PowerDistribution

- Temperature
- Voltage
- ChannelCurrent (all channels)
- TotalCurrent
- TotalPower
- TotalEnergy
- ChannelCount
- Faults
- StickyFaults

Power distribution logging is disabled by default. To enable it, instantiate a [`PowerDistribution`](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/wpilibj/PowerDistribution.html) object or called `LoggedPowerDistribution.getInstance(moduleId, moduleType)`.

## Misc Changes

- Updated to WPILib 2023 (see release notes for the specific version). Added support for NT4 and required changes to `DriverStation` shim.
- Fixed a bug causing "DriverStation.isEnabled()" and "DriverStation.isDSAttached()" to never update after the DS disconnected.
- Fixed an issue causing timestamps from `WPIUtilJNI.now()` to read the true FPGA time (instead of the synchronized/replayed time from AdvantageKit).
- Fixed an issue with `LoggedRobot` causing cyles to run too quickly after a loop overrun. See [wpilibsuite/allwpilib#4101](https://github.com/wpilibsuite/allwpilib/pull/4101).
- Added example projects, attached as zip files to the latest AdvantageKit release.
