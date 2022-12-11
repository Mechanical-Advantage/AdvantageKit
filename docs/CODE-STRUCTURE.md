# Code Structure & IO Layers

## Subsystems and IO Layers

By necessity, any interaction with external hardware must be isolated such that all input data is logged and can be replayed in the simulator where that hardware is not present. Most hardware interaction occurs in subsystem classes (read [this section](#dashboard-options--networktables-inputs) for information on using NetworkTables as an input). Traditionally, a subsystem has three main components:

![Diagram of traditional subsystem](resources/subsystem-1.png)

- The **public interface** consists of methods used by the rest of the robot code to control the subsystem.

- The **control logic** is the internal code used to follow those commands or analyze sensor data.

- The **hardware interface** is the code used to read sensors and directly control hardware like motors or pneumatics.

Data logging of inputs should occur between the control logic and hardware interface - this ensures that any control logic can be replayed in the simulator. We suggest restructuring the subsystem such that hardware interfacing occurs in a separate object (we call this the "IO" layer). The IO layer includes an interface defining all methods used for interacting with the hardware along with one or more implementations that make use of vendor libraries to carry out commands and read data.

![Diagram of restructured subsystem](resources/subsystem-2.png)

> Note: You can refer to the [AdvantageKit command-based example](INSTALLATION.md#new-projects) or [6328's 2022 robot code](https://github.com/Mechanical-Advantage/RobotCode2022/tree/main/src/main/java/frc/robot/subsystems) for some example IO interfaces and implementations.

Outputs (setting voltage, setpoint, PID constants, etc.) make use of simple methods for each command. Input data is more controlled such that it can be logged and replayed. Each IO interface defines a class with public attributes for all input data, along with methods for saving and replaying that data from a log (`toLog` and `fromLog`). We recommend using the [`@AutoLog`](#autolog-annotation) annotation to generate these methods automatically.

The IO layer includes a single method (`updateInputs`) for updating all of the input data. The subsystem class contains an instance of both the current IO implementation and the "inputs" object. Once per cycle, it updates the input data and sends it to the logging framework:

```java
io.updateInputs(inputs); // Update input data from the IO layer
Logger.getInstance().processInputs("ExampleSubsystem", inputs); // Send input data to the logging framework (or update from the log during replay)
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

## Logging Outputs

Output data consists of any calculated values which could be recreated in the simulator, including...

- Odometry pose

- Motor voltages

- Pneumatics commands

- Status data for drivers

- Internal object state

The logging framework supports recording this output data on the real robot and during replay. Essential data like the odometry pose are recorded on the real robot for convenience; even if it can be recreated in a simulator, that's often not a viable option in the rush to fix a problem between matches. During replay, recording extra output data is the primary method of debugging the code - logging calls can be added anywhere as they don't interfere with the replayed control logic. Any loggable data type [(see here)](DATA-FLOW.md) can be saved as an output like so:

```java
Logger.getInstance().recordOutput("Flywheel/Setpoint", setpointSpeed);
Logger.getInstance().recordOutput("FeederState", "IDLE");
Logger.getInstance().recordOutput("Drive/CalculatedLeftVolts", leftVolts);
```

> Note: This data is automatically saved to the `RealOutputs` or `ReplayOutputs` table, and it can be divided further into subtables using slashes (as seen above).

Logging geometry objects like `Pose2d`, `Trajectory`, etc. is common in robot code. AdvantageKit includes the following functions to easily log these objects in the formats expected by AdvantageScope:

```java
// Pose2d
Pose2d poseA, poseB, poseC;
Logger.getInstance().recordOutput("MyPose2d", poseA);
Logger.getInstance().recordOutput("MyPose2dArray", poseA, poseB);
Logger.getInstance().recordOutput("MyPose2dArray", new Pose2d[] { poseA, poseB });

// Pose3d
Pose3d poseA, poseB, poseC;
Logger.getInstance().recordOutput("MyPose3d", poseA);
Logger.getInstance().recordOutput("MyPose3dArray", poseA, poseB);
Logger.getInstance().recordOutput("MyPose3dArray", new Pose3d[] { poseA, poseB });

// Trajectory
Trajectory trajectory;
Logger.getInstance().recordOutput("MyTrajectory", trajectory);

// SwerveModuleState
SwerveModuleState stateA, stateB, stateC, stateD;
Logger.getInstance().recordOutput("MySwerveModuleStates", stateA, stateB, stateC, stateD);
Logger.getInstance().recordOutput("MySwerveModuleStates", new SwerveModuleState[] { stateA, stateB, stateC, stateD });
```

## `@AutoLog` Annotation

As of version 1.8, a new `@AutoLog` annotation was added. By adding this annotation to your inputs class, AdvantageKit will automatically generate implementations of `toLog` and `fromLog` for your inputs.

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

Note that you should use the `<className>AutoLogged` class, rather than your annotated class. The [AdvantageKit command-based project](INSTALLATION.md#new-projects) is a good example of the `@AutoLog` annotation in a full project.
