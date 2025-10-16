# 🔼 Recording Outputs

Output data consists of any calculated values which could be recreated in the simulator, including...

- Odometry pose
- Motor voltages
- Pneumatics commands
- Status data for drivers
- Internal object state

The logging framework supports recording this output data on the real robot and during replay. Essential data like the odometry pose are recorded on the real robot for convenience; even if it can be recreated in a simulator, that's often not a viable option in the rush to fix a problem between matches. During replay, recording extra output data is the primary method of debugging the code - logging calls can be added anywhere as they don't interfere with the replayed control logic. Any loggable data type ([see here](/data-flow/supported-types)) can be saved as an output like so:

```java
Logger.recordOutput("Flywheel/Setpoint", setpointSpeed);
Logger.recordOutput("Drive/Pose", odometryPose);
Logger.recordOutput("FeederState", FeederState.RUNNING);
```

:::info
This data is automatically saved to the `RealOutputs` or `ReplayOutputs` table, and it can be divided further into subtables using slashes (as seen above).
:::

## Structured Types

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

## Units

Double or float fields can be logged with unit metadata in multiple ways. This ensures that AdvantageScope will correctly [visualize unit data](https://docs.advantagescope.org/tab-reference/line-graph/units).

```java
// The unit can be specified as a string (stored as metadata)
Logger.recordOutput("MyDistance", 3.14, "meters");

// A unit object from the WPILib units library can be used in place of a string
Logger.recordOutput("MyDistance", 3.14, Meters);

// Measure values will also be saved with unit metadata
// (The raw value will use the user-specified unit, not the base unit)
Logger.recordOutput("MyDistance", Meters.of(3.14));

// This works too, but requires adding the unit to the field name
// (See the AdvantageScope docs linked above for details)
Logger.recordOutput("MyDistanceMeters", 3.14);
```
