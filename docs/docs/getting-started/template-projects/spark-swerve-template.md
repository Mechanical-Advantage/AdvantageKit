---
sidebar_position: 3
---

# Spark Swerve Template

AdvantageKit includes two swerve project templates with built-in support for advanced features:

- High-frequency odometry
- On-controller feedback loops
- Physics simulation
- Automated characterization routines
- Dashboard alerts for disconnected devices
- Pose estimator integration
- Step-by-step setup and tuning instructions with a prebuilt AdvantageScope layout
- **Deterministic replay** with a **guarantee of accuracy**

By default, the Spark version of the swerve template is configured for robots with **MAXSwerve modules, four NEO Vortex drive motors, four NEO 550 turn motors, four duty cycle absolute encoders, and a NavX or Pigeon 2 gyro**. See the [TalonFX Swerve Template](talonfx-swerve-template.md) for swerve robots using Talon FX.

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

1. Download the Spark swerve template project from the AdvantageKit release on GitHub and open it in VSCode.

2. Click the WPILib icon in the VSCode toolbar and find the task `WPILib: Set Team Number`. Enter your team number and press enter.

3. If not already available, download and install [Git](https://git-scm.com/downloads).

4. If the project will run **only on the roboRIO 2**, uncomment lines 39-42 of `build.gradle`. These contain additional [garbage collection](https://www.geeksforgeeks.org/garbage-collection-java/) optimizations for the RIO 2 to improve performance.

5. Navigate to `src/main/java/frc/robot/subsystems/drive/DriveConstants.java` in the AdvantageKit project.

6. Update the values of `driveMotorReduction` and `turnMotorReduction` based on the robot's module type and configuration. This information can typically be found on the product page for the swerve module. These values represent reductions and should generally be greater than one.

7. Update the values of `trackWidth` and `wheelBase` based on the distance between the left-right and front-back modules (respectively).

8. Update the value of `wheelRadiusMeters` to the theoretical radius of each wheel. This value can be further refined as described in the "Tuning" section below.

9. Update the value of `maxSpeedMetersPerSec` to the theoretical max speed of the robot. This value can be further refined as described in the "Tuning" section below.

10. Set the value of `pigeonCanId` to the correct CAN ID of the Pigeon 2 (as configured using Tuner X). **If using a NavX instead of a Pigeon 2, see the [customization](#customization) section below.**

11. For each module, set the values of `...DriveMotorId` and `...TurnMotorId` to the correct CAN IDs of the drive Spark Flex and turn Spark Max (as configured in the REV Hardware Client).

12. For each module, set the value of `...ZeroRotation` to `new Rotation2d(0.0)`.

13. Deploy the project to the robot and connect using AdvantageScope.

14. Check that there are no dashboard alerts or errors in the Driver Station console. If any errors appear, verify that CAN IDs, firmware versions, and configurations of all devices.

:::warning
The project is configured to save log files when running on a real robot. **A FAT32 formatted USB stick must be connected to one of the roboRIO USB ports to save log files.**
:::

14. Manually rotate the turning position of each module such that the position in AdvantageScope (`/Drive/Module.../TurnPosition`) is **increasing**. The module should be rotating **counter-clockwise** as viewed from above the robot. Verify that the units visible in AdvantageScope (radians) match the physical motion of the module. If necessary, change the value of `turnInverted`, `turnEncoderInverted`, or `turnMotorReduction`.

15. Manually rotate each drive wheel and view the position in AdvantageScope (`/Drive/Module.../DrivePositionRad`). Verify that the units visible in AdvantageScope (radians) match the physical motion of the module. If necessary, change the value of `driveMotorReduction`.

16. Manually rotate each module to align it directly forward. **Verify using AdvantageScope that the drive position _increases_ when the wheel rotates such that the robot would be propelled forward.** We recommend pressing a straight object such as aluminum tubing against the pairs of left and right modules to ensure accurate alignment.

17. Record the value of `/Drive/Module.../TurnPosition` for each aligned module. Update the value of `...ZeroRotation` for each module to `new Rotation2d(<insert value>)`.

## Tuning

### Feedforward Characterization

The project includes default [feedforward gains](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-feedforward.html#introduction-to-dc-motor-feedforward) for velocity control of the drive motors (`kS` and `kV`).

The project includes a simple feedforward routine that can be used to quickly measure the drive `kS` and `kV` values without requiring [SysId](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html):

1. Tune turning PID gains as described [here](#driveturn-pid-tuning).

2. Place the robot in an open space.

3. Select the "Drive Simple FF Characterization" auto routine.

4. Enable the robot in autonomous. The robot will slowly accelerate forward, similar to a SysId quasistic test.

5. Disable the robot after at least ~5-10 seconds.

6. Check the console output for the measured `kS` and `kV` values, and copy them to the `driveKs` and `driveKv` constants in `DriveConstants.java`.

:::info
The feedforward model used in simulation can be characterized using the same method. **Simulation gains are stored in the `driveSimKs` and `driveSimKv` constants.**
:::

Users who wish to characterize acceleration gains (`kA`) can choose to use the full [SysId](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/system-identification/index.html) application. The project includes auto routines for each of the four required SysId tests. Two options are available to load data in SysId:

- The project is configured to use [URCL](https://docs.advantagescope.org/more-features/urcl) by default. This data can be exported as described [here](https://docs.advantagescope.org/more-features/urcl#sysid-usage).
- Export the AdvantageKit log file as described [here](/data-flow/sysid-compatibility).

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
    io.setTurnPosition(
        Rotation2d.fromDegrees(
            switch (index) {
              case 0 -> 135.0;
              case 1 -> 45.0;
              case 2 -> -135.0;
              case 3 -> -45.0;
              default -> 0.0;
            }));
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

5. Check the console output for the measured wheel radius, and copy the value to `wheelRadiusMeters` in `DriveConstants.java`.

### Drive/Turn PID Tuning

The project includes default gains for the drive velocity PID controllers and turn position PID controllers, which can be found in the "Drive PID configuration" and "Turn PID configuration" sections of `DriveConstants.java`. These gains should be tuned for each robot.

:::tip
More information about PID tuning can be found in the [WPILib documentation](https://docs.wpilib.org/en/stable/docs/software/advanced-controls/introduction/introduction-to-pid.html#introduction-to-pid).
:::

We recommend using AdvantageScope to plot the measured and setpoint values while tuning. Measured values are published to the `/RealOutputs/SwerveStates/Measured` field and setpoint values are published to the `/RealOutputs/SwerveStates/SetpointsOptimized` field.

:::info
The PID gains used in simulation can be tuned using the same method. **Simulation gains are stored separately from "real" gains in `DriveConstants.java`.**
:::

### Max Speed Measurement

The effective maximum speed of a robot is typically slightly less than the theroetically max speed based on motor free speed and gearing. To ensure that the robot remains controllable at high speeds, we recommend measuring the effective maximum speed of the robot.

1. Set `maxSpeedMetersPerSec` in `DriveConstants.java` to the theoretical max speed of the robot based on motor free speed and gearing. This value can typically be found on the product page for your chosen swerve modules.

2. Place the robot in a open space.

3. Plot the measured robot speed in AdvantageScope using the `/RealOutputs/SwerveChassisSpeeds/Measured` field.

4. In teleop, drive forwards at full speed until the robot velocity is no longer increasing.

5. Record the maximum velocity achieved and update the value of `maxSpeedMetersPerSec`.

### Slip Current Measurement

The value of `driveMotorCurrentLimit` can be tuned to avoid slipping the wheels.

1. Place the robot against the solid wall.

2. Using AdvantageScope, plot the current of a drive motor from the `/Drive/Module.../DriveCurrentAmps` key, and the velocity of the motor from the `/Drive/Module.../DriveVelocityRadPerSec` key.

3. Accelerate forward until the drive velocity increases (the wheel slips). Note the current at this time.

4. Update the value of `driveMotorCurrentLimit` to this value.

### PathPlanner Configuration

The project includes a built-in configuration for [PathPlanner](https://pathplanner.dev), located in the constructor of `Drive.java`. You may wish to manually adjust the following values:

- Robot mass, MOI, and wheel coefficient as configured at the bottom of `DriveConstants.java`
- Drive PID constants as configured in `AutoBuilder`.
- Turn PID constants as configured in `AutoBuilder`.

## Customization

### Setting Odometry Frequency

By default, the project runs at **100Hz**. This value is stored as `odometryFrequency` at the top of `DriveConstants.java` and can be changed. The project configures all devices to minimize CAN bus utilization, but we recommend monitoring utilization carefully when increasing frequency.

### Switching Between Spark Max and Flex

Switching between the Spark Max and Spark Max for drive and turn motors is very simple. In the constructor of `ModuleIOSpark`, change the call instantiating the Spark object to use `CANSparkMax` or `CANSparkFlex`. The configuration object must also be changed to the corresponding `SparkMaxConfig` or `SparkFlexConfig` class.

When switching between motor types, the `driveGearbox` and `turnGearbox` constants in `DriveConstants` should be updated accordingly.

### Custom Gyro Implementations

The project defaults to the Pigeon 2 gyro, but can be integrated with any standard gyro. An example implementation for a NavX is included.

To change the gyro implementation, switch `new GyroIOPigeon2()` in the `RobotContainer` constructor to any other implementation. For example, the `GyroIONavX` implementation is pre-configured to use a NavX connected to the MXP SPI port. See the page on [IO interfaces](/data-flow/recording-inputs/io-interfaces) for more details on how hardware abstraction works.

The `SparkOdometryThread` class reads high-frequency gyro data for odometry alongside samples from drive encoders. This class supports both Spark devices and generic signals. Note that the gyro should be configured to publish signals at the same frequency as odometry. Call `registerSignal` with a double supplier to create a queue, as shown in the `GyroIONavX` implementation:

```java
Queue<Double> yawPositionQueue = SparkOdometryThread.getInstance().registerSignal(navX::getAngle);
```

:::info
Reference the full `GyroIONavX` implementation for an example of how to create a timestamp queue and update the odometry inputs for the gyro.
:::

### Custom Module Implementations

The implementation of `ModuleIOSpark` can be freely customized to support alternative hardware configurations, such as using a TalonFX-based drive motor. When integrating with TalonFX devices, we recommend referencing the implementation found in the `ModuleIOTalonFX` class of the [TalonFX Swerve Template](talonfx-swerve-template.md).

As described in the previous section, the `SparkOdometryThread` supports non-Spark signals through the `registerSignal` method. This allows devices from different vendors to be freely mixed.

By default, the project uses a duty cycle encoder connected to a turn Spark Max. When using another absolute encoder (such as a CANcoder or HELIUM Canandmag), we recommend resetting the relative encoder based on the absolute encoder; the relative encoder can then be used for PID control. In this case, the following changes are required:

1. Create the encoder object in `ModuleIOSpark` and configure it appropriately.

2. Change the feedback sensor source of the turn controller:

```java
turnEncoder = turnSpark.getEncoder(); // Change the type of turnEncoder to RelativeEncoder
turnConfig.closedLoopConfig.feedbackSensor(FeedbackSensor.kPrimaryEncoder); // Was: kAbsoluteEncoder
```

3. Replace `turnConfig.absoluteEncoder...` with `turnConfig.encoder...`, and `averageDepth` with `uvwAverageDepth`. Remove the setter for `inverted(...)`.

4. In the signal config for the turn motor, change all instances of `absoluteEncoderPosition...` and `absoluteEncoderVelocity...` to `primaryEncoderPosition...` and `primaryEncoderVelocity...`.

5. Incorporate the turn motor reduction in the encoder position and velocity factors:

```java
public static final double turnEncoderPositionFactor = 2 * Math.PI / turnMotorReduction; // Rotor Rotations -> Wheel Radians
public static final double turnEncoderVelocityFactor = (2 * Math.PI) / 60.0 / turnMotorReduction; // Rotor RPM -> Wheel Rad/Sec
```

6. Reset the relative encoder position at startup:

```java
tryUntilOk(turnSpark, 5, () -> turnEncoder.setPosition(customEncoder.getPositionRadians()));
```

### Vision Integration

The `Drive` subsystem uses WPILib's [`SwerveDrivePoseEstimator`](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/math/estimator/SwerveDrivePoseEstimator.html) class for odometry updates. The subsystem exposes the `addVisionMeasurement` method to enable vision systems to publish samples.

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

Optionally, the main thread can be configured to use [real-time](https://blogs.oracle.com/linux/post/task-priority) priority when running the command scheduler by removing the comments [here](https://github.com/Mechanical-Advantage/AdvantageKit/blob/a86d21b27034a36d051798e3eaef167076cd302b/template_projects/sources/spark_swerve/src/main/java/frc/robot/Robot.java#L94) and [here](https://github.com/Mechanical-Advantage/AdvantageKit/blob/a86d21b27034a36d051798e3eaef167076cd302b/template_projects/sources/spark_swerve/src/main/java/frc/robot/Robot.java#L104) (**IMPORTANT:** You must uncomment _both_ lines). This may improve the consistency of loop cycle timing in some cases, but should be used with caution as it will prevent other threads from running during the user code loop cycle (including internal threads required by NetworkTables, vendors, etc).

This customization **should only be used if the loop cycle time is significantly less than 20ms**, which allows other threads to continue running between user code cycles. We always recommend **thoroughly testing this change** to ensure that it does not cause unintended side effects (examples include NetworkTables lag, CAN timeouts, etc). In general, **this customization is only recommended for advanced users** who understand the potential side-effects.
