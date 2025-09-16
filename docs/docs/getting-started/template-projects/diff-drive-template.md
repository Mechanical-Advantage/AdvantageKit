---
sidebar_position: 2
---

# Differential Drive Template

The differential drive template is designed for drives based on Spark Max, Talon FX, or Talon SRX controllers and a NavX, Pigeon 2, or similar gyro. Some of the key features of the template include:

- On-controller feedback loops
- Physics simulation
- Automated characterization routines
- Pose estimator integration (not including vision)
- **Deterministic replay** with a **guarantee of accuracy**

:::info
The AdvantageKit differential drive template is **open-source** and **fully customizable**:

- **No black boxes:** Users can view and adjust all layers of the drive control stack.
- **Customizable:** IO implementations can be adjusted to support any hardware configuration (see the [customization](#customization) section).
- **Replayable:** Every aspect of the drive control logic, pose estimator, etc. can be replayed and logged in simulation using AdvantageKit's deterministic replay features with _guaranteed accuracy_.

:::

## Setup

1. Download the differential drive template project from the AdvantageKit release on GitHub and open it in VSCode.

2. Click the WPILib icon in the VSCode toolbar and find the task `WPILib: Set Team Number`. Enter your team number and press enter.

3. If not already available, download and install [Git](https://git-scm.com/downloads).

4. If the project will run **only on the roboRIO 2**, uncomment lines 39-42 of `build.gradle`. These contain additional [garbage collection](https://www.geeksforgeeks.org/garbage-collection-java/) optimizations for the RIO 2 to improve performance.

5. Navigate to `src/main/java/frc/robot/subsystems/drive/DriveConstants.java` in the AdvantageKit project.

6. Update the value of `motorReduction` based on the robot's gearing. These values represent reductions and should generally be greater than one.

7. Update the value of `trackWidth` based on the distance between the left and right sets of wheels.

8. Update the value of `wheelRadiusMeters` to the theoretical radius of each wheel. This value can be further refined as described in the "Tuning" section below.

9. Update the value of `maxSpeedMetersPerSec` to the theoretical max speed of the robot. This value can be further refined as described in the "Tuning" section below.

10. Set the value of `pigeonCanId` to the correct CAN ID of the Pigeon 2 (as configured using Tuner X). **If using a NavX instead of a Pigeon 2, see the [customization](#customization) section below.**

11. Set values of the left and right leader and follower motors to the correct CAN IDs of the drive controllers (as configured in Phoenix Tuner or REV Hardware Client).

12. In the constructor of `RobotContainer`, switch the IO implementations instantiated for the drive based on your chosen hardware. The default is the Talon SRX and Pigeon 2.

13. Deploy the project to the robot and connect using AdvantageScope.

14. Check that there are no dashboard alerts or errors in the Driver Station console. If any errors appear, verify that CAN IDs, firmware versions, and configurations of all devices.

:::warning
The project is configured to save log files when running on a real robot. **A FAT32 formatted USB stick must be connected to one of the roboRIO USB ports to save log files.**
:::

14. Manually rotate each side of the drive and view the position in AdvantageScope (`/Drive/Module.../DrivePositionRad`). Verify that the units visible in AdvantageScope (radians) match the physical motion of the module, and that positive motion corresponds to forward movement of the robot. If necessary, change the value of `motorReduction`, `leftInverted`, or `righInverted`.

## Tuning

### Feedforward Characterization

The project includes default [feedforward gains](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-feedforward.html#introduction-to-dc-motor-feedforward) for velocity control (`kS` and `kV`).

The project includes a simple feedforward routine that can be used to quickly measure the drive `kS` and `kV` values without requiring [SysId](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html):

1. Place the robot in an open space.

2. Select the "Drive Simple FF Characterization" auto routine.

3. Enable the robot in autonomous. The robot will slowly accelerate forwards, similar to a SysId quasistic test.

4. Disable the robot after at least ~5-10 seconds.

5. Check the console output for the measured `kS` and `kV` values, and copy them to the `realKs` and `realKv` constants in `DriveConstants.java`.

:::info
The feedforward model used in simulation can be characterized using the same method. **Simulation gains are stored in the `simKs` and `simKv` constants.**
:::

Users who wish to characterize acceleration gains (`kA`) can choose to use the full [SysId](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html) application. The project includes auto routines for each of the four required SysId tests. Two options are available to load data in SysId:

- For Spark users, the project is configured to use [URCL](https://docs.advantagescope.org/more-features/urcl) by default. This data can be exported as described [here](https://docs.advantagescope.org/more-features/urcl#sysid-usage).
- TalonFX (**not Talon SRX**) users can export the Hoot log file as described [here](https://pro.docs.ctr-electronics.com/en/latest/docs/api-reference/wpilib-integration/sysid-integration/index.html).
- Export the AdvantageKit log file as described [here](/data-flow/sysid-compatibility).

### Wheel Radius Characterization

The effective wheel radius of a robot tends to change over time as wheels are worn down, swapped, or compress into the carpet. This can have significant impacts on odometry accuracy. We recommend regularly recharacterizing wheel radius to combat these issues.

We recommend the following process to measure wheel radius:

1. Place the robot on carpet. Characterizing on a hard floor may produce errors in the measurement, as the robot's effective wheel radius is affected by carpet compression.

2. Using AdvantageScope, record the values of `/Drive/LeftPositionRad` and `/Drive/RightPositionRad`.

3. Manually push the robot directly forward as far as possible (at least 10 feet).

4. Using a tape measure, record the linear distance traveled by the robot.

5. Record the new values of `/Drive/LeftPositionRad` and `/Drive/RightPositionRad`.

6. The wheel radius is equal to `linear distance / wheel delta in radians`. The units of radius will match the units of the linear measurement.

### Velocity PID Tuning

The project includes default gains for the velocity PID controller, which can be found in the "Velocity PID configuration" section of `DriveConstants.java`. These gains should be tuned for each robot.

:::tip
More information about PID tuning can be found in the [WPILib documentation](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-pid.html#introduction-to-pid).
:::

We recommend using AdvantageScope to plot the measured and setpoint values while tuning. Measured values are published to the `/Drive/LeftVelocityRadPerSec` and `/Drive/RightVelocityRadPerSec` fields and setpoint values are published to the `/RealOutputs/Drive/LeftSetpointRadPerSec` and `/RealOutputs/Drive/RightSetpointRadPerSec` fields.

:::info
The PID gains used in simulation can be tuned using the same method. **Simulation gains are stored separately from "real" gains in `DriveConstants.java`.**
:::

### Max Speed Measurement

The effective maximum speed of a robot is typically slightly less than the theroetically max speed based on motor free speed and gearing. To ensure that the robot remains controllable at high speeds, we recommend measuring the effective maximum speed of the robot.

1. Set `maxSpeedMetersPerSec` in `DriveConstants.java` to the theoretical max speed of the robot based on motor free speed and gearing.

2. Place the robot in an open space.

3. Plot the measured robot speed in AdvantageScope using the `/RealOutputs/Drive/LeftVelocityMetersPerSec` and `/RealOutputs/Drive/RightVelocityMetersPerSec` fields.

4. In teleop, drive forward at full speed until the robot's velocity is no longer increasing.

5. Record the maximum velocity achieved and update the value of `maxSpeedMetersPerSec`.

### PathPlanner Configuration

The project includes a built-in configuration for [PathPlanner](https://pathplanner.dev), located in the constructor of `Drive.java`. You may wish to manually adjust the robot mass, MOI, and wheel coefficient as configured at the bottom of `DriveConstants.java`

## Customization

### Custom Gyro Implementations

The project defaults to the Pigeon 2 gyro, but can be integrated with any standard gyro. An example implementation for a NavX is included.

To change the gyro implementation, switch `new GyroIOPigeon2()` in the `RobotContainer` constructor to any other implementation. For example, the `GyroIONavX` implementation is pre-configured to use a NavX connected to the MXP SPI port. See the page on [IO interfaces](/data-flow/recording-inputs/io-interfaces) for more details on how hardware abstraction works.

### Custom Motor Implementations

The implementation of `ModuleIO` can be freely customized to support alternative hardware configurations, including robots without encoders. For example, the `DriveIOSpark` implementation can be customized for brushed motors by changing `MotorType.kBrushless` to `MotorType.kBrushed` and configuring the encoder counts per revolution by calling `config.encoder.countsPerRevolution(...)`.

### Vision Integration

The `Drive` subsystem uses WPILib's [`DifferentialDrivePoseEstimator`](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/math/estimator/DifferentialDrivePoseEstimator.html) class for odometry updates. The subsystem exposes the `addVisionMeasurement` method to enable vision systems to publish samples.

:::tip
This project is compatible with AdvantageKit's [vision template project](./vision-template.md), which provides a starting point for implementing a pose estimation algorithm based on Limelight or PhotonVision.
:::

### Real-Time Thread Priority

Optionally, the main thread can be configured to use [real-time](https://blogs.oracle.com/linux/post/task-priority) priority when running the command scheduler by removing the comments [here](https://github.com/Mechanical-Advantage/AdvantageKit/blob/a86d21b27034a36d051798e3eaef167076cd302b/template_projects/sources/diff_drive/src/main/java/frc/robot/Robot.java#L94) and [here](https://github.com/Mechanical-Advantage/AdvantageKit/blob/a86d21b27034a36d051798e3eaef167076cd302b/template_projects/sources/diff_drive/src/main/java/frc/robot/Robot.java#L104) (**IMPORTANT:** You must uncomment _both_ lines). This may improve the consistency of loop cycle timing in some cases, but should be used with caution as it will prevent other threads from running during the user code loop cycle (including internal threads required by NetworkTables, vendors, etc).

This customization **should only be used if the loop cycle time is significantly less than 20ms**, which allows other threads to continue running between user code cycles. We always recommend **thoroughly testing this change** to ensure that it does not cause unintended side effects (examples include NetworkTables lag, CAN timeouts, etc). In general, **this customization is only recommended for advanced users** who understand the potential side-effects.
