# `junction/core`

> Central system for managing data, including reading and writing from log files and user code.

`junction/core` manages the flow of data inputs and output in each cycle. See [Getting Started: Logging](/docs/START-LOGGING.md) for details of how to utilize the logging framework in user code, including a list of data sources managed by `junction/core`.

## Packages

* [`inputs`](src/org/littletonrobotics/junction/inputs) - Built-in input data for interacting with WPILib (Driver Station, Network Tables, etc.)
* [`io`](src/org/littletonrobotics/junction/io) - Classes for receiving and replaying log data. Contains encode/decode classes for the RLOG format.

## Interface

Below are a few key classes that are utilized by user code. See documentation in each file for details of their interfaces.

* [`Logger.java`](/junction/core/src/org/littletonrobotics/junction/Logger.java) - Used to initially configure the logging framework, including and data receivers and replay sources. When running, input and output data are sent to this class to be logged and replayed.
* [`LoggedRobot.java`](/junction/core/src/org/littletonrobotics/junction/LoggedRobot.java) - This is the superclass of the main robot class. This class controls the timing of loop cycles (maintaining a regular cycle on the real robot or replaying more quickly in a simulator). It's also responsible for running the periodic code for the logging framework.
* [`LoggableInputs.java`](/junction/core/src/org/littletonrobotics/junction/inputs/LoggableInputs.java) - Input data from subsystems implements this interface, allowing the logging framework to freely read and write values.

## Building

The following Bazel targets are available (defined in [BUILD](BUILD)):

* `core` - Main Java library, depends on [`conduit/api`](/conduit/api) and WPILib.
* `core-compile` - Available as a dependency for other components, providing `core` during compile but excluding it from other JARs.
* `core-export` - Maven export for publishing `core`, used by GitHub Actions.

To publish `junction/core` to your local Maven repository, use the following command:

```bash
bazel run --define "maven_repo=file://$HOME/.m2/repository" //junction/core:core-export.publish
```

As with all components published to Maven, the version number can be set with a command-line flag:

```bash
bazel run --define "publishing_version=X.X.X" ...
```

## Testing

TODO.