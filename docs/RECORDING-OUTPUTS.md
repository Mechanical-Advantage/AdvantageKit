# Recording Outputs

Output data consists of any calculated values which could be recreated in the simulator, including...

- Odometry pose
- Motor voltages
- Pneumatics commands
- Status data for drivers
- Internal object state

The logging framework supports recording this output data on the real robot and during replay. Essential data like the odometry pose are recorded on the real robot for convenience; even if it can be recreated in a simulator, that's often not a viable option in the rush to fix a problem between matches. During replay, recording extra output data is the primary method of debugging the code - logging calls can be added anywhere as they don't interfere with the replayed control logic. Any loggable data type ([see here](DATA-FLOW.md#simple-data-types)) can be saved as an output like so:

```java
Logger.recordOutput("Flywheel/Setpoint", setpointSpeed);
Logger.recordOutput("FeederState", "IDLE");
Logger.recordOutput("Drive/CalculatedLeftVolts", leftVolts);
```

> Note: This data is automatically saved to the `RealOutputs` or `ReplayOutputs` table, and it can be divided further into subtables using slashes (as seen above).

Logging geometry objects like `Pose2d`, `Trajectory`, etc. is common in robot code. Many WPILib classes can be serialized to binary data using [structs](https://github.com/wpilibsuite/allwpilib/blob/main/wpiutil/doc/struct.adoc) or [protobufs](https://protobuf.dev). These objects can be logged as single values or arrays:

```java
// Pose2d
Pose2d poseA, poseB, poseC;
Logger.recordOutput("MyPose2d", poseA);
Logger.recordOutput("MyPose2dArray", poseA, poseB);
Logger.recordOutput("MyPose2dArray", new Pose2d[] { poseA, poseB });

// Pose3d
Pose3d poseA, poseB, poseC;
Logger.recordOutput("MyPose3d", poseA);
Logger.recordOutput("MyPose3dArray", poseA, poseB);
Logger.recordOutput("MyPose3dArray", new Pose3d[] { poseA, poseB });

// Trajectory
Trajectory trajectory;
Logger.recordOutput("MyTrajectory", trajectory);

// SwerveModuleState
SwerveModuleState stateA, stateB, stateC, stateD;
Logger.recordOutput("MySwerveModuleStates", stateA, stateB, stateC, stateD);
Logger.recordOutput("MySwerveModuleStates", new SwerveModuleState[] { stateA, stateB, stateC, stateD });
```

## `AutoLogOutput` Annotation

The `@AutoLogOutput` annotation can also be used to automatically log the value of a field or getter method as an output periodically (including private fields and methods). The key will be selected automatically, or it can be overriden using the `key` parameter. All data types are supported, including arrays and structured data types.

```java
public class Example {
    @AutoLogOutput // Logged as "Example/MyPose"
    private Pose2d myPose = new Pose2d();

    @AutoLogOutput(key = "Custom/Speeds")
    public double[] getSpeeds() {
        ...
    }
}
```

> Note: The parent class where `@AutoLogOutput` is used must be instantiated within the first loop cycle, be accessible by a recursive search of the fields of `Robot`, and be within the same package as `Robot` (or a subpackage). This feature is primarily intended to log outputs from subsystems and other similar classes. For classes that do not fit the criteria above, call `Logger.recordOutput` periodically to record outputs.

## Mechanism2d

AdvantageKit can also log [`Mechanism2d`](https://docs.wpilib.org/en/stable/docs/software/dashboards/glass/mech2d-widget.html) objects as outputs, which can be viewed using AdvantageScope. If not using `@AutoLogOutput`, note that the logging call only records the current state of the `Mechanism2d` and so it must be called periodically.

```java
public class Example {
    @AutoLogOutput // Auto logged as "Example/Mechanism"
    private Mechanism2d mechanism = new Mechanism2d(3, 3);

    public void periodic() {
        // Alternative approach if not using @AutoLogOutput
        // (Must be called periodically)
        Logger.recordOutput("Example/Mechanism", mechanism);
    }
}
```

## Console

Console output is automatically logged by AdvantageKit to the "Console" field, and can be viewed using AdvantageScope's 💬 [Console](https://github.com/Mechanical-Advantage/AdvantageScope/blob/main/docs/tabs/CONSOLE.md) tab. Note that output from native code is not included when running in simulation.
