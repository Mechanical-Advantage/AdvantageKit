# junction/core

Manages the flow of data each cycle between user code, WPILib, data receivers, and a replay source.

< Insert diagram and description here >

## Packages

* [inputs](/junction/core/src/org/littletonrobotics/junction/inputs) - Built-in input data for interacting with WPILib (Driver Station, Network Tables, etc.)
* [io](/junction/core/src/org/littletonrobotics/junction/io) - Classes for receiving and replaying log data. Contains encode/decode classes for the RLOG format.

## Interface

Below are a few key classes which are utilized by user code. See documentation in each file for details of their interfaces.

* [`Logger.java`](/junction/core/src/org/littletonrobotics/junction/Logger.java) - Used to initially configure the logging framework, including and data receivers and replay sources. When running, input and output data are sent to this class to be logged and replayed.
* [`LoggedRobot.java`](/junction/core/src/org/littletonrobotics/junction/LoggedRobot.java) - This is the superclass of the main robot class. This class controls the timing of loop cycles (maintaining a regular cycle on the real robot or replaying more quickly in a simiulator). It's also reponsible for running the periodic code for the logging framework.
* [`LoggableInputs.java`](/junction/core/src/org/littletonrobotics/junction/inputs/LoggableInputs.java) - Input data from subsystems implements this interface, allowing the logging framework to freely read and write values.

## Building

"junction/core" depends on WPILib and "conduit/api".

## Testing

TODO.