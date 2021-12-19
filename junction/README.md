# `junction`

> Primary component of logging, manages the flow of data between user code, WPILib, log files, network clients, etc.

`junction` is the primary component of the logging framework. It's the piece that user code interacts with, and interfaces with a variety of other components to manage data. Most code written to utilize the logging framework will interact with [`junction/core`](/junction/core).

## Components

* [`core`](/junction/core) - Central system for managing data, including reading and writing from log files and user code.
* [`shims`](/junction/shims) - Replaces components of other libraries (WPILib) to interact directly with `junction`.
  * [`shims/wpilib`](/junction/shims/wpilib) - Replaces WPILib components to read data from `junction` instead of the HAL.

## Interface

`junction` interacts directly with user code. Classes in [`core`](/junction/core) are used explicitly to set up and use the logging framework. Classes in [`shims`](/junction/shims) replace existing classes in other frameworks and so are used indirectly by user code. See the descriptions of each component for more details.