---
sidebar_position: 3
---

# Dashboard Inputs

Like the robot's hardware, **data retrieved from NetworkTables must be isolated and treated as input data.** For example, the following call will NOT function correctly in replay:

```java
var flywheelSetpoint = SmartDashboard.getNumber("FlywheelSpeed", 0.0);
```

AdvantageKit provides several solutions to deal with this issue:

- For subsystems that use NT input data (reading from coprocessors), we recommend treating the NetworkTables interaction as a hardware interface using an IO layer. See 6328's [2022 vision subsystem](https://github.com/Mechanical-Advantage/RobotCode2022/tree/main/src/main/java/frc/robot/subsystems/vision) as an example.
- When reading dashboard inputs from NT (auto selector, tuning values, etc) AdvantageKit includes the following classes which correctly handle periodic logging and replay:
  - [`LoggedDashboardChooser`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedDashboardChooser.java) - Replaces `SendableChooser` with equivalent functionality. See the example below.
  - [`LoggedDashboardNumber`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedDashboardNumber.java) - Simple number field
  - [`LoggedDashboardString`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedDashboardString.java) - Simple string field
  - [`LoggedDashboardBoolean`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedDashboardBoolean.java) - Simple boolean field

Example use of `LoggedDashboardChooser` for auto routines in a command-based project:

```java
private final LoggedDashboardChooser<Command> autoChooser = new LoggedDashboardChooser<>("Auto Routine");

public RobotContainer() {
    // ...
    autoChooser.addDefaultOption("Do Nothing", new InstantCommand());
    autoChooser.addOption("My First Auto", new MyFirstAuto());
    autoChooser.addOption("My Second Auto", new MySecondAuto());
    autoChooser.addOption("My Third Auto", new MyThirdAuto());
}

public Command getAutonomousCommand() {
    return autoChooser.get();
}
```

A `LoggedDashboardChooser` can also be constructed using an existing `SendableChooser`, which allows for compatibility PathPlanner's `AutoBuilder` API:

```java
private final LoggedDashboardChooser<Command> autoChooser;

public RobotContainer() {
    // ...

    // buildAutoChooser() returns a SendableChooser
    autoChooser = new LoggedDashboardChooser<>("Auto Routine", AutoBuilder.buildAutoChooser());
}
```
