# `junction/shims`

> Replaces components of other libraries (WPILib) to interact directly with `junction`.

Currently, only shim classes for WPILib are included. See [shims/wpilib](/junction/shims/wpilib).

## Components

* [wpilib](/junction/shims/wpilib) - Replaces WPILib components to read data from `junction` instead of the HAL.

## Interface

Each shim matches the interface of the class it replaces. See [shims/wpilib](/junction/shims/wpilib) for details on the modified classes.