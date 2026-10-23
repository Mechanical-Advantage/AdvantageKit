---
sidebar_position: 3
---

# Dashboard Inputs {#dashboard-inputs}

Like the robot's hardware, **data retrieved from NetworkTables must be isolated and treated as input data.** For example, the following call will NOT function correctly in replay:

```java
var flywheelSetpoint = SmartDashboard.getNumber("FlywheelSpeed", 0.0);
```

AdvantageKit provides several solutions to deal with this issue:

- For subsystems that use NT input data (reading from coprocessors), we recommend treating the NetworkTables interaction as a hardware interface using an IO layer. See the [vision template project](/getting-started/template-projects/vision-template) as an example.
- When reading dashboard inputs from NT (auto selector, tuning values, etc) AdvantageKit includes the following classes that correctly handle periodic logging and replay:
  - [`LoggedNetworkChooser`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/akit/src/main/java/org/littletonrobotics/junction/networktables/LoggedNetworkChooser.java) - Replaces `Selectable` with equivalent functionality. See the example below.
  - [`LoggedNetworkNumber`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/akit/src/main/java/org/littletonrobotics/junction/networktables/LoggedNetworkNumber.java) - Simple number field
  - [`LoggedNetworkString`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/akit/src/main/java/org/littletonrobotics/junction/networktables/LoggedNetworkString.java) - Simple string field
  - [`LoggedNetworkBoolean`](https://github.com/Mechanical-Advantage/AdvantageKit/blob/main/akit/src/main/java/org/littletonrobotics/junction/networktables/LoggedNetworkBoolean.java) - Simple boolean field

Example use of `LoggedNetworkChooser` for auto routines in a command-based project:

```java
private final LoggedNetworkChooser<Command> autoChooser = new LoggedNetworkChooser<>("Auto Routine");

public RobotContainer() {
    // ...
    autoChooser.addDefault("Do Nothing", new InstantCommand());
    autoChooser.add("My First Auto", new MyFirstAuto());
    autoChooser.add("My Second Auto", new MySecondAuto());
    autoChooser.add("My Third Auto", new MyThirdAuto());
}

public Command getAutonomousCommand() {
    return autoChooser.get();
}
```

:::tip
AdvantageScope supports tuning via NetworkTables when running in the AdvantageKit NetworkTables mode. Tunable values must be published to the "/Tuning" table using `LoggedNetworkNumber`, `LoggedNetworkString`, or `LoggedNetworkBoolean`. Check the [AdvantageScope docs](https://docs.advantagescope.org/overview/live-sources/tuning-mode#tuning-with-advantagekit) for details.
:::

A `LoggedNetworkChooser` can also be constructed using an existing `Selectable`, which allows for compatibility with PathPlanner's `AutoBuilder` API:

```java
private final LoggedNetworkChooser<Command> autoChooser;

public RobotContainer() {
    // ...

    // buildAutoChooser() returns a Selectable
    autoChooser = new LoggedNetworkChooser<>("Auto Routine", AutoBuilder.buildAutoChooser());
}
```
