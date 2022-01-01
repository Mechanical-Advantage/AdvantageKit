# `junction/shims/wpilib`

> Replaces WPILib components to read data from `junction` instead of the HAL.

Replacing these classes means that user code can interact with WPILib components the same way as any other robot program. The Java export from this component is a full replacement for `wpilibj` that includes our shims. 

## Interface

The interfaces of each shim match the origin WPILib classes, replacing only the implementations. No modification is required in user code to use the shims. Below is a list of all modified classes:

* [`DriverStation.java`](src/edu/wpi/first/wpilibj/DriverStation.java) - Reads joystick data from [`junction/core`](/junction/core), most other functions are unmodified. This allows for synronization of data within each cycle and guarantees that data read on the robot and in the simulator are identical. Note that the method `updateControlWordFromCache` is unavailable as the class no longer reads a `ControlWord` from the HAL. This method is only used by `DSControlWord` by default, which remains functional.
* [`DSControlWord.java`](src/edu/wpi/first/wpilibj/DSControlWord.java) - Reads robot state directly from [`junction/core`](/junction/core) rather than `DriverStation`. The existing method of reading data from `DriverStation` required use of a `ControlWord` object, which the shim `DriverStation` is unable to create. The functionality of this class is identical.
* [`RobotController.java`](src/edu/wpi/first/wpilibj/RobotController.java) - Reads the FPGA timestamp from [`junction/core`](/junction/core), all other functions are unmodified. This means that the timestamp is constant within each cycle and can be replayed accurately even when running faster than real time.
* [`Tracer.java`](src/edu/wpi/first/wpilibj/Tracer.java) - Reads the real FPGA timestamp from the HAL through [`junction/core`](/junction/core) instead of relying on the modified `RobotController` class. This class needs to read the true timestamp to accurately record epoch times.
* [`Watchdog.java`](src/edu/wpi/first/wpilibj/Watchdog.java) - Reads the real FPGA timestamp from the HAL through [`junction/core`](/junction/core) instead of relying on the modified `RobotController` class. This class needs to read the true timestamp to correctly detect performance issues.

## Building

The following Bazel targets are available (defined in [BUILD](BUILD)):

* `wpilib` - Main Java library, depends on [`junction/core`](/junction/core) and WPILib for building. This library includes only the shim classes, not the rest of WPILib.
* `wpilib-export` - Maven export for publishing `wpilib`, used by GitHub Actions. This target includes runtime dependencies for both the original `wpilibj` and the `wpilib` shim target above. By explicitly depending on `wpilibj` (the original) first, the `java_export` rule replaces the duplicate class files with our shims.

To publish `junction/shims/wpilib` to your local Maven repository, use the following command:

```bash
bazel run --define "maven_repo=file://$HOME/.m2/repository" //junction/wpilib:wpilib-export.publish
```

As with all components published to Maven, the version number can be set with a command line flag:

```bash
bazel run --define "publishing_version=X.X.X" ...
```

## Testing

The `signature-test` rule checks that the method signatures of each class match the stock WPILib versions.