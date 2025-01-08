---
sidebar_position: 3
---

# Dashboard Inputs

Like the robot's hardware, **data retrieved from NetworkTables must be isolated and treated as input data.** For example, the following call will NOT function correctly in replay:

```java
var flywheelSetpoint = SmartDashboard.getNumber("FlywheelSpeed", 0.0);
```

AdvantageKit provides several solutions to deal with this issue:

- For subsystems that use NT input data (reading from coprocessors), we recommend treating the NetworkTables interaction as a hardware interface using an IO layer. See the [vision template project](/getting-started/template-projects/vision-template) as an example.
- When reading dashboard inputs from NT (auto selector, tuning values, etc) AdvantageKit includes the following classes that correctly handle periodic logging and replay:
  - [`LoggedDashboardChooser`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedDashboardChooser.java) - Replaces `SendableChooser` with equivalent functionality. See the example below.
  - [`LoggedNetworkNumber`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedNetworkNumber.java) - Simple number field
  - [`LoggedNetworkString`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedNetworkString.java) - Simple string field
  - [`LoggedNetworkBoolean`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/junction/core/src/org/littletonrobotics/junction/networktables/LoggedNetworkBoolean.java) - Simple boolean field

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

:::tip
AdvantageScope supports tuning via NetworkTables when running in the AdvantageKit NetworkTables mode. Tunable values must be published to the "/Tuning" table using `LoggedNetworkNumber`, `LoggedNetworkString`, or `LoggedNetworkBoolean`. Check the [AdvantageScope docs](https://docs.advantagescope.org/getting-started/connect-live#tuning-with-advantagekit) for details.
:::

A `LoggedDashboardChooser` can also be constructed using an existing `SendableChooser`, which allows for compatibility with PathPlanner's `AutoBuilder` API:

```java
private final LoggedDashboardChooser<Command> autoChooser;

public RobotContainer() {
    // ...

    // buildAutoChooser() returns a SendableChooser
    autoChooser = new LoggedDashboardChooser<>("Auto Routine", AutoBuilder.buildAutoChooser());
}
```
