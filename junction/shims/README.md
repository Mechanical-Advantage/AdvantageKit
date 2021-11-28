# junction/shims

Replaces components of other libraries to interact directly with junction. This technique reduces the changes required in user code.

## Components

* [wpilib](/junction/shims/wpilib) - Replaces WPILib components to read data from Junction instead of the HAL.

## Interface

Each shim matches the interface of the class it replaces. See "junction/shims/wpilib" for details on the modified classes.

## Building

This component cannot be built on its own. See "junction/shims/wpilib" for building information.

## Testing

TODO.