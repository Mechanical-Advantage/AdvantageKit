---
sidebar_position: 4
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# TalonFX Swerve Template

AdvantageKit includes two swerve project templates with built-in support for advanced features:

- Easy setup with Tuner X swerve project generator
- High-frequency odometry
- CANivore time sync (for Phoenix Pro users)
- On-controller feedback loops
- Physics simulation
- Automated characterization routines
- Dashboard alerts for disconnected devices
- Pose estimator integration using standard FPGA timestamps
- Step-by-step setup and tuning instructions with a prebuilt AdvantageScope layout
- **Deterministic replay** with a **guarantee of accuracy**

By default, the TalonFX version of the swerve template is configured for robots with **four TalonFX drive motors, four TalonFX turn motors, four CANcoders, and a NavX or Pigeon 2 gyro**. See the [Spark Swerve Template](spark-swerve-template.md) for swerve robots using Spark Max/Flex.

:::info
The AdvantageKit swerve templates are **open-source** and **fully customizable**:

- **No black boxes:** Users can view and adjust all layers of the swerve control stack.
- **Customizable:** IO implementations can be adjusted to support any hardware configuration (see the [customization](#customization) section).
- **Replayable:** Every aspect of the swerve control logic, pose estimator, etc. can be replayed and logged in simulation using AdvantageKit's deterministic replay features with _guaranteed accuracy_.

:::

## Setup

:::tip
The swerve project folder includes a predefined AdvantageScope layout with tabs for each setup and tuning step described below. To open it, click `File` > `Import Layout...` in the tab bar of AdvantageScope and select the file `AdvantageScope Swerve Calibration.json` in the swerve project folder.
:::

<Tabs>
<TabItem value="tuner-x" label="Swerve Project Generator" default>

:::danger
CTRE only permits the swerve project generator to be used on swerve robots with **exclusively CTRE hardware** (eight TalonFX controllers, four CANcoders, and a Pigeon 2). Otherwise, switch to the "Manual" tab for standard setup instructions.
:::

1. Download the TalonFX swerve template project from the AdvantageKit release on GitHub and open it in VSCode.

2. Click the WPILib icon in the VSCode toolbar and find the task `WPILib: Set Team Number`. Enter your team number and press enter.

3. If not already available, download and install [Git](https://git-scm.com/downloads).

4. If the project will run **only on the roboRIO 2**, uncomment lines 39-42 of `build.gradle`. These contain additional [garbage collection](https://www.geeksforgeeks.org/garbage-collection-java/) optimizations for the RIO 2 to improve performance.

5. Follow the instructions in the Phoenix documentation for the [Tuner X Swerve Project Generator](https://v6.docs.ctr-electronics.com/en/latest/docs/tuner/tuner-swerve/index.html).

6. On the final screen in Tuner X, choose "Generate only TunerConstants" and overwrite the file located at `src/main/java/frc/robot/generated/TunerConstants.java`.

7. In `TunerConstants.java`, comment out the [last import](https://github.com/CrossTheRoadElec/Phoenix6-Examples/blob/88be410fdbfd811e6f776197d41c0bea5f109b0e/java/SwerveWithPathPlanner/src/main/java/frc/robot/generated/TunerConstants.java#L17) and [last method](https://github.com/CrossTheRoadElec/Phoenix6-Examples/blob/88be410fdbfd811e6f776197d41c0bea5f109b0e/java/SwerveWithPathPlanner/src/main/java/frc/robot/generated/TunerConstants.java#L198-L202). Before removing them, both lines will be marked as errors in VSCode.

8. In `TunerConstants.java`, change `kSteerInertia` to 0.004 and `kDriveInertia` to 0.025.

:::warning
The project is configured to save log files when running on a real robot. **A FAT32 formatted USB stick must be connected to one of the roboRIO USB ports to save log files.**
:::

</TabItem>
<TabItem value="manual" label="Manual" default>

1. Download the TalonFX swerve template project from the AdvantageKit release on GitHub and open it in VSCode.

2. Click the WPILib icon in the VSCode toolbar and find the task `WPILib: Set Team Number`. Enter your team number and press enter.

3. If not already available, download and install [Git](https://git-scm.com/downloads).

4. If the project will run **only on the roboRIO 2**, uncomment lines 39-42 of `build.gradle`. These contain additional [garbage collection](https://www.geeksforgeeks.org/garbage-collection-java/) optimizations for the RIO 2 to improve performance.

5. Navigate to `src/main/java/frc/robot/generated/TunerConstants.java` in the AdvantageKit project.

6. Update the values of `kDriveGearRatio` and `kSteerGearRatio` based on the robot's module type and configuration. This information can typically be found on the product page for the swerve module. These values represent reductions and should generally be greater than one.

7. Update the value of `kWheelRadius` to the theoretical radius of each wheel. This value can be further refined as described in the "Tuning" section below.

8. Update the value of `kSpeedAt12Volts` to the theoretical max speed of the robot. This value can be further refined as described in the "Tuning" section below.

9. Update the value of `kCANBus` based on the CAN bus used by the drive devices. Check the [`CANBus`](https://api.ctr-electronics.com/phoenix6/latest/java/com/ctre/phoenix6/CANBus.html) API documentation for details on possible values.

10. Set the value of `kPigeonId` to the correct CAN ID of the Pigeon 2 (as configured using Tuner X). **If using a NavX instead of a Pigeon 2, see the [customization](#customization) section below.**

11. For each module, set the values of `k...DriveMotorId`, `k...SteerMotorId`, and `k...EncoderId` to the correct CAN IDs of the drive TalonFX, turn TalonFX, and CANcoder (as configured in Tuner X).

12. For each module, set the values of `k...XPos` and `k...YPos` based on the distance from each module to the center of the robot. Positive X values are closer to the front of the robot and positive Y values are closer to the left side of the robot.

13. For each module, set the value of `k...EncoderOffset` to `Radians.of(0.0)`.

14. Deploy the project to the robot and connect using AdvantageScope.

15. Check that there are no dashboard alerts or errors in the Driver Station console. If any errors appear, verify tha CAN IDs, firmware versions, and configurations of all devices.

:::warning
The project is configured to save log files when running on a real robot. **A FAT32 formatted USB stick must be connected to one of the roboRIO USB ports to save log files.**
:::

15. Manually rotate the turning position of each module such that the position in AdvantageScope (`/Drive/Module.../TurnPosition`) is **increasing**. The module should be rotating **counter-clockwise** as viewed from above the robot. Verify that the units visible in AdvantageScope (radians) match the physical motion of the module. If necessary, change the value of `k...SteerMotorInverted` or `kSteerGearRatio`.

16. Manually rotate each drive wheel and view the position in AdvantageScope (`/Drive/Module.../DrivePositionRad`). Verify that the units visible in AdvantageScope (radians) match the physical motion of the module. If necessary, change the value of `kDriveGearRatio`.

17. Manually rotate each module to align it directly forward. **Verify using AdvantageScope that the drive position _increases_ when the wheel rotates such that the robot would be propelled forward.** We recommend pressing a straight object such as aluminum tubing against the pairs of left and right modules to ensure accurate alignment.

18. Record the value of `/Drive/Module.../TurnPosition` for each aligned module. Update the value of `k...EncoderOffset` for each module to `Radians.of(<insert value>)`. **The value saved in `TunerConstants` must be the _negative_ of the value displayed in AdvantageScope (i.e. positive values become negative and vice versa).**

</TabItem>
</Tabs>

## Tuning

### Torque-Current Control

The project defaults to voltage control for both the drive and turn motors. Phoenix Pro subscribers can optionally switch to torque-current control, as described in the [Phoenix documentation](https://pro.docs.ctr-electronics.com/en/latest/docs/api-reference/device-specific/talonfx/talonfx-control-intro.html#torquecurrentfoc). This can be configured by changing the values of `kSteerClosedLoopOutput` and/or `kDriveClosedLoopOutput` in `TunerConstants.java` to `ClosedLoopOutputType.TorqueCurrentFOC`.

:::info
Torque-current control requires different gains than voltage control. We recommend following the steps below to tune feedforward and PID gains.
:::

### Feedforward Characterization

The project includes default [feedforward gains](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-feedforward.html#introduction-to-dc-motor-feedforward) for velocity control of the drive motors (`kS` and `kV`), acceleration control of the drive motors (`kA`), and velocity control of the turn motors (`kS` and `kV`).

:::info
The AdvantageKit template requires different feedforward gains than CTRE's default swerve code, because it applies the swerve gear ratio using the TalonFX firmware and not on the RIO.
:::

:::tip
The drive `kS` and `kV` gains should **always** be characterized (as described below). The drive/turn `kA` gains and turn `kS` and `kV` gains are unnecessary in most cases, but can be tuned by advanced users.
:::

The project includes a simple feedforward routine that can be used to quickly measure the drive `kS` and `kV` values without requiring [SysId](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html):

1. Tune turning PID gains as described [here](#driveturn-pid-tuning).

2. Place the robot in an open space.

3. Select the "Drive Simple FF Characterization" auto routine.

4. Enable the robot in autonomous. The robot will slowly accelerate forwards, similar to a SysId quasistic test.

5. Disable the robot after at least ~5-10 seconds.

6. Check the console output for the measured `kS` and `kV` values, and copy them to the `driveGains` config in `TunerConstants.java`.

:::info
The feedforward model used in simulation can be characterized using the same method. **Simulation gains are stored in `ModuleIOSim.java` instead of `TunerConstants.java`.**
:::

Users who wish to characterize acceleration gains (`kA`) or turn gains can choose to use the full [SysId](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html) application. The project includes auto routines for each of the four required SysId tests. Two options are available to load data in SysId:

- Export the Hoot log file as described [here](https://pro.docs.ctr-electronics.com/en/latest/docs/api-reference/wpilib-integration/sysid-integration/index.html).
- Export the AdvantageKit log file as described [here](/data-flow/sysid-compatibility). Note that AdvantageKit values are logged in radians while Phoenix requires rotations to be used. Gains must be converted appropriately.

:::tip
The built-in SysId routines can be easily adapted to characterize the turn motor feedforward or the angular motion of the robot (for example, to estimate the robot's [moment of inertia](https://sleipnirgroup.github.io/Choreo/usage/estimating-moi/)). The code below shows how the `runCharacterization` method can be adapted for these use cases.

```java
/** Characterize turn motor feedforward. */
public void runCharacterization(double output) {
    io.setDriveOpenLoop(0.0);
    io.setTurnOpenLoop(output);
}

/** Characterize robot angular motion. */
public void runCharacterization(double output) {
    io.setDriveOpenLoop(output);
    io.setTurnPosition(new Rotation2d(constants.LocationX, constants.LocationY).plus(Rotation2d.kCCW_Pi_2));
}
```

:::

### Wheel Radius Characterization

The effective wheel radius of a robot tends to change over time as wheels are worn down, swapped, or compress into the carpet. This can have significant impacts on odometry accuracy. We recommend regularly recharacterizing wheel radius to combat these issues.

The project includes an automated wheel radius characterization routine, which only requires enough space for the robot to rotate in place.

1. Place the robot on carpet. Characterizing on a hard floor may produce errors in the measurement, as the robot's effective wheel radius is affected by carpet compression.

2. Select the "Drive Wheel Radius Characterization" auto routine.

3. Enable the robot in autonomous. The robot will slowly rotate in place.

4. Disable the robot after at least one full rotation.

5. Check the console output for the measured wheel radius, and copy the value to `kWheelRadius` in `TunerConstants.java`.

### Drive/Turn PID Tuning

The project includes default gains for the drive velocity PID controllers and turn position PID controllers, which can be found in the `steerGains` and `driveGains` configs in `TunerConstants.java`. These gains should be tuned for each robot.

:::info
The AdvantageKit template requires different PID gains than CTRE's default swerve code, because it applies the swerve gear ratio using the TalonFX firmware and not on the RIO.
:::

:::tip
More information about PID tuning can be found in the [WPILib documentation](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-pid.html#introduction-to-pid).
:::

We recommend using AdvantageScope to plot the measured and setpoint values while tuning. Measured values are published to the `/RealOutputs/SwerveStates/Measured` field and setpoint values are published to the `/RealOutputs/SwerveStates/SetpointsOptimized` field.

:::info
The PID gains used in simulation can be tuned using the same method. **Simulation gains are stored in `ModuleIOSim.java` instead of `TunerConstants.java`.**
:::

### Max Speed Measurement

The effective maximum speed of a robot is typically slightly less than the theroetically max speed based on motor free speed and gearing. To ensure that the robot remains controllable at high speeds, we recommend measuring the effective maximum speed of the robot.

1. Set `kSpeedAt12Volts` in `TunerConstants.java` to the theoretical max speed of the robot based on motor free speed and gearing. This value can typically be found on the product page for your chosen swerve modules.

2. Place the robot in an open space.

3. Plot the measured robot speed in AdvantageScope using the `/RealOutputs/SwerveChassisSpeeds/Measured` field.

4. In teleop, drive forward at full speed until the robot's velocity is no longer increasing.

5. Record the maximum velocity achieved and update the value of `kSpeedAt12Volts`.

### Slip Current Measurement

The value of `kSlipCurrent` can be tuned to avoid slipping the wheels.

1. Place the robot against the solid wall.

2. Using AdvantageScope, plot the current of a drive motor from the `/Drive/Module.../DriveCurrentAmps` key, and the velocity of the motor from the `/Drive/Module.../DriveVelocityRadPerSec` key.

3. Accelerate forward until the drive velocity increases (the wheel slips). Note the current at this time.

4. Update the value of `kSlipCurrent` to this value.

### PathPlanner Configuration

The project includes a built-in configuration for [PathPlanner](https://pathplanner.dev), located in the constructor of `Drive.java`. You may wish to manually adjust the following values:

- Robot mass, MOI, and wheel coefficient as configured at the top of `Drive.java`
- Drive PID constants as configured in `AutoBuilder`.
- Turn PID constants as configured in `AutoBuilder`.

## Customization

### Setting Odometry Frequency

By default, the project runs at **100Hz** on the RIO CAN bus and **250Hz** on CAN FD buses. These values are stored at the top of `Drive.java` and can be freely customized. The project configures all devices to minimize CAN bus utilization, but we recommend monitoring utilization carefully when increasing frequency.

### Custom Gyro Implementations

The project defaults to the Pigeon 2 gyro, but can be integrated with any standard gyro. An example implementation for a NavX is included.

To change the gyro implementation, switch `new GyroIOPigeon2()` in the `RobotContainer` constructor to any other implementation. For example, the `GyroIONavX` implementation is pre-configured to use a NavX connected to the MXP SPI port. See the page on [IO interfaces](/data-flow/recording-inputs/io-interfaces) for more details on how hardware abstraction works.

The `PhoenixOdometryThread` class reads high-frequency gyro data for odometry alongside samples from drive encoders. This class supports both Phoenix signals and generic signals. Note that the gyro should be configured to publish signals at the same frequency as odometry. Call `registerSignal` with a double supplier to create a queue, as shown in the `GyroIONavX` implementation:

```java
Queue<Double> yawPositionQueue = PhoenixOdometryThread.getInstance().registerSignal(navX::getAngle);
```

:::info
Reference the full `GyroIONavX` implementation for an example of how to create a timestamp queue and update the odometry inputs for the gyro.
:::

### Custom Module Implementations

The implementation of `ModuleIOTalonFX` can be freely customized to support alternative hardware configurations, such as using a Spark Max as a turn controller. When integrating with Spark devices, we recommend referencing the implementation found in the `ModuleIOSpark` class of the [Spark Swerve Template](spark-swerve-template.md).

As described in the previous section, the `PhoenixOdometryThread` supports non-Phoenix signals through the `registerSignal` method. This allows devices from different vendors to be freely mixed.

By default, the project uses a CANcoder in remote/fused/sync mode. When using another absolute encoder (such as a duty cycle encoder or HELIUM Canandmag), we recommend reseting the relative encoder based on the absolute encoder; the relative encoder can then be used for PID control. In this case, the following changes are required:

1. Create the encoder object in `ModuleIOTalonFX` and configure it appropriately.

2. Change the feedback sensor source of the turn controller:

```java
turnConfig.Feedback.FeedbackSensorSource = FeedbackSensorSourceValue.RotorSensor;
```

3. Replace `RotorToSensorRatio` with `SensorToMechanismRatio` as shown below:

```java
turnConfig.Feedback.SensorToMechanismRatio = constants.SteerMotorGearRatio;
```

4. Reset the relative encoder position at startup:

```java
tryUntilOk(5, () -> turnTalon.setPosition(customEncoder.getPositionRotations(), 0.25));
```

### Profiled Turning PID

By default, the project uses standard PID controllers for turn control. Users may choose to replace the standard control request with [Motion Magic](https://pro.docs.ctr-electronics.com/en/latest/docs/api-reference/device-specific/talonfx/motion-magic.html#motion-magic) or [Motion Magic Expo](https://pro.docs.ctr-electronics.com/en/latest/docs/api-reference/device-specific/talonfx/motion-magic.html#motion-magic-expo) control requests. To implement this, simply replace the position request in `ModuleIOTalonFX` with the new request type, as shown below. The Motion Magic constraints are already configured in the `ModuleIOTalonFX` constructor, but can be adjusted.

```java
private final MotionMagicVoltage positionVoltageRequest = new MotionMagicVoltage(0.0);
private final MotionMagicTorqueCurrentFOC positionTorqueCurrentRequest = new MotionMagicTorqueCurrentFOC(0.0);
```

### Vision Integration

The `Drive` subsystem uses WPILib's [`SwerveDrivePoseEstimator`](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/math/estimator/SwerveDrivePoseEstimator.html) class for odometry updates. The subsystem exposes the `addVisionMeasurement` method to enable vision systems to publish samples.

Users migrating from CTRE's swerve library should note that the AdvantageKit template uses **standard FPGA timestamps** for pose estimation rather than CTRE's "current time." This means that pose estimates from Limelight or PhotonVision can be passed _directly_ to the pose estimator without needing to call [`Utils.fpgaToCurrentTime`](<https://api.ctr-electronics.com/phoenix6/release/java/com/ctre/phoenix6/Utils.html#fpgaToCurrentTime(double)>).

:::tip
This project is compatible with AdvantageKit's [vision template project](./vision-template.md), which provides a starting point for implementing a pose estimation algorithm based on Limelight or PhotonVision.
:::

### Swerve Setpoint Generator

The project already includes basic mechanisms to reduce skidding, such as drive current limits and cosine optimization. Users who prefer more control over module skidding may wish to utilize Team 254's swerve setpoint generator. Documentation for using the version of this algorithm bundled with PathPlanner can be found [here](https://pathplanner.dev/pplib-swerve-setpoint-generator.html). The `SwerveSetpointGenerator` should be instantiated in the `Drive` subsystem and used in the `runVelocity` method, as shown below:

```java
private final SwerveSetpointGenerator setpointGenerator;
private SwerveSetpoint previousSetpoint;

public Drive(...) {
    // ...

    setpointGenerator = new SwerveSetpointGenerator(...);
    previousSetpoint = new SwerveSetpoint(getChassisSpeeds(), getModuleStates(), DriveFeedforwards.zeroes(4));

    // ...
}

public void runVelocity(ChassisSpeeds speeds) {
    previousSetpoint = setpointGenerator.generateSetpoint(previousSetpoint, speeds, 0.02);
    SwerveModuleStatep[] setpointStates = previousSetpoint.moduleStates();

    // ...
}
```

### Advanced Physics Simulation

The project can be easily adapted to utilize Team 5516's [maple-sim](https://github.com/Shenzhen-Robotics-Alliance/Maple-Sim) library for simulation, which provides a full rigid-body simulation of the swerve drive and its interactions with the field. Check the documentation for more details on how to install and use the library.

### Real-Time Thread Priority

Optionally, the main thread can be configured to use [real-time](https://blogs.oracle.com/linux/post/task-priority) priority when running the command scheduler by removing the comments [here](https://github.com/Mechanical-Advantage/AdvantageKit/blob/a86d21b27034a36d051798e3eaef167076cd302b/template_projects/sources/talonfx_swerve/src/main/java/frc/robot/Robot.java#L110) and [here](https://github.com/Mechanical-Advantage/AdvantageKit/blob/a86d21b27034a36d051798e3eaef167076cd302b/template_projects/sources/talonfx_swerve/src/main/java/frc/robot/Robot.java#L120) (**IMPORTANT:** You must uncomment _both_ lines). This may improve the consistency of loop cycle timing in some cases, but should be used with caution as it will prevent other threads from running during the user code loop cycle (including internal threads required by NetworkTables, vendors, etc).

This customization **should only be used if the loop cycle time is significantly less than 20ms**, which allows other threads to continue running between user code cycles. We always recommend **thoroughly testing this change** to ensure that it does not cause unintended side effects (examples include NetworkTables lag, CAN timeouts, etc). In general, **this customization is only recommended for advanced users** who understand the potential side-effects.
