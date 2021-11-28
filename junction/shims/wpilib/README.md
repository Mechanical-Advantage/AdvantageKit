# junction/shims/wpilib

Replaces existing WPILib classes to read data from Junction instead of the HAL:

* [`DriverStation.java`](/junction/shims/wpilib/src/edu/wpi/first/wpilibj/DriverStation.java) - Reads joystick data from "junction/core", all other functions are unmodified. This allows of synronization of data within each cycle and guarantees that data read on the robot and in the simulator are identical.
* [`RobotController.java`](/junction/shims/wpilib/src/edu/wpi/first/wpilibj/RobotController.java) - Reads the FPGA timestamp from "junction/core", all other functions are unmodified. This means that the timestamp is constant within each cycle and can be replayed accurately even when running faster than real time.

## Interface

The interfaces of each shim match the origin WPILib classes, replacing only the implementations. No modified is required in user code to use the shims.

## Building

Two rules are provided for building the WPILib shims:

* wpilib - Builds the shim classes independently.
* wpilibj_shimmed - Exports stock WPILib with the shimmed classes. When building, classes with matching identifiers are replaced.

## Testing (REVIEW)

The "jtest" rule checks that the method signatures of each class match the stock WPILib versions.