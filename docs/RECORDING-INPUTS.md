# Recording Inputs: IO Layers

By necessity, any interaction with external hardware must be isolated such that all input data is logged and can be replayed in the simulator where that hardware is not present. Most hardware interaction occurs in subsystem classes (read [this section](#dashboard-options--networktables-inputs) for information on using NetworkTables as an input). Traditionally, a subsystem has three main components:

![Diagram of traditional subsystem](resources/subsystem-1.png)

- The **public interface** consists of methods used by the rest of the robot code to control the subsystem.

- The **control logic** is the internal code used to follow those commands or analyze sensor data.

- The **hardware interface** is the code used to read sensors and directly control hardware like motors or pneumatics.

Data logging of inputs should occur between the control logic and hardware interface - this ensures that any control logic can be replayed in the simulator. We suggest restructuring the subsystem such that hardware interfacing occurs in a separate object (we call this the "IO" layer). The IO layer includes an interface defining all methods used for interacting with the hardware along with one or more implementations that make use of vendor libraries to carry out commands and read data.

![Diagram of restructured subsystem](resources/subsystem-2.png)

> Note: You can refer to the [AdvantageKit examples](INSTALLATION.md#new-projects) or [6328's 2022 robot code](https://github.com/Mechanical-Advantage/RobotCode2022/tree/main/src/main/java/frc/robot/subsystems) for some reference IO interfaces and implementations.

Outputs (setting voltage, setpoint, PID constants, etc.) make use of simple methods for each command. Input data is more controlled such that it can be logged and replayed. Each IO interface defines a class with public attributes for all input data, along with methods for saving and replaying that data from a log (`toLog` and `fromLog`). We recommend using the [`@AutoLog`](#autolog-annotation) annotation to generate these methods automatically.

The IO layer includes a single method (`updateInputs`) for updating all of the input data. The subsystem class contains an instance of both the current IO implementation and the "inputs" object. Once per cycle, it updates the input data and sends it to the logging framework:

```java
io.updateInputs(inputs); // Update input data from the IO layer
Logger.processInputs("ExampleSubsystem", inputs); // Send input data to the logging framework (or update from the log during replay)
```

The rest of the subsystem then reads data from this inputs object rather than directly from the IO layer. This structure ensures that:

- The logging framework has access to all of the data being logged and can insert data from the log during replay.
- Throughout each cycle, all code making use of the input data reads the same values - the cache is never updated _during a cycle_. This means that the data replayed from the log appears identical to the data read on the real robot.

All of the IO methods include a default implementation which is used during simulation. We suggest setting up each subsystem accept the IO object as a constructor argument, so that the central robot class (like `RobotContainer`) can decide whether or not to use real hardware:

```java
public RobotContainer() {
    if (isReal()) {
        // Instantiate IO implementations to talk to real hardware
        driveTrain = new DriveTrain(DriveTrainIOReal());
        elevator = new Elevator(ElevatorIOReal());
        intake = new Intake(IntakeIOReal());
    } else {
        // Use anonymous classes to create "dummy" IO implementations
        driveTrain = new DriveTrain(DriveTrainIO() {});
        elevator = new Elevator(ElevatorIO() {});
        intake = new Intake(IntakeIO() {});
    }
}
```

> Note: We suggest the use of an IO layer to minimize the chance of interacting with hardware that doesn't exist. However, any structure will work where all input data flows through an inputs object implementing `LoggableInputs` and the two methods `fromLog` and `toLog`. Feel free to make use of whatever structure best fits your own requirements.

## `AutoLog` Annotation & Data Types

By adding the `@AutoLog` annotation to your inputs class, AdvantageKit will automatically generate implementations of `toLog` and `fromLog` for your inputs. All simple data types (including single values and arrays) are supported. [Structured data types](DATA-FLOW.md#structured-data-types) and enum values are also supported, so geometry objects like `Rotation2d` and `Pose3d` can be directly used as inputs.

For example:

```java
@AutoLog
public class MyInputs {
    public double myNumber = 0.0;
    public Pose2d myPose = new Pose2d();
    public MyEnum myEnum = MyEnum.VALUE;
}
```

This will generate the following class:

```java
class MyInputsAutoLogged extends MyInputs implements LoggableInputs {
    public void toLog(LogTable table) {
        table.put("MyNumber", myField);
        table.put("MyPose", myPose);
        table.put("MyEnum", myEnum);
    }

    public void fromLog(LogTable table) {
        myNumber = table.get("MyNumber", myNumber);
        myPose = table.get("MyPose", myPose);
        myEnum = table.get("MyEnum", myEnum);
    }
}
```

Note that you should use the `<className>AutoLogged` class, rather than your annotated class. The [AdvantageKit examples projects](INSTALLATION.md#new-projects) are a useful reference for how to use `@AutoLog` in a full project.

## Dashboard Options & NetworkTables Inputs

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
