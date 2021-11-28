# junction

Junction is the primary component of the logging framework, written in Java. It handles the flow of data between user code, WPILib, log files, network clients, etc.

## Components

* [core](/junction/core) - Contains most of the code for managing data, including reading and writing from log files and user code.
* [shims](/junction/shims) - Replaces components of other libraries (WPILib) to interact directly with junction.

## Interface

Junction interacts directly with user code. Classes in "core" are used explicitly to set up and use the logging framework. Classes in "shims" replace existing classes in other frameworks and so are used indirectly by user code. See the descriptions of each component for more details.

## Building

"junction/core" and "junction/shims/wpilib" are separate buildable components of Junction. See descriptions of those components for more details.

## Testing

TODO.